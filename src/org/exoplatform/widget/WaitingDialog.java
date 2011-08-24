package org.exoplatform.widget;

import org.exoplatform.R;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

public class WaitingDialog extends Dialog {
  private TextView contentView;

  public WaitingDialog(Context context, String titleString, String contentString) {
    super(context);
    if (titleString == null) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
    } else {
      setTitle(titleString);
    }
    setContentView(R.layout.waiting_dialog_layout);
    contentView = (TextView) findViewById(R.id.waiting_content);
    contentView.setText(contentString);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    dismiss();
  }

  public void setMessage(String message) {
    contentView.setText(message);
  }
}
