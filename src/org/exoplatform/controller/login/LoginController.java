package org.exoplatform.controller.login;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class LoginController {
  private String    userName;

  private String    password;

  private Context   mContext;

  private LoginTask mLoadTask;

  private String    strSigning;

  private String    strNetworkConnectionFailed;

  private String    strUserNamePasswordFailed;

  private String    titleString;

  private String    okString;

  private URI       uri;

  public LoginController(Context context, String user, String pass) {
    mContext = context;
    userName = user;
    password = pass;
    getLanguage();
    onLoad();
  }

  private void onLoad() {
    if (mLoadTask == null || mLoadTask.getStatus() == LoginTask.Status.FINISHED) {
      mLoadTask = (LoginTask) new LoginTask().execute();
    }
  }

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == LoginTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  private void getLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    strSigning = bundle.getString("SigningIn");
    strNetworkConnectionFailed = bundle.getString("NetworkConnectionFailed");
    strUserNamePasswordFailed = bundle.getString("UserNamePasswordFailed");
    titleString = bundle.getString("Warning");
    okString = bundle.getString("OK");

  }

  private void createAuthorization(String url, int port, String userName, String password) {
    AuthScope auth = new AuthScope(url, port);
    AccountSetting.getInstance().setAuthScope(auth);
    UsernamePasswordCredentials credential = new UsernamePasswordCredentials(userName, password);
    AccountSetting.getInstance().setCredentials(credential);
  }

  private class LoginTask extends UserTask<Void, Void, String> {
    private WaitingDialog _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = new WaitingDialog(mContext, null, strSigning);
      _progressDialog.show();
    }

    @Override
    public String doInBackground(Void... params) {

      try {
        String _strDomain = AccountSetting.getInstance().getDomainName();
        if (_strDomain.indexOf("http://") == -1) {
          _strDomain = "http://" + _strDomain;
        }
        uri = new URI(_strDomain);
        return ExoConnectionUtils.sendAuthentication(_strDomain, userName, password);
      } catch (URISyntaxException e) {
        return null;
      }

    }

    @Override
    public void onPostExecute(String result) {
      if (result == null) {
        WarningDialog dialog = new WarningDialog(mContext,
                                                 titleString,
                                                 strNetworkConnectionFailed,
                                                 okString);
        dialog.show();
      } else if (result.equalsIgnoreCase("YES")) {
        AccountSetting accountSetting = AccountSetting.getInstance();
        createAuthorization(uri.getHost(), uri.getPort(), userName, password);
        SharedPreferences.Editor editor = LocalizationHelper.getInstance().getSharePrefs().edit();
        editor.putString(ExoConstants.EXO_PRF_DOMAIN, accountSetting.getDomainName());
        editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, accountSetting.getDomainIndex());
        editor.putString(ExoConstants.EXO_PRF_USERNAME, userName);
        editor.putString(ExoConstants.EXO_PRF_PASSWORD, password);
        editor.commit();
        accountSetting.setUsername(userName);
        accountSetting.setPassword(password);
        boolean isNewVersion = ExoConnectionUtils.checkPLFVersion();
        accountSetting.setIsNewVersion(isNewVersion);
        System.out.println("isNewVersion" + isNewVersion);

        Intent next = new Intent(mContext, HomeActivity.class);

        mContext.startActivity(next);
        ((Activity) mContext).finish();

      } else if (result.equalsIgnoreCase("NO")) {
        WarningDialog dialog = new WarningDialog(mContext,
                                                 titleString,
                                                 strUserNamePasswordFailed,
                                                 okString);
        dialog.show();
      } else {
        WarningDialog dialog = new WarningDialog(mContext,
                                                 titleString,
                                                 strNetworkConnectionFailed,
                                                 okString);
        dialog.show();
      }
      _progressDialog.dismiss();
    }

  }

}
