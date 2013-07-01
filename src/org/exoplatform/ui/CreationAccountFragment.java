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
import org.exoplatform.utils.ExoConnectionUtils;

public class CreationAccountFragment extends Fragment {

  private SignUpActivity mSignUpActivity;

  private Context mContext;

  private TextView mAlertText;

  private EditText mEmailEditTxt;

  private static final String TAG = "CreationAccountFragment";

  public CreationAccountFragment(SignUpActivity context) {
    mSignUpActivity = context;
    mContext = (Context) context;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.account_creation_panel, container, false);
    mAlertText = (TextView) layout.findViewById(R.id.signup_alert_txt);

    mEmailEditTxt = (EditText) layout.findViewById(R.id.signup_email_edit_txt);
    mEmailEditTxt.addTextChangedListener(createTextWatcher());
    mEmailEditTxt.setOnClickListener(onClickEditEmail());
    Button createAccBtn = (Button) layout.findViewById(R.id.signup_create_acc_btn);
    createAccBtn.setOnClickListener(onClickCreateAccBtn());
    return layout;
  }

  private View.OnClickListener onClickCreateAccBtn() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View createAccBtn) {
        Log.i(TAG, "createAccount");

        String email = mEmailEditTxt.getText().toString();
        if (!ExoConnectionUtils.validateEmail(email)) showAlertMessage();
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
