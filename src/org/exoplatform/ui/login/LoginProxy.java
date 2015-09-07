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
import org.exoplatform.base.BaseActivity;
import org.exoplatform.base.BaseActivity.BasicActivityLifecycleCallbacks;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.login.tasks.CheckAccountExistsTask;
import org.exoplatform.ui.login.tasks.CheckingTenantStatusTask;
import org.exoplatform.ui.login.tasks.LoginTask;
import org.exoplatform.ui.login.tasks.RequestTenantTask;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoUtils;
import org.exoplatform.utils.Log;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.WaitingDialog;

import com.crashlytics.android.Crashlytics;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask.Status;
import android.os.Bundle;

/**
 * A proxy contains logic relating to network operation. LoginProxy performs
 * login operation <br/>
 * 2 possible cases of logging in: <br/>
 * 1> Login with account configured: <br/>
 * For this case, this proxy is launched from either LoginActivity or
 * LaunchActivity <br/>
 * 2> Login without account configured: <br/>
 * A procedure is started to determine necessary information such as tenant
 * name, username ... for the operation <br/>
 * The proxy, in this case, is launched from either SignInActivity or
 * SignInOnPremiseActivity: <br/>
 * - Sign in: log in with email and password <br/>
 * - On premise: log in with server url, username and password <br/>
 */
public class LoginProxy implements CheckingTenantStatusTask.AsyncTaskListener, RequestTenantTask.AsyncTaskListener,
    LoginTask.AsyncTaskListener, CheckAccountExistsTask.AsyncTaskListener {

  // connection status of the app
  public static boolean                   userIsLoggedIn;

  /*** === Data === ***/
  private String                          mNewUserName;

  private String                          mNewPassword;

  private String                          mTenant;

  private String                          mAccountName;

  private String                          mDomain;

  private String                          mUpdatedDomain;

  private String                          mEmail;

  private Context                         mContext;

  private AccountSetting                  mSetting;

  private Resources                       mResource;

  /** === Async Tasks === **/
  private LoginTask                       mLoginTask;

  private RequestTenantTask               mRequestTenantTask;

  /** the warning dialog that shows error */
  private LoginWarningDialog              mWarningDialog;

  private LoginWaitingDialog              mProgressDialog;

  private BasicActivityLifecycleCallbacks mLifecycleCallback    = new BasicActivityLifecycleCallbacks() {

    public void onPause(org.exoplatform.base.BaseActivity act) {
      if (act == mContext) {
        // TODO implement correct behavior, current only dismissUI and cancel
        // current task
        if (Log.LOGD)
          Log.d(TAG, "onPause cancel task");
        setListener(null);
        if (mLoginTask != null && mLoginTask.getStatus() == Status.RUNNING) {
          mLoginTask.cancel(true);
        }
        dismissDialog();
      }
    };
  };

  /** === States === **/
  private int                             mLaunchMode;

  private int                             mState                = WORKING;

  public static final int                 WITH_EXISTING_ACCOUNT = 0;

  public static final int                 WITH_USERNAME         = 10;

  public static final int                 WITH_EMAIL            = 11;

  public static final int                 SWITCH_ACCOUNT        = 12;

  public static final String              USERNAME              = "USERNAME";

  public static final String              PASSWORD              = "PASSWORD";

  public static final String              EMAIL                 = "EMAIL";

  public static final String              DOMAIN                = "DOMAIN";

  public static final String              ACCOUNT_NAME          = "ACCOUNT_NAME";

  public static final String              SHOW_PROGRESS         = "SHOW_PROGRESS";

  private static final int                WORKING               = 100;

  private static final int                FINISHED              = 101;

  private static final String             TAG                   = "eXo____LoginProxy____";

  /** data should be verified before entering LoginProxy */
  public LoginProxy(Context context, int state, Bundle loginData) {
    SettingUtils.setDefaultLanguage(context);
    mContext = context;
    mResource = mContext.getResources();
    mSetting = AccountSetting.getInstance();
    mLaunchMode = state;
    mUpdatedDomain = null;
    if (mContext instanceof BaseActivity) {
      ((BaseActivity) mContext).addLifeCycleObserverRef(mLifecycleCallback);
    }
    initStates(loginData);
  }

  /**
   * Perform initialization of data <br/>
   * - if domain supplied, it must start with Http:// <br/>
   * 
   * @param loginData
   */
  private void initStates(Bundle loginData) {

    mWarningDialog = new LoginWarningDialog(mContext);
    mProgressDialog = new LoginWaitingDialog(mContext, null, mResource.getString(R.string.SigningIn));

    switch (mLaunchMode) {

    /**
     * Login from LoginActivity screen If any error happens, - redirect user to
     * Login screen if not launched from login - otherwise, dismiss the dialog
     */
    case WITH_EXISTING_ACCOUNT:
      mNewUserName = loginData.getString(USERNAME);
      mNewPassword = loginData.getString(PASSWORD);
      mDomain = loginData.getString(DOMAIN);
      mTenant = getTenant(mDomain);
      // TODO: if this is a cloud server with invalid url, should raise a
      // warning
      mEmail = mNewUserName + "@" + mTenant + ".com";

      mProgressDialog = loginData.getBoolean(SHOW_PROGRESS, true)
                                                                  ? new LoginWaitingDialog(mContext,
                                                                                           null,
                                                                                           mResource.getString(R.string.SigningIn))
                                                                  : null;
      break;

    /**
     * Login from SignInActivity screen
     */
    case WITH_EMAIL:
      mEmail = loginData.getString(EMAIL);
      mNewPassword = loginData.getString(PASSWORD);

      if (!checkNetworkConnection())
        return;
      mProgressDialog.show();

      /** figure out which tenant is */
      mRequestTenantTask = new RequestTenantTask();
      mRequestTenantTask.setListener(this);
      mRequestTenantTask.execute(mEmail);
      break;

    /**
     * Login from SignInOnPremiseActivity screen
     */
    case WITH_USERNAME:
      mNewUserName = loginData.getString(USERNAME);
      mNewPassword = loginData.getString(PASSWORD);
      mDomain = loginData.getString(DOMAIN);
      mTenant = getTenant(mDomain);
      mEmail = mNewUserName + "@" + mTenant + ".com";
      break;
    /**
     * Login from AccountSwitcherFragment screen
     */
    case SWITCH_ACCOUNT:
      mNewUserName = loginData.getString(USERNAME);
      mNewPassword = loginData.getString(PASSWORD);
      mDomain = loginData.getString(DOMAIN);
      mAccountName = loginData.getString(ACCOUNT_NAME);
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
      return;
    }

    mNewUserName = userAndTenant[0];
    mTenant = userAndTenant[1];
    mDomain = ExoConnectionUtils.HTTPS + mTenant + "." + ExoConnectionUtils.EXO_CLOUD_WS_DOMAIN;

    performLogin();
  }

  /**
   * Figure out tenant based on url of server <br/>
   * <<<<<<< HEAD Url of cloud server must be in the form of https://<tenant>.
   * ======= Url of cloud server must be in the form of http://<tenant>. >>>>>>>
   * 5856d29... Support server URL and HTTP to HTTPS redirections
   * <exo_cloud_domain> <br/>
   * For example: http://exoplatform.wks-acc.exoplatform.org
   */
  private String getTenant(String domain) {
    /** strip off https:// */
    String cloudDomain = domain.startsWith(ExoConnectionUtils.HTTPS) ? domain.substring(ExoConnectionUtils.HTTPS.length())
                                                                     : domain;
    int idx = cloudDomain.indexOf(ExoConnectionUtils.EXO_CLOUD_WS_DOMAIN);
    if (idx <= 1)
      return null;
    String tenant = cloudDomain.substring(0, idx);
    if (!tenant.endsWith("."))
      return null; // raise an exception at this point for invalid cloud
                   // server format
    tenant = tenant.substring(0, tenant.length() - 1);
    return (tenant.contains(".")) ? null : tenant;
  }

  /**
   * Actual logic of login
   */
  public void performLogin() {
    if (mState == FINISHED)
      return;

    if (!checkNetworkConnection())
      return;

    if (mProgressDialog != null && !mProgressDialog.isShowing())
      mProgressDialog.show();

    /** cloud server - check tenant status */
    if (mTenant != null) {
      CheckingTenantStatusTask checkingTenantStatus = new CheckingTenantStatusTask();
      checkingTenantStatus.setListener(this);
      checkingTenantStatus.execute(mTenant, mEmail);
    } else
      launchLoginTask();
  }

  @Override
  public void onCheckingTenantStatusFinished(int result) {
    if (result != ExoConnectionUtils.TENANT_OK) {
      finish(result);
      return;
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
                                                                                                             ? ExoConnectionUtils.HTTP
                                                                                                                 + mDomain
                                                                                                             : mDomain;
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
    try {
      if (!((Activity) mContext).isFinishing()) {
        finish(result);
      } else {
        String resultStr = result == ExoConnectionUtils.LOGIN_SUCCESS ? "success" : "failed";
        Log.i(TAG, String.format("Login %s but activity is finishing...", resultStr));
      }
    } catch (ClassCastException e) {
      Log.i(TAG, "Login proxy was not executed in the context of an activity...");
    }

  }

  @Override
  public void onUpdateDomain(String newDomain) {
    if (newDomain != null && !newDomain.equalsIgnoreCase(mDomain)) {
      mUpdatedDomain = newDomain;
    }
  }

  @Override
  public void onCanceled() {
    dismissDialog();
  }

  private void dismissDialog() {
    if (mProgressDialog != null && mProgressDialog.isShowing())
      mProgressDialog.dismiss();
  }

  /**
   * Handling final result of login operation
   * 
   * @param result
   */
  private void finish(int result) {
    Log.i(TAG, "PROXY FINISHED - result: " + result);

    userIsLoggedIn = false;

    if (mState == FINISHED)
      return;
    mState = FINISHED;
    dismissDialog();

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

    /** Login successful - save data */
    case ExoConnectionUtils.LOGIN_SUCCESS:

      /* Set social and document settings */
      StringBuilder builder = new StringBuilder(mDomain).append("_").append(mNewUserName).append("_");

      mSetting.socialKey = builder.toString() + ExoConstants.SETTING_SOCIAL_FILTER;
      mSetting.socialKeyIndex = builder.toString() + ExoConstants.SETTING_SOCIAL_FILTER_INDEX;
      mSetting.documentKey = builder.toString() + ExoConstants.SETTING_DOCUMENT_SHOW_HIDDEN_FILE;

      ExoAccount newAccountObj;
      int serverIdx;
      if (mLaunchMode == SWITCH_ACCOUNT) {
        newAccountObj = new ExoAccount();
        newAccountObj.username = mNewUserName;
        newAccountObj.password = mNewPassword;
        newAccountObj.serverUrl = mDomain;
        newAccountObj.accountName = mAccountName;
      } else if (mLaunchMode == WITH_EXISTING_ACCOUNT) {
        newAccountObj = mSetting.getCurrentAccount().clone();
        newAccountObj.username = mNewUserName;
        newAccountObj.password = mNewPassword;
      } else {
        newAccountObj = new ExoAccount();
        String name = mTenant;
        if (name == null)
          name = getTenant(mDomain);
        if (name == null)
          name = ExoUtils.getAccountNameFromURL(mDomain, mResource.getString(R.string.DefaultServer));
        newAccountObj.accountName = ExoUtils.capitalize(name);
        newAccountObj.serverUrl = mDomain;
        newAccountObj.username = mNewUserName;
        newAccountObj.password = mNewPassword;
      }
      newAccountObj.lastLoginDate = System.currentTimeMillis();

      ArrayList<ExoAccount> serverList = ServerSettingHelper.getInstance().getServerInfoList(mContext);
      int duplicatedIdx = serverList.indexOf(newAccountObj);
      // The account used to login is not a duplicate, it is added to the
      // list
      if (duplicatedIdx == -1) {
        serverList.add(newAccountObj);
        serverIdx = serverList.size() - 1;
      } else {
        // The account already exists, its index in the list is used as
        // the current account index
        ExoAccount duplicatedServer = serverList.get(duplicatedIdx);
        serverIdx = duplicatedIdx;
        // The password property is updated if it changed
        if (!duplicatedServer.password.equals(newAccountObj.password)) {
          duplicatedServer.password = newAccountObj.password;
        }
        // The server's URL is updated if it changed (e.g. from a 301 redir)
        if (mUpdatedDomain != null) {
          duplicatedServer.serverUrl = mUpdatedDomain;
        }
        duplicatedServer.lastLoginDate = newAccountObj.lastLoginDate;
      }

      mSetting.setCurrentAccount(serverList.get(serverIdx));
      mSetting.setDomainIndex(String.valueOf(serverIdx));
      userIsLoggedIn = true;

      // Save config each time to update the last login date property
      SettingUtils.persistServerSetting(mContext);

      // Set Crashlytics user information
      Crashlytics.setUserName(mNewUserName);
      Crashlytics.setString("ServerDomain", mDomain);
      break;
    }

    /** invoke listeners */
    if (mWarningDialog != null && mLaunchMode == SWITCH_ACCOUNT && result != ExoConnectionUtils.LOGIN_SUCCESS) {
      // when switching account, wait until the user dismisses the dialog
      // to inform the caller if the login has failed
      mWarningDialog.setViewListener(new LoginWarningDialog.ViewListener() {
        @Override
        public void onClickOk(LoginWarningDialog dialog) {
          if (mListener != null)
            mListener.onLoginFinished(false);
        }
      });
    } else if (mListener != null)
      // otherwise, inform the caller immediately with the result
      mListener.onLoginFinished(result == ExoConnectionUtils.LOGIN_SUCCESS);
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
      if (mIsShowing)
        return;
      mIsShowing = true;
      super.show();
    }
  }

  private ProxyListener mListener;

  public void setListener(ProxyListener listener) {
    mListener = listener;
  }

  public interface ProxyListener {
    /**
     * Notify the caller when the proxy has finished
     * 
     * @param result true if the login is successful, false otherwise
     */
    void onLoginFinished(boolean result);
  }

}
