package org.exoplatform.ui;

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
import org.exoplatform.R;
import org.exoplatform.controller.signup.SignUpController;

public class CreationAccountFragment extends Fragment {

  private SignUpActivity mSignUpActivity;

  private Context mContext;

  private TextView mAlertText;

  private EditText edit_txt_email;

  private static final String TAG = "CreationAccountFragment";

  public CreationAccountFragment(SignUpActivity context) {
    mSignUpActivity = context;
    mContext = (Context) context;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.account_creation_panel, container, false);
    mAlertText = (TextView) layout.findViewById(R.id.alert_txt);

    edit_txt_email = (EditText) layout.findViewById(R.id.edit_txt_email);
    edit_txt_email.addTextChangedListener(createTextWatcher());
    edit_txt_email.setOnClickListener(onClickEditEmail());
    Button create_acc_btn = (Button) layout.findViewById(R.id.create_acc_btn);
    create_acc_btn.setOnClickListener(onClickCreateAccBtn());
    return layout;
  }

  private View.OnClickListener onClickCreateAccBtn() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View create_acc_btn) {
        Log.i(TAG, "createAccount");

        String email = edit_txt_email.getText().toString();
        if (!validateEmail(email)) showAlertMessage();
        else makeRequestCreatingAccount(email);
      }
    };
  }

  private View.OnClickListener onClickEditEmail() {

    return new View.OnClickListener() {
      @Override
      public void onClick(View edit_txt_email) {
        Log.i(TAG, "editEmailAddress");

        mAlertText.setVisibility(View.INVISIBLE);
        InputMethodManager mgr = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(edit_txt_email, InputMethodManager.HIDE_IMPLICIT_ONLY);
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

    // prepare the request to cloud workspace WS, send it then
    // display the loader

    // check server not available
    // account already exists for this email
    new SignUpController(mSignUpActivity, email);
  }

  private boolean validateEmail(String aEmailAddress) {
    if (aEmailAddress == null) return false;
    boolean result = true;
    if (!hasNameAndDomain(aEmailAddress)) {
      result = false;
    }
    return result;
  }

  private boolean hasNameAndDomain(String aEmailAddress) {
    String[] tokens = aEmailAddress.split("@");
    return tokens.length == 2 && tokens[0].trim().length() > 0 && tokens[1].trim().length() > 0
        && tokens[1].split("\\.").length > 1;
  }

}
