package org.exoplatform.ui.login;

import org.exoplatform.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * A dialog that display a warning message with an animation <br/>
 */
public class LoginWarningDialog extends Dialog implements android.view.View.OnClickListener {

  private   TextView    mTitleTxt;

  private   TextView    mMessageTxt;

  protected Button      mBtn;

  private   int         mWindowsAnim = 0;

  public LoginWarningDialog(Context context) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.login_warning_dialog_layout);

    initSubViews();
  }

  public LoginWarningDialog(Context context, String titleString, String contentString, String okString) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.login_warning_dialog_layout);

    initSubViews();

    mTitleTxt.setText(titleString);
    mMessageTxt.setText(contentString);
    mBtn.setText(okString);
  }

  private void initSubViews() {
    mTitleTxt   = (TextView) findViewById(R.id.warning_dialog_title_txt);
    mMessageTxt = (TextView) findViewById(R.id.warning_dialog_message_txt);
    mBtn        = (Button)   findViewById(R.id.warning_dialog_btn);
    mBtn.setOnClickListener(this);
  }

  public LoginWarningDialog setTitle(String title) {
    mTitleTxt.setText(title);
    return this;
  }

  public LoginWarningDialog setMessage(String message) {
    mMessageTxt.setText(message);
    return this;
  }

  public LoginWarningDialog setButtonText(String text) {
    mBtn.setText(text);
    return this;
  }

  public LoginWarningDialog setWindowsAnimation(int anim) {
    mWindowsAnim = anim;
    return this;
  }

  @Override
  public void show() {
    getWindow().getAttributes().windowAnimations = mWindowsAnim != 0
        ? mWindowsAnim : R.style.Animations_Window;
    super.show();
  }

  public void onClick(View view) {
    if (view.equals(mBtn)) {
      dismiss();

      if (mViewListener != null) mViewListener.onClickOk(this);
    }
  }

  private ViewListener mViewListener;

  /* interface to listen to view event */
  public interface ViewListener {

    void onClickOk(LoginWarningDialog dialog);
  }

  public void setViewListener(ViewListener l) {
    mViewListener = l;
  }
}
