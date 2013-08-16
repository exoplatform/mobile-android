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
import java.util.ArrayList;

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

  private AccountSetting mSetting;

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

  private String serverResuming;

  private static final String TAG = "eXoSignInController";

  public SignInController(Context context, String email, String password) {
    mContext = context;
    SettingUtils.setDefaultLanguage(mContext);
    mResource = mContext.getResources();
    mSetting  = AccountSetting.getInstance();

    mEmail    = email;
    mPassword = password;

    getSignInMessages();
    onLoad();
  }

  public SignInController(Context context, String url, String user, String pass) {
    mContext = context;
    SettingUtils.setDefaultLanguage(mContext);
    mResource = mContext.getResources();
    mSetting  = AccountSetting.getInstance();

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
    serverResuming = mResource.getString(R.string.ServerResuming);
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

          mUsername = userAndTenant[0];
          mTenant   = userAndTenant[1];
          mDomain   = ExoConnectionUtils.HTTP + mTenant + "."
              + ExoConnectionUtils.EXO_CLOUD_WS_DOMAIN;

          int status = ExoConnectionUtils.requestTenantStatus(mTenant);
          Log.i(TAG, "status: " + status);
          if (status == ExoConnectionUtils.LOGIN_SERVER_RESUMING)
            return ExoConnectionUtils.LOGIN_SERVER_RESUMING;
          else if (status == ExoConnectionUtils.SIGNIN_SERVER_SUSPENDED) {
            /* stimulate a sign up request to force restoring server */
            ExoConnectionUtils.makeCloudSignUpRequest(mEmail);
            return ExoConnectionUtils.LOGIN_SERVER_RESUMING;
          }

          if (!ExoConnectionUtils.requestAccountExistsForUser(mUsername, mTenant))
            return ExoConnectionUtils.SIGNIN_NO_ACCOUNT;
        }

        mDomain = !mDomain.startsWith(ExoConnectionUtils.HTTP)
            ? ExoConnectionUtils.HTTP + mDomain: mDomain;

        String versionUrl = mDomain + ExoConstants.DOMAIN_PLATFORM_VERSION;
        if (!URLAnalyzer.isValidUrl(versionUrl)) return ExoConnectionUtils.LOGIN_INVALID;

        mResponse = ExoConnectionUtils.getPlatformResponse(mUsername, mPassword, versionUrl);
        if(mResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND){
          return ExoConnectionUtils.LOGIN_INCOMPATIBLE;
        }

        isCompliant = ExoConnectionUtils.checkPLFVersion(mResponse);
        ExoDocumentUtils.setRepositoryHomeUrl(mUsername, mDomain);
        return ExoConnectionUtils.checkPlatformRespose(mResponse);
      } catch(HttpHostConnectException e) {
        Log.i(TAG, "HttpHostConnectException");
        return ExoConnectionUtils.SIGNIN_SERVER_NAV;
      } catch (IOException e) {
        Log.i(TAG, "IOException");
        return ExoConnectionUtils.SIGNIN_SERVER_NAV;
      }
    }

    @Override
    public void onPostExecute(Integer result) {

      switch (result) {
        case ExoConnectionUtils.SIGNIN_NO_TENANT_FOR_EMAIL:
          mDialog = new WarningDialog(mContext, warningTitle, noTenantForEmail, okString);
          mDialog.show();
          break;

        case ExoConnectionUtils.SIGNIN_SERVER_NAV:
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

        case ExoConnectionUtils.LOGIN_SERVER_RESUMING:
          mDialog = new WarningDialog(mContext, warningTitle, serverResuming, okString);
          mDialog.show();
          break;

        case ExoConnectionUtils.LOGIN_SUSCESS:
          /* Set social and document settings */
          StringBuilder builder = new StringBuilder(mDomain)
              .append("_").append(mUsername).append("_");

          mSetting.socialKey      = builder.toString() + ExoConstants.SETTING_SOCIAL_FILTER;
          mSetting.socialKeyIndex = builder.toString() + ExoConstants.SETTING_SOCIAL_FILTER_INDEX;
          mSetting.documentKey    = builder.toString() + ExoConstants.SETTING_DOCUMENT_SHOW_HIDDEN_FILE;

          // save server to default server list
          ServerObjInfo serverObj  = new ServerObjInfo();
          serverObj.serverName     = mResource.getString(R.string.DefaultServer);
          serverObj.serverUrl      = mDomain;
          serverObj.username       = mUsername;
          serverObj.password       = mPassword;

          boolean needToSave = false;
          ArrayList<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList();
          int serverIdx = serverList.indexOf(serverObj);
          if (serverIdx == -1) {       // new server
            serverList.add(serverObj);
            needToSave = true;
            serverIdx  = serverList.size() - 1;
          }
          else { // might have new password
            if (!serverList.get(serverIdx).password.equals(mPassword)) {
              needToSave = true;
              serverList.get(serverIdx).password = mPassword;
            }
          }

          mSetting.setCurrentServer(serverList.get(serverIdx));
          mSetting.setDomainIndex(String.valueOf(serverIdx));

          if (needToSave) SettingUtils.persistServerSetting(mContext);

           /* Checking platform version */
          if (isCompliant) {
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
