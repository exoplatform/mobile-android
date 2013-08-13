package org.exoplatform.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.exoplatform.controller.login.LaunchController;

/**
 * Lightweight activity acts as entry point to the application
 */
public class LaunchActivity extends Activity {

  private static final String TAG = "eXoLaunchActivity";

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LaunchController launchController = new LaunchController(this);
    launchController.redirect();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 0 && resultCode == RESULT_CANCELED) {
      /* back pressed from child activity */
      Intent startMain = new Intent(Intent.ACTION_MAIN);
      startMain.addCategory(Intent.CATEGORY_HOME);
      startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(startMain);
      finish();
    }
  }

}