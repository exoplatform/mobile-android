package org.exoplatform.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.exoplatform.R;
import org.exoplatform.controller.signup.SignInController;
import org.exoplatform.utils.ExoConnectionUtils;

public class SignInOnPremiseActivity extends Activity {

  private Button   mLoginBtn;

  private EditText mUrlTxt;

  private EditText mUserTxt;

  private EditText mPassTxt;

  private TextView mAlertTxt;

  private static final String TAG = "eXoSignInOnPremiseActivity";

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.onpremise);

    mLoginBtn = (Button) findViewById(R.id.onpremise_login_btn);
    mLoginBtn.setEnabled(false);

    mUrlTxt   = (EditText) findViewById(R.id.onpremise_url_edit_txt);
    mUserTxt  = (EditText) findViewById(R.id.onpremise_user_edit_txt);
    mPassTxt  = (EditText) findViewById(R.id.onpremise_pass_edit_txt);

    mUrlTxt.addTextChangedListener(onAnyInputChanged());
    mUserTxt.addTextChangedListener(onAnyInputChanged());
    mPassTxt.addTextChangedListener(onAnyInputChanged());
  }


  public View.OnClickListener onClickLogIn() {

    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "click on log in");

        String url      = mUrlTxt.getText().toString();
        String user     = mUserTxt.getText().toString();
        String pass     = mPassTxt.getText().toString();

        makeRequestSigningIn(url, user, pass);
      }
    };
  }

  private void makeRequestSigningIn(String url, String user, String pass) {
    new SignInController(this, url, user, pass);
  }

  private TextWatcher onAnyInputChanged() {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        String url   = mUrlTxt.getText().toString();
        String user  = mUserTxt.getText().toString();
        String pass  = mPassTxt.getText().toString();

        /* check password and email is inputted */
        if (url.isEmpty() || user.isEmpty() || pass.isEmpty()) {
          mLoginBtn.setEnabled(false);
          return ;
        }

        mLoginBtn.setEnabled(true);
        mLoginBtn.setOnClickListener(onClickLogIn());
      }

      @Override
      public void afterTextChanged(Editable editable) { }
    };
  }
}