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
package org.exoplatform.ui.login;

import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Represents the account panel in login screen that contains 2 edit input and a
 * login button
 */
public class AccountPanel extends LinearLayout implements View.OnClickListener {

  private AccountSetting     mSetting;

  /** === Components === **/
  private EditText           mUserEditTxt;

  private EditText           mPassEditTxt;

  private Button             mLoginBtn;

  /** === Constants === **/
  public static final String USERNAME = "USERNAME";

  public static final String PASSWORD = "PASSWORD";

  public AccountPanel(Context context) {
    super(context);
  }

  public AccountPanel(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    mSetting = AccountSetting.getInstance();

    initSubViews();
  }

  private void initSubViews() {
    mUserEditTxt = (EditText) findViewById(R.id.EditText_UserName);
    mPassEditTxt = (EditText) findViewById(R.id.EditText_Password);
    mUserEditTxt.addTextChangedListener(mOnEditCredentials);
    mPassEditTxt.addTextChangedListener(mOnEditCredentials);

    mLoginBtn = (Button) findViewById(R.id.Button_Login);
    mLoginBtn.setOnClickListener(this);

    mPassEditTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
          mLoginBtn.performClick();
          return true;
        }
        return false;
      }
    });
  }

  /**
   * Make this panel visible
   */
  public void turnOn() {
    setVisibility(View.VISIBLE);

    if (mSetting.getCurrentAccount() != null) {
      // if a server is selected
      // 1) set username if remember me is enabled otherwise set nothing
      mUserEditTxt.setText(mSetting.isRememberMeEnabled() && !mSetting.getUsername().isEmpty() ? mSetting.getUsername() : "");
      // 2) set password if remember me is enabled otherwise set nothing
      mPassEditTxt.setText(mSetting.isRememberMeEnabled() && !mSetting.getPassword().isEmpty() ? mSetting.getPassword() : "");
      // 3) append the server name to the login button label
      String to = getResources().getString(R.string.SignInButton_In);
      String connect = getResources().getString(R.string.SignInButton);
      String loginButtonLabel = connect + " " + to + " " + mSetting.getServerName();
      mLoginBtn.setText(loginButtonLabel);
    }

    changeStateOfLoginBtn();
  }

  /**
   * Make this panel invisible
   */
  public void turnOff() {
    setVisibility(View.INVISIBLE);
  }

  public void onSaveState(Bundle saveState) {
    saveState.putString(USERNAME, mUserEditTxt.getText().toString());
    saveState.putString(PASSWORD, mPassEditTxt.getText().toString());
  }

  public void onRestoreState(Bundle saveState) {
    mUserEditTxt.setText(saveState.getString(USERNAME));
    mPassEditTxt.setText(saveState.getString(PASSWORD));
  }

  public void onChangeLanguage() {
    mUserEditTxt.setHint(getResources().getString(R.string.UserNameCellTitle));
    mPassEditTxt.setHint(getResources().getString(R.string.PasswordCellTitle));
    mLoginBtn.setText(getResources().getString(R.string.SignInButton));
  }

  /**
   * Listens for login click Forward click event to controller
   * 
   * @param view
   */
  @Override
  public void onClick(View view) {

    if (view.equals(mLoginBtn)) {
      mViewListener.onClickLogin(mUserEditTxt.getText().toString(), mPassEditTxt.getText().toString());
    }

  }

  private void changeStateOfLoginBtn() {
    boolean isCredentialsEntered = !mUserEditTxt.getText().toString().isEmpty() && !mPassEditTxt.getText().toString().isEmpty();
    mLoginBtn.setEnabled(isCredentialsEntered);
  }

  private TextWatcher  mOnEditCredentials = new TextWatcher() {
                                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                            }

                                            public void afterTextChanged(Editable s) {
                                            }

                                            @Override
                                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                              changeStateOfLoginBtn();
                                            }

                                          };

  private ViewListener mViewListener;

  /* interface to listen to view event */
  public interface ViewListener {

    void onClickLogin(String username, String password);
  }

  public void setViewListener(ViewListener l) {
    mViewListener = l;
  }
}
