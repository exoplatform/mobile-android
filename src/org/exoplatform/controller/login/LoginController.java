package org.exoplatform.controller.login;

import java.io.IOException;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.conn.HttpHostConnectException;
import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.image.FileCache;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;

public class LoginController {

  private String             userName;

  private String             password;

  private String             _strDomain;

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

  private WarningDialog      dialog;

  private LoginWaitingDialog _progressDialog;

  private Resources          resource;

  public LoginController(Context context, String user, String pass) {
    SettingUtils.setDefaultLanguage(context);
    mContext = context;
    resource = mContext.getResources();
    userName = user;
    password = pass;
    _strDomain = AccountSetting.getInstance().getDomainName();
    getLanguage();
    if (checkLogin() == true) {
      onLoad();
    } else {
      dialog = new WarningDialog(mContext, titleString, blankError, okString);
      dialog.show();
    }

  }

  private boolean checkLogin() {
    if (userName == null || userName.length() == 0) {
      blankError = resource.getString(R.string.NoUsernameEnter);
    } else if (password == null || password.length() == 0) {
      blankError = resource.getString(R.string.NoPasswordEnter);
    } else if (_strDomain == null || _strDomain.length() == 0) {
      blankError = resource.getString(R.string.NoServerSelected);
    } else
      return true;
    return false;
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
      _progressDialog = new LoginWaitingDialog(mContext, null, strSigning);
      _progressDialog.show();
    }

    @Override
    public Integer doInBackground(Void... params) {

      try {
        String versionUrl = SocialActivityUtil.getDomain() + ExoConstants.DOMAIN_PLATFORM_VERSION;
        response = ExoConnectionUtils.getPlatformResponse(userName, password, versionUrl);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND){
         return ExoConnectionUtils.LOGIN_INCOMPATIBLE;
        }
        isCompliant = ExoConnectionUtils.checkPLFVersion(response);
        ExoDocumentUtils.setRepositoryHomeUrl(userName, _strDomain);
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
        dialog = new WarningDialog(mContext, titleString, mobileNotCompilant, okString);
        dialog.show();
      } else if (result == ExoConnectionUtils.LOGIN_SUSCESS) {
        /*
         * Set social and document settings
         */
        StringBuilder builder = new StringBuilder(AccountSetting.getInstance().getDomainName());
        builder.append("_");
        builder.append(userName);
        builder.append("_");
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
        if (!isLastAccount(userName)) {
          editor.putBoolean(ExoConstants.SETTING_SOCIAL_FILTER, false);
          clearDownloadRepository();
        }
        editor.putString(ExoConstants.EXO_PRF_DOMAIN, AccountSetting.getInstance().getDomainName());
        editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, AccountSetting.getInstance().getDomainIndex());

        boolean isRememberMeEnabled = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0)
            .getBoolean(ExoConstants.SETTING_REMEMBER_ME, false);
        if (isRememberMeEnabled) {
          editor.putString(ExoConstants.EXO_PRF_USERNAME, userName);
          editor.putString(ExoConstants.EXO_PRF_PASSWORD, password);
        }
        editor.commit();
        AccountSetting.getInstance().setUsername(userName);
        AccountSetting.getInstance().setPassword(password);
        /*
         * Checking platform version
         */
        if (isCompliant == true) {
          Intent next = new Intent(mContext, HomeActivity.class);
          next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mContext.startActivity(next);
        } else {
          dialog = new WarningDialog(mContext, titleString, mobileNotCompilant, okString);
          dialog.show();
        }

      } else if (result == ExoConnectionUtils.LOGIN_UNAUTHORIZED) {
        dialog = new WarningDialog(mContext, titleString, strUserNamePasswordFailed, okString);
        dialog.show();
      } else if (result == ExoConnectionUtils.LOGIN_INVALID) {
        dialog = new WarningDialog(mContext, titleString, strServerInvalid, okString);
        dialog.show();
      } else if (result == ExoConnectionUtils.LOGIN_FAILED) {
        dialog = new WarningDialog(mContext, titleString, strServerUnreachable, okString);
        dialog.show();
      } else {
        dialog = new WarningDialog(mContext, titleString, strNetworkConnectionFailed, okString);
        dialog.show();
      }
      _progressDialog.dismiss();
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
