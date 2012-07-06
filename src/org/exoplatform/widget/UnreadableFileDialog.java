package org.exoplatform.widget;

import org.exoplatform.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UnreadableFileDialog extends Dialog implements android.view.View.OnClickListener {

  private Button okButton;

  public UnreadableFileDialog(Context context, String content) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.warning_dialog_layout);
    TextView titleView = (TextView) findViewById(R.id.warning_dialog_title_view);
    titleView.setText(context.getResources().getString(R.string.UnreachableFileTitle));
    ImageView imageView = (ImageView) findViewById(R.id.warning_image);
    imageView.setImageResource(R.drawable.icon_for_unreadable_file);
    TextView contentView = (TextView) findViewById(R.id.warning_content);
    if (content == null) {
      contentView.setText(context.getResources().getString(R.string.UnreachableFile));
    } else
      contentView.setText(content);

    okButton = (Button) findViewById(R.id.warning_ok_button);
    okButton.setOnClickListener(this);
  }

  public void onClick(View view) {
    if (view.equals(okButton)) {
      dismiss();
    }
  }

}
