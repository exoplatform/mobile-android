/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
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
import org.exoplatform.ui.login.LoginProxy;
import org.exoplatform.utils.AssetUtils;
import org.exoplatform.utils.ExoUtils;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;

public class SignInActivity extends Activity implements LoginProxy.ProxyListener {

  private Button   mLoginBtn;

  private EditText mEmailTxt;

  private EditText mPassTxt;

  private TextView mAlertTxt;

  private LoginProxy mLoginProxy;

  private static final String TAG = "eXoSignInActivity";

  public void onCreate(Bundle savedInstanceState) {
    if (!WelcomeActivity.mIsTablet) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.signin);

    mLoginBtn = (Button) findViewById(R.id.signin_login_btn);
    mLoginBtn.setEnabled(false);

    mEmailTxt = (EditText) findViewById(R.id.signin_edit_txt_email);
    String email = getIntent().getStringExtra(ExoConstants.EXO_EMAIL);
    mEmailTxt.setText(email == null? "": email);
    TextWatcher _onEmailOrPasswordChanged = onEmailOrPasswordChanged();
    mEmailTxt.addTextChangedListener(_onEmailOrPasswordChanged);
    AssetUtils.setContext(this);
    Typeface type = AssetUtils.getCustomTypeface(AssetUtils.ROBOTO_REGULAR);
    if (type != null) AssetUtils.setTypeFace(type, mEmailTxt);
    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    mgr.showSoftInput(mEmailTxt, InputMethodManager.HIDE_IMPLICIT_ONLY);

    mPassTxt = (EditText) findViewById(R.id.signin_edit_txt_pass);
    mPassTxt.addTextChangedListener(_onEmailOrPasswordChanged);
    if (type != null) AssetUtils.setTypeFace(type, mPassTxt);

    mAlertTxt = (TextView) findViewById(R.id.signin_alert_txt);
    if (type != null) AssetUtils.setTypeFace(type, mAlertTxt);
  }

  @Override
  protected void onResume() {
    super.onResume();
    SettingUtils.setDefaultLanguage(this);
    onChangeLanguage();
  }


  private void onChangeLanguage() {
    Resources resources = getResources();
    ((TextView) findViewById(R.id.signin_title_txt)).setText(resources.getString(R.string.GetStarted));
    ((TextView) findViewById(R.id.signin_email_txt)).setText(resources.getString(R.string.EmailSignIn));
    ((TextView) findViewById(R.id.signin_alert_txt)).setText(resources.getString(R.string.InvalidEmail));
    ((EditText) findViewById(R.id.signin_edit_txt_email)).setHint(resources.getString(R.string.Email));
    ((EditText) findViewById(R.id.signin_edit_txt_pass)).setHint(resources.getString(R.string.PasswordHint));
    ((TextView) findViewById(R.id.signin_or_txt)).setText(resources.getString(R.string.Or));
    ((Button) findViewById(R.id.signin_login_btn)).setText(resources.getString(R.string.LogIn));
    ((Button) findViewById(R.id.signin_connect_on_premises_btn)).setText(resources.getString(R.string.ConnectOnPremise));
  }

  public View.OnClickListener onClickLogIn() {

    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
      Log.i(TAG, "click on log in");

        mAlertTxt.setVisibility(View.INVISIBLE);
        String email    = mEmailTxt.getText().toString();
        String password = mPassTxt.getText().toString();
        if (!ExoUtils.isEmailValid(email)) showAlertMessage();
        else makeRequestSigningIn(email, password);
      }
    };
  }


  private void showAlertMessage() {
    mAlertTxt.setVisibility(View.VISIBLE);
  }

  private void makeRequestSigningIn(String email, String password) {
    Log.i(TAG, "makeRequestSigningIn");

    //new SignInController(this, email, password);

    Bundle loginData = new Bundle();
    loginData.putString(LoginProxy.EMAIL, email);
    loginData.putString(LoginProxy.PASSWORD, password);
    mLoginProxy = new LoginProxy(this, LoginProxy.WITH_EMAIL, loginData);
    mLoginProxy.setListener(this);
    //mLoginProxy.performLogin();  // do not call perform login when logging in by email
  }


  @Override
  public void onLoginFinished(boolean result) {
    if (!result) return ;
    Intent next = new Intent(this, HomeActivity.class);
    //next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(next);
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