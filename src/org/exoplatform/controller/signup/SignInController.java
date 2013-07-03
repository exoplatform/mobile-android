package org.exoplatform.controller.signup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.conn.HttpHostConnectException;
import org.exoplatform.R;
import org.exoplatform.controller.login.LaunchController;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.ui.SignInActivity;
import org.exoplatform.utils.*;
import org.exoplatform.utils.image.FileCache;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import java.io.IOException;

public class SignInController {

  private Context      mContext;

  private SignInWaitingDialog mProgressDialog;

  private Resources    mResource;

  private String       mEmail;

  private String       mPassword;

  private String       mUsername;

  private String       mTenant;

  private HttpResponse mResponse;

  private String       mDomain;

  private SignInTask   mSignInTask;

  private SharedPreferences mSharedPreference;

  private WarningDialog mDialog;

  /* Sign In messages */
  private String signInMess;

  private String warningTitle;

  private String okString;

  private String strNetworkConnectionFailed;

  private String strUserNamePasswordFailed;

  private String mobileNotCompilant;

  private String strServerInvalid;

  private String strServerUnreachable;

  private String noTenantForEmail;

  private String serverNotAvailable;

  private String noAccountForEmail;

  private String invalidCredentials;

  private static final String TAG = "eXoSignInController";


  public SignInController(Context context, String email, String password) {
    mContext = context;
    SettingUtils.setDefaultLanguage(mContext);
    mResource = mContext.getResources();

    mEmail    = email;
    mPassword = password;

    getSignInMessages();
    onLoad();
  }

  /* signing in on premise installation */
  public SignInController(Context context, String url, String user, String pass) {
    mContext = context;
    SettingUtils.setDefaultLanguage(mContext);
    mResource = mContext.getResources();

    mDomain   = url;
    mUsername = user;
    mPassword = pass;

    getSignInMessages();
    onLoad();
  }

  private void getSignInMessages() {
    signInMess         = mResource.getString(R.string.SigningIn);
    warningTitle       = mResource.getString(R.string.Warning);
    noTenantForEmail   = mResource.getString(R.string.NoTenantForEmail);
    serverNotAvailable = mResource.getString(R.string.ServerNotAvailable);
    noAccountForEmail  = mResource.getString(R.string.NoAccountExists);
    invalidCredentials = mResource.getString(R.string.InvalidCredentials);

    strNetworkConnectionFailed = mResource.getString(R.string.ConnectionError);
    strUserNamePasswordFailed = mResource.getString(R.string.UserNamePasswordFailed);
    mobileNotCompilant = mResource.getString(R.string.CompliantMessage);
    strServerInvalid = mResource.getString(R.string.ServerInvalid);
    strServerUnreachable = mResource.getString(R.string.ServerUnreachable);
    okString = mResource.getString(R.string.OK);
  }

  private void onLoad() {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mSignInTask == null || mSignInTask.getStatus() == SignInTask.Status.FINISHED) {
        mSignInTask = (SignInTask) new SignInTask().execute();
      }
    } else {
      new ConnectionErrorDialog(mContext).show();
    }
  }

  private boolean isLastAccount(String username) {
    if (username.equals(AccountSetting.getInstance().getUsername())) {
      return true;
    }

    return false;
  }

  private void clearDownloadRepository() {
    FileCache filecache = new FileCache(mContext, ExoConstants.DOCUMENT_FILE_CACHE);
    filecache.clear();
  }


  public class SignInTask extends AsyncTask<Void, Void, Integer> {

    private boolean      isCompliant;

    @Override
    public void onPreExecute() {
      mProgressDialog = new SignInWaitingDialog(mContext, null, signInMess);
      mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
      try {
        /* log in by email */
        if ((mUsername == null) && (mEmail != null)) {
          mResponse = ExoConnectionUtils.requestTenantForEmail(mEmail);
          String[] userAndTenant    = ExoConnectionUtils.checkRequestTenant(mResponse);
          if (userAndTenant == null) return ExoConnectionUtils.SIGNIN_NO_TENANT_FOR_EMAIL;

          mUsername   = userAndTenant[0];
          mTenant     = userAndTenant[1];
          mDomain     = "http://" + mTenant + ".cloud-workspaces.com";

          if (!ExoConnectionUtils.requestAccountExistsForUser(mUsername, mTenant))
            return ExoConnectionUtils.SIGNIN_NO_ACCOUNT;
        }

        String versionUrl = mDomain + ExoConstants.DOMAIN_PLATFORM_VERSION;
        Log.i(TAG, "pass: " + mPassword);
        Log.i(TAG, "user: " + mUsername);
        mResponse = ExoConnectionUtils.getPlatformResponse(mUsername, mPassword, versionUrl);
        if(mResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND){
          return ExoConnectionUtils.LOGIN_INCOMPATIBLE;
        }

        Log.i(TAG, "checkPLFVersion");
        isCompliant = ExoConnectionUtils.checkPLFVersion(mResponse);
        ExoDocumentUtils.setRepositoryHomeUrl(mUsername, mDomain);
        Log.i(TAG, "checkPlatformResponse");

        return ExoConnectionUtils.checkPlatformRespose(mResponse);
      } catch(HttpHostConnectException e) {
        return ExoConnectionUtils.SIGNIN_SERVER_NOT_AVAILABLE;
      } catch (IOException e) {
        return ExoConnectionUtils.LOGIN_INCOMPATIBLE;
      }
    }

    @Override
    public void onPostExecute(Integer result) {

      switch (result) {
        case ExoConnectionUtils.SIGNIN_NO_TENANT_FOR_EMAIL:
          mDialog = new WarningDialog(mContext, warningTitle, noTenantForEmail, okString);
          mDialog.show();
          break;

        case ExoConnectionUtils.SIGNIN_SERVER_NOT_AVAILABLE:
          mDialog = new WarningDialog(mContext, warningTitle, serverNotAvailable, okString);
          mDialog.show();
          break;

        case ExoConnectionUtils.SIGNIN_NO_ACCOUNT:
          mDialog = new WarningDialog(mContext, warningTitle, noAccountForEmail, okString);
          mDialog.show();
          break;

        case ExoConnectionUtils.LOGIN_INCOMPATIBLE:
          mDialog = new WarningDialog(mContext, warningTitle, mobileNotCompilant, okString);
          mDialog.show();
          break;

        case ExoConnectionUtils.LOGIN_SUSCESS:
          /* Set social and document settings */
          StringBuilder builder = new StringBuilder(mDomain)
              .append("_").append(mUsername).append("_");

          // save account settings
          AccountSetting.getInstance().setUsername(mUsername);
          AccountSetting.getInstance().setPassword(mPassword);
          AccountSetting.getInstance().setDomainName(mDomain);

          // save server to default server list
          ServerObjInfo serverObj  = new ServerObjInfo();
          //serverObj._bSystemServer = false;
          serverObj._strServerName = mResource.getString(R.string.DefaultServer);
          // (mTenant != null) ? mTenant : Uri.parse(mDomain).getAuthority();
          serverObj._strServerUrl  = mDomain;
          serverObj.username       = mUsername;
          serverObj.password       = mPassword;

          ServerSettingHelper settingHelper = ServerSettingHelper.getInstance();
          settingHelper.getServerInfoList().add(serverObj);
          ServerConfigurationUtils.generateXmlFileWithServerList(mContext, settingHelper.getServerInfoList(),
              ExoConstants.EXO_SERVER_SETTING_FILE, "");
          // set current selected server to the new server
          AccountSetting.getInstance().setDomainIndex(String.valueOf(settingHelper.getServerInfoList().size() -1));

          AccountSetting.getInstance().socialKey = builder.toString()
            + ExoConstants.SETTING_SOCIAL_FILTER;
          AccountSetting.getInstance().socialKeyIndex = builder.toString()
            + ExoConstants.SETTING_SOCIAL_FILTER_INDEX;
          AccountSetting.getInstance().documentKey = builder.toString()
            + ExoConstants.SETTING_DOCUMENT_SHOW_HIDDEN_FILE;

          SharedPreferences.Editor editor = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0)
            .edit();

          /*
           * disable saving social filter when login with difference account and
           * clear the download repository
           */
          if (!isLastAccount(mUsername)) {
            editor.putBoolean(ExoConstants.SETTING_SOCIAL_FILTER, false);
            clearDownloadRepository();
          }
          editor.putString(ExoConstants.EXO_PRF_DOMAIN, AccountSetting.getInstance().getDomainName());
          editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, AccountSetting.getInstance().getDomainIndex());
          editor.putString(ExoConstants.EXO_PRF_USERNAME, mUsername);
          editor.putString(ExoConstants.EXO_PRF_PASSWORD, mPassword);
          editor.commit();

           /* Checking platform version */
          if (isCompliant == true) {
            Intent next = new Intent(mContext, HomeActivity.class);
            next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(next);
          } else {
            mDialog = new WarningDialog(mContext, warningTitle, mobileNotCompilant, okString);
            mDialog.show();
          }

          break;

        case ExoConnectionUtils.LOGIN_UNAUTHORIZED:
          /* wrong password */
          mDialog = new WarningDialog(mContext, warningTitle, invalidCredentials, okString);
          mDialog.show();
          break;

        case ExoConnectionUtils.LOGIN_INVALID:
          mDialog = new WarningDialog(mContext, warningTitle, strServerInvalid, okString);
          mDialog.show();
          break;

        case ExoConnectionUtils.LOGIN_FAILED:
          mDialog = new WarningDialog(mContext, warningTitle, strServerUnreachable, okString);
          mDialog.show();
          break;

        default:
          mDialog = new WarningDialog(mContext, warningTitle, strNetworkConnectionFailed, okString);
          mDialog.show();
          break;
      }

      mProgressDialog.dismiss();
    }
  }

  public class SignInWaitingDialog extends WaitingDialog {

    public SignInWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      //onCancelLoad();
    }

  }

}