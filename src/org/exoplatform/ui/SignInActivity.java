package org.exoplatform.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.exoplatform.R;
import org.exoplatform.controller.signup.SignInController;
import org.exoplatform.utils.ExoConnectionUtils;

public class SignInActivity extends Activity {

  private Button   mLoginBtn;

  private EditText mEmailTxt;

  private EditText mPassTxt;

  private TextView mAlertTxt;

  private static final String TAG = "eXoSignInActivity";

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.signin);

    mLoginBtn = (Button) findViewById(R.id.signin_login_btn);
    mLoginBtn.setEnabled(false);

    mEmailTxt = (EditText) findViewById(R.id.signin_edit_txt_email);
    TextWatcher _onEmailOrPasswordChanged = onEmailOrPasswordChanged();
    mEmailTxt.addTextChangedListener(_onEmailOrPasswordChanged);
    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    mgr.showSoftInput(mEmailTxt, InputMethodManager.HIDE_IMPLICIT_ONLY);

    mPassTxt = (EditText) findViewById(R.id.signin_edit_txt_pass);
    mPassTxt.addTextChangedListener(_onEmailOrPasswordChanged);

    mAlertTxt = (TextView) findViewById(R.id.signin_alert_txt);
  }

  public View.OnClickListener onClickLogIn() {

    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
      Log.i(TAG, "click on log in");

        mAlertTxt.setVisibility(View.INVISIBLE);
        String email    = mEmailTxt.getText().toString();
        String password = mPassTxt.getText().toString();
        if (!ExoConnectionUtils.validateEmail(email)) showAlertMessage();
        else makeRequestSigningIn(email, password);
      }
    };
  }


  private void showAlertMessage() {
    mAlertTxt.setVisibility(View.VISIBLE);
  }

  private void makeRequestSigningIn(String email, String password) {
    Log.i(TAG, "makeRequestSigningIn");

    new SignInController(this, email, password);
  }


  private TextWatcher onEmailOrPasswordChanged() {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        String email = mEmailTxt.getText().toString();
        String pass  = mPassTxt.getText().toString();

        /* check password and email is inputted */
        if ((email != null) && (pass != null)) {
          if ((!email.isEmpty()) && (!pass.isEmpty())) {
            mLoginBtn.setEnabled(true);
            mLoginBtn.setOnClickListener(onClickLogIn());
            return;
          }
        }

        mLoginBtn.setEnabled(false);
      }

      @Override
      public void afterTextChanged(Editable editable) { }
    };
  }


  public void connectToOnPremise(View connectOnPremiseBtn) {
    Log.i(TAG, "connectToOnPremise");

    Intent next = new Intent(this, SignInOnPremiseActivity.class);
    startActivity(next);
  }

}