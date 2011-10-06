package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.singleton.LocalizationHelper;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class UnreadableFileDialog extends Dialog implements android.view.View.OnClickListener {
  private TextView contentView;

  private Button   okButton;

  private String   titleString;

  private String   contentString;

  private String   okString;

  public UnreadableFileDialog(Context context) {
    super(context);
    setContentView(R.layout.unreadable_file_dialog_layout);
    changeLanguage();
    setTitle(titleString);
    contentView = (TextView) findViewById(R.id.unreadable_content);
    contentView.setText(contentString);
    okButton = (Button) findViewById(R.id.unreadable_ok_button);
    okButton.setText(okString);
    okButton.setOnClickListener(this);
  }

  public void onClick(View view) {
    if (view == okButton) {
      dismiss();
    }
  }

  private void changeLanguage() {
    LocalizationHelper local = LocalizationHelper.getInstance();
    titleString = local.getString("UnreachableFileTitle");
    contentString = local.getString("UnreachableFile");
    okString = local.getString("OK");

  }

}
