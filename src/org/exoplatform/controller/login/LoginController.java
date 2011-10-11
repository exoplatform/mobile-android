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
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

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

  private String    invalidServerName;

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
    strNetworkConnectionFailed = bundle.getString("ConnectionError");
    strUserNamePasswordFailed = bundle.getString("UserNamePasswordFailed");
    invalidServerName = bundle.getString("InvalidServerName");
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
    private LoginWaitingDialog _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = new LoginWaitingDialog(mContext, null, strSigning);
      _progressDialog.show();
    }

    @Override
    public String doInBackground(Void... params) {

      try {
        String _strDomain = AccountSetting.getInstance().getDomainName();
        uri = new URI(_strDomain);
        String resultStr = ExoConnectionUtils.sendAuthentication(_strDomain, userName, password);
        return resultStr;

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
        
        ExoDocumentUtils.setRepositoryHomeUrl(userName, accountSetting.getDomainName());
        
        Intent next = new Intent(mContext, HomeActivity.class);
        mContext.startActivity(next);

      } else if (result.equalsIgnoreCase("NO")) {
        WarningDialog dialog = new WarningDialog(mContext,
                                                 titleString,
                                                 strUserNamePasswordFailed,
                                                 okString);
        dialog.show();
      } else if (result.equalsIgnoreCase("URL_ERROR")) {
        WarningDialog dialog = new WarningDialog(mContext, titleString, invalidServerName, okString);
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
