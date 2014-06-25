/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ui.login;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.login.tasks.CheckAccountExistsTask;
import org.exoplatform.ui.login.tasks.CheckingTenantStatusTask;
import org.exoplatform.ui.login.tasks.LoginTask;
import org.exoplatform.ui.login.tasks.RequestTenantTask;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.WaitingDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;


/**
 * A proxy contains logic relating to network operation.
 * LoginProxy performs login operation  <br/>
 *
 * 2 possible cases of logging in:  <br/>
 *
 * 1> Login with account configured:  <br/>
 * For this case, this proxy is launched from either LoginActivity or LaunchActivity  <br/>
 *
 * 2> Login without account configured:  <br/>
 * A procedure is started to determine necessary information such as tenant name, username ...
 * for the operation <br/>
 * The proxy, in this case, is launched from either SignInActivity or SignInOnPremiseActivity:  <br/>
 * - Sign in: log in with email and password   <br/>
 * - On premise: log in with server url, username and password <br/>
 *
 */
public class LoginProxy implements
    CheckingTenantStatusTask.AsyncTaskListener,
    RequestTenantTask.AsyncTaskListener,
    LoginTask.AsyncTaskListener,
    CheckAccountExistsTask.AsyncTaskListener {


  /***=== Data ===***/
  private String                mNewUserName;

  private String                mNewPassword;

  private String                mTenant;

  private String                mDomain;

  private String                mEmail;


  private Context               mContext;

  private AccountSetting        mSetting;

  private Resources             mResource;


  /**=== Async Tasks ===**/
  private LoginTask             mLoginTask;

  private RequestTenantTask     mRequestTenantTask;


  /** the warning dialog that shows error */
  private LoginWarningDialog    mWarningDialog;

  private LoginWaitingDialog    mProgressDialog;


  /**=== States ===**/
  private int                   mLaunchMode;

  private int                   mState                 = WORKING;

  public  static final int      WITH_EXISTING_ACCOUNT  = 0;

  public  static final int      WITH_USERNAME          = 10;
  public  static final int      WITH_EMAIL             = 11;

  public  static final String   USERNAME               = "USERNAME";
  public  static final String   PASSWORD               = "PASSWORD";
  public  static final String   EMAIL                  = "EMAIL";
  public  static final String   DOMAIN                 = "DOMAIN";
  public  static final String   SHOW_PROGRESS          = "SHOW_PROGRESS";

  private static final int      WORKING                = 100;

  private static final int      FINISHED               = 101;

  private static final String TAG = "eXo____LoginProxy____";


  /** data should be verified before entering LoginProxy */
  public LoginProxy(Context context, int state, Bundle loginData) {
    SettingUtils.setDefaultLanguage(context);
    mContext    = context;
    mResource   = mContext.getResources();
    mSetting    = AccountSetting.getInstance();
    mLaunchMode = state;

    initStates(loginData);
  }


  /**
   * Perform initialization of data   <br/>
   *
   * - if domain supplied, it must start with Http://   <br/>
   *
   * @param loginData
   */
  private void initStates(Bundle loginData) {

    switch (mLaunchMode) {

      /**
       * If any error happens,
       * - redirect user to Login screen if not launched from login
       * - otherwise, dismiss the dialog
       */
      case WITH_EXISTING_ACCOUNT:
        mNewUserName = loginData.getString(USERNAME);
        mNewPassword = loginData.getString(PASSWORD);
        mDomain      = loginData.getString(DOMAIN);
        mTenant      = getTenant(mDomain);
        // TODO: if this is a cloud server with invalid url, should raise a warning
        mEmail       = mNewUserName + "@" + mTenant + ".com";
        mWarningDialog = new LoginWarningDialog(mContext);

        mProgressDialog =  loginData.getBoolean(SHOW_PROGRESS, true) ?
            new LoginWaitingDialog(mContext, null, mResource.getString(R.string.SigningIn)) : null;
        break;

      /**
       * Login from Sign in screen
       */
      case WITH_EMAIL:
        mEmail          = loginData.getString(EMAIL);
        mNewPassword    = loginData.getString(PASSWORD);
        mWarningDialog  = new LoginWarningDialog(mContext);
        mProgressDialog =
            new LoginWaitingDialog(mContext, null, mResource.getString(R.string.SigningIn));

        if (!checkNetworkConnection()) return ;
        mProgressDialog.show();

        /** figure out which tenant is */
        mRequestTenantTask = new RequestTenantTask();
        mRequestTenantTask.setListener(this);
        mRequestTenantTask.execute(mEmail);
        break;

      /**
       * Log in from Sign in on premise
       */
      case WITH_USERNAME:
        mNewUserName = loginData.getString(USERNAME);
        mNewPassword = loginData.getString(PASSWORD);
        mDomain      = loginData.getString(DOMAIN);
        mTenant      = getTenant(mDomain);
        mEmail       = mNewUserName + "@" + mTenant + ".com";

        mWarningDialog = new LoginWarningDialog(mContext);
        mProgressDialog =
            new LoginWaitingDialog(mContext, null, mResource.getString(R.string.SigningIn));

        break;
    }

  }


  public LoginWarningDialog getWarningDialog() {
    return mWarningDialog;
  }

  @Override
  public void onRequestingTenantFinished(int result, String[] userAndTenant) {
    Log.i(TAG, "onRequestingTenantFinished: " + result);
    if (result != ExoConnectionUtils.TENANT_OK) {
      finish(result);
      return ;
    }

    mNewUserName = userAndTenant[0];
    mTenant      = userAndTenant[1];
    mDomain      = ExoConnectionUtils.HTTP + mTenant + "."
        + ExoConnectionUtils.EXO_CLOUD_WS_DOMAIN;

    performLogin();
  }

  /**
   * Figure out tenant based on url of server  <br/>
   * Url of cloud server must be in the form of http://<tenant>.<exo_cloud_domain>  <br/>
   * For example: http://exoplatform.wks-acc.exoplatform.org
   *
   */
  private String getTenant(String domain) {
    /** strip off http */
    String cloudDomain = domain.startsWith(ExoConnectionUtils.HTTP) ?
        domain.substring(ExoConnectionUtils.HTTP.length()) : domain;
    int idx = cloudDomain.indexOf(ExoConnectionUtils.EXO_CLOUD_WS_DOMAIN);
    if (idx <= 1) return null;
    String tenant = cloudDomain.substring(0, idx);
    if (!tenant.endsWith(".")) return null;        // raise an exception at this point for invalid cloud server format
    tenant = tenant.substring(0, tenant.length() - 1);
    return (tenant.contains(".")) ? null : tenant;
  }


  /**
   * Actual logic of login
   */
  public void performLogin() {
    if (mState == FINISHED) return ;

    if (!checkNetworkConnection()) return ;

    if (mProgressDialog != null) mProgressDialog.show();

    /** cloud server - check tenant status */
    if (mTenant != null) {
      CheckingTenantStatusTask checkingTenantStatus = new CheckingTenantStatusTask();
      checkingTenantStatus.setListener(this);
      checkingTenantStatus.execute(mTenant, mEmail);
    }
    else launchLoginTask();
  }


  @Override
  public void onCheckingTenantStatusFinished(int result) {
    if (result != ExoConnectionUtils.TENANT_OK) {
      finish(result);
      return ;
    }

    /** cloud server - check email existence */
    if (mTenant != null && mLaunchMode == WITH_EMAIL) {
      
      CheckAccountExistsTask accountExists = new CheckAccountExistsTask();
      accountExists.setListener(this);
      accountExists.execute(mNewUserName, mTenant);
      
      return;
      
    }
    
    launchLoginTask();

  }
  
  @Override
  public void onCheckAccountExistsFinished(boolean accountExists) {
    
    if (accountExists)
      launchLoginTask();
    else
      finish(ExoConnectionUtils.SIGNIN_NO_ACCOUNT);
    
  }

  private void launchLoginTask() {
    mDomain = !(mDomain.startsWith(ExoConnectionUtils.HTTP) || mDomain.startsWith(ExoConnectionUtils.HTTPS))
        ? ExoConnectionUtils.HTTP + mDomain: mDomain;
    mLoginTask = new LoginTask();
    mLoginTask.setListener(this);
    mLoginTask.execute(mNewUserName, mNewPassword, mDomain);
  }

  private boolean checkNetworkConnection() {
    if (!ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      finish(ExoConnectionUtils.SIGNIN_CONNECTION_ERR);
      return false;
    }
    return true;
  }

  @Override
  public void onLoggingInFinished(int result) {
    finish(result);
  }

  /**
   * Handling final result of login operation
   *
   * @param result
   */
  private void finish(int result) {
    Log.i(TAG, "PROXY FINISHED - result: " + result);

    if (mState == FINISHED) return ;
    mState = FINISHED;

    if (mProgressDialog!= null) mProgressDialog.dismiss();

    switch (result) {
      case ExoConnectionUtils.LOGIN_INCOMPATIBLE:
        mWarningDialog.setMessage(mResource.getString(R.string.CompliantMessage)).show();
        break;

      case ExoConnectionUtils.SIGNIN_SERVER_NAV:
        mWarningDialog.setMessage(mResource.getString(R.string.ServerNotAvailable)).show();
        break;

      case ExoConnectionUtils.SIGNIN_NO_TENANT_FOR_EMAIL:
        mWarningDialog.setMessage(mResource.getString(R.string.NoAccountExists)).show();
        break;

      case ExoConnectionUtils.SIGNIN_NO_ACCOUNT:
        mWarningDialog.setMessage(mResource.getString(R.string.NoAccountExists)).show();
        break;

      case ExoConnectionUtils.LOGIN_SERVER_RESUMING:
        mWarningDialog.setMessage(mResource.getString(R.string.ServerResuming)).show();
        break;

      case ExoConnectionUtils.LOGIN_UNAUTHORIZED:
        mWarningDialog.setMessage(mResource.getString(R.string.InvalidCredentials)).show();
        break;

      case ExoConnectionUtils.SIGNIN_CONNECTION_ERR:
        mWarningDialog.setMessage(mResource.getString(R.string.ServerNotAvailable)).show();
        break;

      default:
        mWarningDialog.setMessage(mResource.getString(R.string.ServerNotAvailable)).show();
        break;

      /** Login successfully - save data */
      case ExoConnectionUtils.LOGIN_SUSCESS:

        /* Set social and document settings */
        StringBuilder builder = new StringBuilder(mDomain)
            .append("_").append(mNewUserName).append("_");

        mSetting.socialKey      = builder.toString() + ExoConstants.SETTING_SOCIAL_FILTER;
        mSetting.socialKeyIndex = builder.toString() + ExoConstants.SETTING_SOCIAL_FILTER_INDEX;
        mSetting.documentKey    = builder.toString() + ExoConstants.SETTING_DOCUMENT_SHOW_HIDDEN_FILE;

        boolean needToSave = false;

        ServerObjInfo newServerObj;
        int serverIdx;
        if (mLaunchMode == WITH_EXISTING_ACCOUNT) {
          newServerObj             =  mSetting.getCurrentServer().clone();
          newServerObj.username    =  mNewUserName;
          newServerObj.password    =  mNewPassword;
        }
        else {
          newServerObj = new ServerObjInfo();
          newServerObj.serverName  =  mResource.getString(R.string.DefaultServer);
          newServerObj.serverUrl   =  mDomain;
          newServerObj.username    =  mNewUserName;
          newServerObj.password    =  mNewPassword;
        }

        ArrayList<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList(mContext);
        int duplicatedIdx = serverList.indexOf(newServerObj);
        /** No duplicate */
        if (duplicatedIdx == -1) {
          needToSave = true;
          serverList.add(newServerObj);
          serverIdx = serverList.size()-1;
        }
        else {
          /** Duplicate server */
          ServerObjInfo duplicatedServer = serverList.get(duplicatedIdx);
          serverIdx  = duplicatedIdx;
          /** Check password */
          if (!duplicatedServer.password.equals(newServerObj.password)) {
            duplicatedServer.password = newServerObj.password;
            needToSave = true;
          }
        }

        mSetting.setCurrentServer(serverList.get(serverIdx));
        mSetting.setDomainIndex(String.valueOf(serverIdx));

        /** Save config */
        if (needToSave) SettingUtils.persistServerSetting(mContext);
      break;
    }

    if (mProgressDialog != null) mProgressDialog.dismiss();

    /** invoke listeners */
    if (mListener!= null) mListener.onLoginFinished(result == ExoConnectionUtils.LOGIN_SUSCESS);
  }

  public void onCancelLoad() {
    if (mLoginTask != null && mLoginTask.getStatus() == LoginTask.Status.RUNNING) {
      mLoginTask.cancel(true);
      mLoginTask = null;
    }
  }


  /**
   * A waiting dialog that only shows once even called multiple times
   */
  private class LoginWaitingDialog extends WaitingDialog {

    public boolean mIsShowing = false;

    public LoginWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      onCancelLoad();
    }

    @Override
    public void show() {
      if (mIsShowing) return ;
      mIsShowing = true;
      super.show();
    }
  }

  private ProxyListener mListener;

  public void setListener(ProxyListener listener) {
    mListener = listener;
  }

  public interface ProxyListener {

    void onLoginFinished(boolean result);
  }

}
