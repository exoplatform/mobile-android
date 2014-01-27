package org.exoplatform.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

import org.exoplatform.poc.tabletversion.R;
import org.exoplatform.utils.LaunchUtils;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.ui.login.LoginProxy;
import org.exoplatform.ui.login.LoginWarningDialog;

/**
 * Lightweight activity acts as entry point to the application
 */
public class LaunchActivity extends ActionBarActivity implements LoginProxy.ProxyListener {

  private static final String TAG = "eXoLaunchActivity";

  private AccountSetting mSetting;

  private LoginProxy     mLoginProxy;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    new LaunchUtils(this);
    mSetting   = AccountSetting.getInstance();

    redirect();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.i(TAG, "onCreateOptionsMenu");
    getMenuInflater().inflate(R.menu.home, menu);
    //mOptionsMenu = menu;
    menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
    return true;
  }

  public void redirect() {
    /** no account configured - redirect to Welcome screen */
    if (mSetting.getCurrentServer() == null) {
      //requestWindowFeature(Window.FEATURE_NO_TITLE);
      Intent next = new Intent(this, WelcomeActivity.class);
      startActivityForResult(next, 0);
      overridePendingTransition(0, 0);
      return ;
    }

    /** performs login */
    if (mSetting.isAutoLoginEnabled()) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.launch);

      Bundle loginData = new Bundle();
      loginData.putString(LoginProxy.USERNAME, mSetting.getUsername());
      loginData.putString(LoginProxy.PASSWORD, mSetting.getPassword());
      loginData.putString(LoginProxy.DOMAIN,   mSetting.getDomainName());
      loginData.putBoolean(LoginProxy.SHOW_PROGRESS, false);
      mLoginProxy = new LoginProxy(this, LoginProxy.WITH_EXISTING_ACCOUNT, loginData);
      mLoginProxy.setListener(this);

      /** if some errors raise up, we'll redirect to login screen */
      mLoginProxy.getWarningDialog()
          .setTitle(getString(R.string.LoginWarningMsg))
          .setButtonText(getString(R.string.RedirectToLogin))
          .setViewListener(new LoginWarningDialog.ViewListener() {
            @Override
            public void onClickOk(LoginWarningDialog dialog) {
              /** redirect to login screen */
              Intent next = new Intent(LaunchActivity.this, LoginActivity.class);
              startActivity(next);
              /** don't come back to Launch */
              finish();
            }
          });

      mLoginProxy.performLogin();

      return ;
      //new LoginController(this, username, password);
    }


    /** redirect to login screen */
    Intent next = new Intent(this, LoginActivity.class);
    startActivityForResult(next, 0);
    overridePendingTransition(0, 0);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 0 && resultCode == RESULT_CANCELED) {
      /** back pressed from child activity */
      Intent startMain = new Intent(Intent.ACTION_MAIN);
      startMain.addCategory(Intent.CATEGORY_HOME);
      startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(startMain);
      finish();
    }
  }

  @Override
  public void onLoginFinished(boolean result) {
    if (!result) return ;

    /** Login ok, to Home screen!! */
    Intent next = new Intent(this, HomeActivity.class);
    //next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivityForResult(next, 0);
    overridePendingTransition(0, 0);
  }
}