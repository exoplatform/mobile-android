package org.exoplatform.controller.login;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.conn.HttpHostConnectException;
import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.ui.LaunchActivity;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.utils.*;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;

/**
 * Performs login operation
 * can be launched from LoginActivity or LaunchActivity
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

  private Resources          mResource;

  /** the activity that launches this controller */
  private Activity           mCurrentActivity;

  /** indicate that this controlelr is launched from Launch activity */
  private boolean            mIsLaunchFromLaunchActivity;

  /** the warning dialog that shows error */
  private WarningDialog      mWarningDialog;

  private LoginWaitingDialog mProgressDialog;

  /**===  Dialog Messages  ===*/
  private String             strSigning;

  private String             blankError;

  private String             strNetworkConnectionFailed;

  private String             strServerUnreachable;

  private String             strServerInvalid;

  private String             strUserNamePasswordFailed;

  private String             mobileNotCompilant;

  private String             titleString;

  private String             okString;

  private String             redirectToLogin;

  private String             loginWarningMsg;

  private static final String TAG = "eXoLoginController";

  public LoginController(Activity context, String user, String pass) {
    SettingUtils.setDefaultLanguage(context);
    mContext = context;
    mCurrentActivity = context;
    mResource = mContext.getResources();
    mNewUserName = user;
    mNewPassword = pass;
    mSetting     = AccountSetting.getInstance();
    mDomain  = mSetting.getDomainName();
    mIsLaunchFromLaunchActivity = (mCurrentActivity instanceof LaunchActivity);

    getLanguage();
    if (checkLogin()) onLoad();
    else showWarningDialog(blankError, redirectToLogin, okString);
  }

  /**
   * Show warning dialog depending on what activity launches this controller
   *
   * @param msg               warning message
   * @param okStringForLaunch text in the button
   * @param defaultOkString   default text in the button
   */
  private void showWarningDialog(String msg, String okStringForLaunch, String defaultOkString) {
    mWarningDialog = mIsLaunchFromLaunchActivity ?
      new LoginWarningDialog(mContext, loginWarningMsg, msg, okStringForLaunch) :
      new WarningDialog(mContext, titleString, msg, defaultOkString);

    mWarningDialog.show();
  }

  private boolean checkLogin() {
    if (mNewUserName == null || mNewUserName.length() == 0) {
      blankError = mResource.getString(R.string.NoUsernameEnter);
    } else if (mNewPassword == null || mNewPassword.length() == 0) {
      blankError = mResource.getString(R.string.NoPasswordEnter);
    } else if (mDomain == null || mDomain.length() == 0) {
      blankError = mResource.getString(R.string.NoServerSelected);
    } else return true;

    return false;
  }

  private void onLoad() {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mLoadTask == null || mLoadTask.getStatus() == LoginTask.Status.FINISHED) {
        mLoadTask = (LoginTask) new LoginTask().execute();
      }
    }
    else showWarningDialog(strNetworkConnectionFailed, redirectToLogin, okString);
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == LoginTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  private void getLanguage() {
    strSigning = mResource.getString(R.string.SigningIn);
    strNetworkConnectionFailed = mResource.getString(R.string.ConnectionError);
    strUserNamePasswordFailed = mResource.getString(R.string.UserNamePasswordFailed);
    mobileNotCompilant = mResource.getString(R.string.CompliantMessage);
    strServerInvalid = mResource.getString(R.string.ServerInvalid);
    strServerUnreachable = mResource.getString(R.string.ServerUnreachable);
    titleString = mResource.getString(R.string.Warning);
    okString = mResource.getString(R.string.OK);
    redirectToLogin = mResource.getString(R.string.RedirectToLogin);
    loginWarningMsg = mResource.getString(R.string.LoginWarningMsg);
  }


  public class LoginTask extends AsyncTask<Void, Void, Integer> {

    private HttpResponse response;

    private boolean      isCompliant;

    private static final String TAG = "eXoLoginTask";

    @Override
    public void onPreExecute() {
      if (!mIsLaunchFromLaunchActivity) {
        mProgressDialog = new LoginWaitingDialog(mContext, null, strSigning);
        mProgressDialog.show();
      }
      ExoConnectionUtils.loggingOut();
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
      Log.d(TAG, "onPostExecute - login result: " + result);
      String dialogMsg = "";

      switch (result) {
        case ExoConnectionUtils.LOGIN_INCOMPATIBLE:
          dialogMsg = mobileNotCompilant;
          break;

        case ExoConnectionUtils.LOGIN_SUSCESS:
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

          if ( !mNewUserName.equals(mSetting.getUsername()) ) {    // new username - new credential
            Log.i(TAG, "new user: " + mNewUserName);
            ServerObjInfo newServer = mSetting.getCurrentServer().clone();
            newServer.username   = mNewUserName;
            newServer.password   = mNewPassword;
            newServer.isRememberEnabled  = true;
            newServer.isAutoLoginEnabled = true;

            if (mSetting.getUsername().equals("")) {
              // Override old server
              Log.i(TAG, "override old server");
              mSetting.getCurrentServer().username = mNewUserName;
              mSetting.getCurrentServer().password = mNewPassword;
              needToSave = true;
            }
            else {
              // Add a new server, only if no same server exist
              int duplicatedIdx = serverList.indexOf(newServer);
              if (duplicatedIdx > -1) {
                Log.i(TAG, "duplicated server: " + duplicatedIdx);
                mSetting.setDomainIndex(String.valueOf(duplicatedIdx));
                mSetting.setCurrentServer(newServer);
                needToSave = false;
              }
              else {
                // no duplicated, add server
                Log.i(TAG, "no duplicated");
                serverList.add(newServer);
                // set current selected server to the new server
                mSetting.setDomainIndex(String.valueOf(serverList.size() - 1));
                mSetting.setCurrentServer(newServer);
                needToSave = true;
              }

            }

          }
          else {
            // same user, but password might change
            if ( !mSetting.getPassword().equals(mNewPassword) ) {
              Log.i(TAG, "password changes");
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

            /* start from Launch activity */
            if (mCurrentActivity instanceof LaunchActivity) {
              mCurrentActivity.overridePendingTransition(0, 0);
            }

            if (mSetting.isAutoLoginEnabled()) mCurrentActivity.finish();

          } else {
            dialogMsg = mobileNotCompilant;
            break;
          }

          /* login successfully */
          if (!mIsLaunchFromLaunchActivity) mProgressDialog.dismiss();
          return ;

        case ExoConnectionUtils.LOGIN_UNAUTHORIZED:
          dialogMsg = strUserNamePasswordFailed;
          break;

        case ExoConnectionUtils.LOGIN_INVALID:
          dialogMsg = strServerInvalid;
          break;

        case ExoConnectionUtils.LOGIN_FAILED:
          dialogMsg = strServerUnreachable;
          break;

        default:
          dialogMsg = strNetworkConnectionFailed;
          break;
      }

      showWarningDialog(dialogMsg, redirectToLogin, okString);
      if (!mIsLaunchFromLaunchActivity) mProgressDialog.dismiss();
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


  /**
   * A version of Warning dialog that redirect user to login screen if click on Ok
   */
  private class LoginWarningDialog extends WarningDialog {

    public LoginWarningDialog(Context context, String titleString, String contentString, String okString) {
      super(context, titleString, contentString, okString);
      getWindow().getAttributes().windowAnimations = R.style.Animations_Window;
    }

    @Override
    public void onClick(View view) {
      if (view.equals(okButton)) {
        dismiss();

        /* redirect to login screen */
        Intent next = new Intent(mCurrentActivity, LoginActivity.class);
        mContext.startActivity(next);
        mCurrentActivity.finish(); /* don't come back to Launch */
      }
    }
  }

}
