package org.exoplatform.widget;

import org.exoplatform.R;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ConnectionErrorDialog extends Dialog implements android.view.View.OnClickListener {
  private TextView titleView;

  private TextView contentView;

  private Button   okButton;

  public ConnectionErrorDialog(Context context) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.warning_dialog_layout);
    Resources res = context.getResources();
    titleView = (TextView) findViewById(R.id.warning_dialog_title_view);
    String titleString = res.getString(R.string.Warning);
    titleView.setText(titleString);
    ImageView imageView = (ImageView) findViewById(R.id.warning_image);
    imageView.setImageResource(R.drawable.warning_icon);
    contentView = (TextView) findViewById(R.id.warning_content);
    String contentString = res.getString(R.string.ConnectionError);
    contentView.setText(contentString);
    okButton = (Button) findViewById(R.id.warning_ok_button);
    String okString = res.getString(R.string.OK);
    okButton.setText(okString);
    okButton.setOnClickListener(this);
  }

  public void onClick(View view) {
    if (view.equals(okButton)) {
      dismiss();
    }
  }

}
