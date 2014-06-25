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

import org.exoplatform.R;
import org.exoplatform.controller.signup.SignUpController;
import org.exoplatform.utils.ExoUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class CreationAccountFragment extends Fragment {

  private SignUpActivity mSignUpActivity;

  private Context  mContext;

  private TextView mAlertText;

  private EditText mEmailEditTxt;

  private Button   mCreateAccBtn;

  private static final String TAG = "CreationAccountFragment";

  public CreationAccountFragment() {
    
  }
  
  public CreationAccountFragment(SignUpActivity context) {
    mSignUpActivity = context;
    mContext = context;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.account_creation_panel, container, false);
    mAlertText = (TextView) layout.findViewById(R.id.signup_alert_txt);

    mEmailEditTxt = (EditText) layout.findViewById(R.id.signup_email_edit_txt);
    mEmailEditTxt.addTextChangedListener(createTextWatcher());
    mEmailEditTxt.setOnClickListener(onClickEditEmail());
    mCreateAccBtn = (Button) layout.findViewById(R.id.signup_create_acc_btn);
    mCreateAccBtn.setEnabled(false);
    return layout;
  }

  private View.OnClickListener onClickCreateAccBtn() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View createAccBtn) {
        Log.i(TAG, "createAccount");

        String email = mEmailEditTxt.getText().toString();
        if (!ExoUtils.isEmailValid(email)) showAlertMessage();
        else makeRequestCreatingAccount(email);
      }
    };
  }

  private View.OnClickListener onClickEditEmail() {

    return new View.OnClickListener() {
      @Override
      public void onClick(View mEmailEditTxt) {
        Log.i(TAG, "editEmailAddress");

        mAlertText.setVisibility(View.INVISIBLE);
        InputMethodManager mgr = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(mEmailEditTxt, InputMethodManager.HIDE_IMPLICIT_ONLY);
      }
    };
  }

  private TextWatcher createTextWatcher() {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        mAlertText.setVisibility(View.INVISIBLE);

        String email = mEmailEditTxt.getText().toString();
        if (email.isEmpty()) {
          mCreateAccBtn.setEnabled(false);
          return ;
        }

        mCreateAccBtn.setEnabled(true);
        mCreateAccBtn.setOnClickListener(onClickCreateAccBtn());
      }

      @Override
      public void afterTextChanged(Editable editable) { }
    };
  }


  private void showAlertMessage() {
    Log.i(TAG, "showAlertMessage");

    mAlertText.setVisibility(View.VISIBLE);
  }

  private void makeRequestCreatingAccount(String email) {
    Log.i(TAG, "makeRequestCreatingAccount");

    new SignUpController(mSignUpActivity, email);
  }
}
