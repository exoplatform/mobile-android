package org.exoplatform.widget;

import org.exoplatform.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SocialDetailsWarningDialog extends Dialog implements android.view.View.OnClickListener {
  private TextView titleView;

  private TextView contentView;

  private Button   okButton;

  private boolean  hasContent;

  private Context  mContext;

  public SocialDetailsWarningDialog(Context context,
                                    String titleString,
                                    String contentString,
                                    String okString,
                                    boolean is) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.warning_dialog_layout);
    mContext = context;
    ImageView imageView = (ImageView) findViewById(R.id.warning_image);
    imageView.setImageResource(R.drawable.warning_icon);
    titleView = (TextView) findViewById(R.id.warning_dialog_title_view);
    titleView.setText(titleString);
    contentView = (TextView) findViewById(R.id.warning_content);
    contentView.setText(contentString);
    okButton = (Button) findViewById(R.id.warning_ok_button);
    hasContent = is;
    okButton.setText(okString);
    okButton.setOnClickListener(this);
  }

  public void onClick(View view) {
    if (view.equals(okButton)) {
      dismiss();
      if (hasContent == false) {
        ((Activity) mContext).finish();
      }
    }
  }
}
