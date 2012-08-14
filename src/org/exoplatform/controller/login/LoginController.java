package org.exoplatform.controller.login;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.SocialActivityUtil;
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
        isCompliant = ExoConnectionUtils.checkPLFVersion(response);
        return ExoConnectionUtils.checkPlatformRespose(response);
      } catch (IOException e) {
        return ExoConnectionUtils.LOGIN_WRONG;
      }

    }

    @Override
    public void onPostExecute(Integer result) {
      if (result == ExoConnectionUtils.LOGIN_WRONG) {
        dialog = new WarningDialog(mContext, titleString, strNetworkConnectionFailed, okString);
        dialog.show();
      } else if (result == ExoConnectionUtils.LOGIN_SUSCESS) {
        AccountSetting accountSetting = AccountSetting.getInstance();
        SharedPreferences.Editor editor = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE,
                                                                        0)
                                                  .edit();
        editor.putString(ExoConstants.EXO_PRF_DOMAIN, accountSetting.getDomainName());
        editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, accountSetting.getDomainIndex());
        editor.putString(ExoConstants.EXO_PRF_USERNAME, userName);
        editor.putString(ExoConstants.EXO_PRF_PASSWORD, password);
        editor.commit();
        accountSetting.setUsername(userName);
        accountSetting.setPassword(password);
        /*
         * Checking platform version
         */
        if (isCompliant == true) {
          ExoDocumentUtils.setRepositoryHomeUrl(userName, _strDomain);
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
