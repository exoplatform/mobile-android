package org.exoplatform.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import org.exoplatform.R;
import org.exoplatform.utils.AssetUtils;

public class SignUpActivity extends FragmentActivity {

  private static final String TAG = "eXoSignUpActivity";

  public void onCreate(Bundle savedInstanceState) {
    if (!WelcomeActivity.mIsTablet) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.signup);

    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.fragment_panel, new CreationAccountFragment(this))
        .commit();
  }

  public void flipToGreetingsPanel() {
    getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(
            R.anim.grow_from_middle, R.anim.shrink_to_middle)
        .replace(R.id.fragment_panel, new GreetingsFragment())
        .commit();
  }

}