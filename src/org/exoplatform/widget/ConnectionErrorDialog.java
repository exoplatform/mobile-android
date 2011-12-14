package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.singleton.LocalizationHelper;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class ConnectionErrorDialog extends Dialog implements android.view.View.OnClickListener {
  private TextView titleView;

  private TextView contentView;

  private Button   okButton;

  public ConnectionErrorDialog(Context context) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.warning_dialog_layout);
    titleView = (TextView) findViewById(R.id.warning_dialog_title_view);
    String titleString = LocalizationHelper.getInstance().getString("Warning");
    titleView.setText(titleString);
    contentView = (TextView) findViewById(R.id.warning_content);
    String contentString = LocalizationHelper.getInstance().getString("ConnectionError");
    contentView.setText(contentString);
    okButton = (Button) findViewById(R.id.warning_ok_button);
    String okString = LocalizationHelper.getInstance().getString("OK");
    okButton.setText(okString);
    okButton.setOnClickListener(this);
  }

  public void onClick(View view) {
    if (view == okButton) {
      dismiss();
    }
  }

}
