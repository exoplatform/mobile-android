package org.exoplatform.controller.login;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.conn.HttpHostConnectException;
import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.utils.*;
import org.exoplatform.utils.image.FileCache;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;

/**
 * Performs login
 */
public class LoginController {

  private AccountSetting     mSetting;

  /* new username to login, might be different from username in current setting */
  private String             mNewUserName;

  /* new password to login, might be different from password in current setting */
  private String             mNewPassword;

  /* current domain */
  private String             mDomain;

  private Context            mContext;

  private LoginTask          mLoadTask;

  private String             strSigning;

  private String             blankError;

  private String             strNetworkConnectionFailed;

  private String             strServerUnreachable;

  private String             strServerInvalid;

  private String             strUserNamePasswordFailed;

  private String             mobileNotCompilant;

  private String             titleString;

  private String             okString;

  private WarningDialog      mWarningDialog;

  private LoginWaitingDialog mProgressDialog;

  private boolean            mIsShowingDialog;

  private Resources          resource;

  private Activity           mCurrentActivity;

  private static final String TAG = "eXoLoginController";

  public LoginController(Activity context, String user, String pass, boolean isShowingDialog) {
    SettingUtils.setDefaultLanguage(context);
    mContext = context;
    mCurrentActivity = context;
    resource = mContext.getResources();
    mNewUserName = user;
    mNewPassword = pass;
    mSetting     = AccountSetting.getInstance();
    mDomain  = mSetting.getDomainName();
    mIsShowingDialog = isShowingDialog;

    getLanguage();
    if (checkLogin()) onLoad();
    else {
      if (isShowingDialog) {
        mWarningDialog = new WarningDialog(mContext, titleString, blankError, okString);
        mWarningDialog.show();
      }
    }
  }

  private boolean checkLogin() {
    if (mNewUserName == null || mNewUserName.length() == 0) {
      blankError = resource.getString(R.string.NoUsernameEnter);
    } else if (mNewPassword == null || mNewPassword.length() == 0) {
      blankError = resource.getString(R.string.NoPasswordEnter);
    } else if (mDomain == null || mDomain.length() == 0) {
      blankError = resource.getString(R.string.NoServerSelected);
    } else return true;

    return false;
  }

  private void onLoad() {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mLoadTask == null || mLoadTask.getStatus() == LoginTask.Status.FINISHED) {
        mLoadTask = (LoginTask) new LoginTask().execute();
      }
    } else {
      new ConnectionErrorDialog(mContext).show();
    }
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == LoginTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  private void getLanguage() {
    strSigning = resource.getString(R.string.SigningIn);
    strNetworkConnectionFailed = resource.getString(R.string.ConnectionError);
    strUserNamePasswordFailed = resource.getString(R.string.UserNamePasswordFailed);
    mobileNotCompilant = resource.getString(R.string.CompliantMessage);
    strServerInvalid = resource.getString(R.string.ServerInvalid);
    strServerUnreachable = resource.getString(R.string.ServerUnreachable);
    titleString = resource.getString(R.string.Warning);
    okString = resource.getString(R.string.OK);

  }

  public class LoginTask extends AsyncTask<Void, Void, Integer> {

    private HttpResponse response;

    private boolean      isCompliant;

    private static final String TAG = "eXoLoginTask";

    @Override
    public void onPreExecute() {
      if (mIsShowingDialog) {
        mProgressDialog = new LoginWaitingDialog(mContext, null, strSigning);
        mProgressDialog.show();
      }
    }

    @Override
    public Integer doInBackground(Void... params) {

      try {
        String versionUrl = SocialActivityUtil.getDomain() + ExoConstants.DOMAIN_PLATFORM_VERSION;
        response = ExoConnectionUtils.getPlatformResponse(mNewUserName, mNewPassword, versionUrl);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND){
          return ExoConnectionUtils.LOGIN_INCOMPATIBLE;
        }
        isCompliant = ExoConnectionUtils.checkPLFVersion(response);
        ExoDocumentUtils.setRepositoryHomeUrl(mNewUserName, mDomain);
        return ExoConnectionUtils.checkPlatformRespose(response);
      } catch(HttpHostConnectException e) {
        return ExoConnectionUtils.LOGIN_FAILED;
      } catch (IOException e) {
        return ExoConnectionUtils.LOGIN_INCOMPATIBLE;
      }

    }

    @Override
    public void onPostExecute(Integer result) {
      if (result == ExoConnectionUtils.LOGIN_INCOMPATIBLE) {
        mWarningDialog = new WarningDialog(mContext, titleString, mobileNotCompilant, okString);
        mWarningDialog.show();
      } else if (result == ExoConnectionUtils.LOGIN_SUSCESS) {
        /* Set social and document settings */
        StringBuilder builder = new StringBuilder(mDomain)
            .append("_").append(mNewUserName).append("_");
        mSetting.socialKey = builder.toString() + ExoConstants.SETTING_SOCIAL_FILTER;
        mSetting.socialKeyIndex = builder.toString() + ExoConstants.SETTING_SOCIAL_FILTER_INDEX;
        mSetting.documentKey = builder.toString() + ExoConstants.SETTING_DOCUMENT_SHOW_HIDDEN_FILE;

        /**
         * Login can only be done with existing server
         * but there's case when user inputs new username changes or
         * new password, we need to persist both
         */
        boolean needToSave = false;
        ArrayList<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList();

        if ( !mNewUserName.equals(mSetting.getUsername()) ) {    // new credential
          ServerObjInfo newServer = mSetting.getCurrentServer().clone();
          newServer.username   = mNewUserName;
          newServer.password   = mNewPassword;
          newServer.isRememberEnabled  = true;
          newServer.isAutoLoginEnabled = true;

          serverList.add(newServer);
          // set current selected server to the new server
          mSetting.setDomainIndex(String.valueOf(serverList.size() - 1));
          mSetting.setCurrentServer(newServer);
          needToSave = true;
        }
        else {
          // same user, but password might change
          if ( !mSetting.getPassword().equals(mNewPassword) ) {
            needToSave = true;
            mSetting.getCurrentServer().password = mNewPassword;
          }
        }

        // Save config
        if (needToSave) SettingUtils.persistServerSetting(mContext);

        /* Checking platform version */
        if (isCompliant) {
          Intent next = new Intent(mContext, HomeActivity.class);
          next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mContext.startActivity(next);
          if (mSetting.isAutoLoginEnabled()) mCurrentActivity.finish();
        } else {
          mWarningDialog = new WarningDialog(mContext, titleString, mobileNotCompilant, okString);
          mWarningDialog.show();
        }

      } else if (result == ExoConnectionUtils.LOGIN_UNAUTHORIZED) {
        mWarningDialog = new WarningDialog(mContext, titleString, strUserNamePasswordFailed, okString);
        mWarningDialog.show();
      } else if (result == ExoConnectionUtils.LOGIN_INVALID) {
        mWarningDialog = new WarningDialog(mContext, titleString, strServerInvalid, okString);
        mWarningDialog.show();
      } else if (result == ExoConnectionUtils.LOGIN_FAILED) {
        mWarningDialog = new WarningDialog(mContext, titleString, strServerUnreachable, okString);
        mWarningDialog.show();
      } else {
        mWarningDialog = new WarningDialog(mContext, titleString, strNetworkConnectionFailed, okString);
        mWarningDialog.show();
      }
      if (mIsShowingDialog) mProgressDialog.dismiss();
    }

  }

  private class LoginWaitingDialog extends WaitingDialog {

    public LoginWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      onCancelLoad();
    }

  }

}
