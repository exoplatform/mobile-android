package org.exoplatform.ui.login;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;

/**
 * Represents the account panel in login screen
 * that contains 2 edit input and a login button
 *
 */
public class AccountPanel extends LinearLayout implements View.OnClickListener {

  private Context  mContext;

  private Resources mResources;

  private AccountSetting mSetting;

  /**=== Components ===**/
  private EditText mUserEditTxt;

  private EditText mPassEditTxt;

  private Button   mLoginBtn;

  /**=== Constants ===**/
  public static final String USERNAME      = "USERNAME";

  public static final String PASSWORD      = "PASSWORD";

  private static final String TAG = "eXo____AccountPanel____";


  public AccountPanel(Context context) {
    super(context);
    mContext = context;
  }

  public AccountPanel(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    mSetting = AccountSetting.getInstance();
    mResources = mContext.getResources();

    initSubViews();
  }

  private void initSubViews() {
    mUserEditTxt  = (EditText) findViewById(R.id.EditText_UserName);
    mPassEditTxt  = (EditText) findViewById(R.id.EditText_Password);
    mUserEditTxt.addTextChangedListener(mOnEditCredentials);
    mPassEditTxt.addTextChangedListener(mOnEditCredentials);

    mLoginBtn     = (Button) findViewById(R.id.Button_Login);
    mLoginBtn.setOnClickListener(this);
  }


  /**
   * Make this panel visible
   */
  public void turnOn() {
    setVisibility(View.VISIBLE);

    if (mSetting.getCurrentServer() != null) {
      mUserEditTxt.setText(mSetting.isRememberMeEnabled()
          && !mSetting.getUsername().isEmpty() ?
          mSetting.getUsername(): mUserEditTxt.getText().toString());

      mPassEditTxt.setText(mSetting.isRememberMeEnabled()
          && !mSetting.getPassword().isEmpty() ?
          mSetting.getPassword(): mPassEditTxt.getText().toString());
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
    mUserEditTxt.setHint(mResources.getString(R.string.UserNameCellTitle));
    mPassEditTxt.setHint(mResources.getString(R.string.PasswordCellTitle));
    mLoginBtn.setText(mResources.getString(R.string.SignInButton));
  }


  /**
   * Listens for login click
   * Forward click event to controller
   *
   * @param view
   */
  @Override
  public void onClick(View view) {

    if (view.equals(mLoginBtn)) {
      mViewListener.onClickLogin(
          mUserEditTxt.getText().toString(),
          mPassEditTxt.getText().toString());
    }

  }

  private void changeStateOfLoginBtn() {
    boolean isCredentialsEntered = !mUserEditTxt.getText().toString().isEmpty()
        && !mPassEditTxt.getText().toString().isEmpty();
    mLoginBtn.setEnabled(isCredentialsEntered);
  }

  private TextWatcher mOnEditCredentials = new TextWatcher() {
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    public void afterTextChanged(Editable s) {}

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
