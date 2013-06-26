package org.exoplatform.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.widget.TextView;

import org.exoplatform.R;

public class SignUpActivity extends FragmentActivity {

  private static final String TAG = "eXoSignUpActivity";

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.signup);

    TextView eXo_txt = (TextView) findViewById(R.id.eXo_txt);
    eXo_txt.setText(Html.fromHtml("<font>eXo</font><br/>" + "<font>" + getResources().getString(R.string.SocialIntranet ) + "</font>"));

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