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

  private LocalizationHelper bundle;

  private String             userName;

  private String             password;

  private String             _strDomain;

  private Context            mContext;

  private LoginTask          mLoadTask;

  private String             strSigning;

  private String             blankError;

  private String             strNetworkConnectionFailed;

  private String             strServerFailed;

  private String             strUserNamePasswordFailed;

  private String             mobileNotCompilant;

  private String             titleString;

  private String             okString;

  private URI                uri;

  private WarningDialog      dialog;

  private LoginWaitingDialog _progressDialog;

  public LoginController(Context context, String user, String pass) {
    mContext = context;
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
      blankError = bundle.getString("NoUsernameEnter");
    } else if (password == null || password.length() == 0) {
      blankError = bundle.getString("NoPasswordEnter");
    } else if (_strDomain == null || _strDomain.length() == 0) {
      blankError = bundle.getString("NoServerSelected");
    } else
      return true;
    return false;
  }

  private void onLoad() {
    if (mLoadTask == null || mLoadTask.getStatus() == LoginTask.Status.FINISHED) {
      mLoadTask = (LoginTask) new LoginTask().execute();
    }
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == LoginTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  private void getLanguage() {
    bundle = LocalizationHelper.getInstance();
    strSigning = bundle.getString("SigningIn");
    strNetworkConnectionFailed = bundle.getString("ConnectionError");
    strUserNamePasswordFailed = bundle.getString("UserNamePasswordFailed");
    mobileNotCompilant = bundle.getString("CompliantMessage");
    titleString = bundle.getString("Warning");
    okString = bundle.getString("OK");

  }

  private void createAuthorization(String url, int port, String userName, String password) {
    AuthScope auth = new AuthScope(url, port);
    AccountSetting.getInstance().setAuthScope(auth);
    UsernamePasswordCredentials credential = new UsernamePasswordCredentials(userName, password);
    AccountSetting.getInstance().setCredentials(credential);
  }

  public class LoginTask extends UserTask<Void, Void, String> {
    @Override
    public void onPreExecute() {
      _progressDialog = new LoginWaitingDialog(mContext, null, strSigning);
      _progressDialog.show();
    }

    @Override
    public String doInBackground(Void... params) {

      try {

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
        dialog = new WarningDialog(mContext, titleString, strNetworkConnectionFailed, okString);
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
        ExoDocumentUtils.setRepositoryHomeUrl(userName, _strDomain);

        boolean isCompliant = ExoConnectionUtils.checkPLFVersion();
        if (isCompliant == true) {
          Intent next = new Intent(mContext, HomeActivity.class);
          mContext.startActivity(next);
        } else {
          dialog = new WarningDialog(mContext, titleString, mobileNotCompilant, okString);
          dialog.show();
        }

      } else if (result.equalsIgnoreCase("NO")) {
        dialog = new WarningDialog(mContext, titleString, strUserNamePasswordFailed, okString);
        dialog.show();
      } else if (result.contains("confused")) {
        dialog = new WarningDialog(mContext, titleString, result, okString);
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
