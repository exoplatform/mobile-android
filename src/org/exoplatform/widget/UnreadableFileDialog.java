package org.exoplatform.widget;

import org.exoplatform.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class UnreadableFileDialog extends Dialog implements android.view.View.OnClickListener {

  private Button   okButton;

  public UnreadableFileDialog(Context context) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.unreadable_file_dialog_layout);
    okButton = (Button) findViewById(R.id.unreadable_ok_button);
    okButton.setOnClickListener(this);
  }

  public void onClick(View view) {
    if (view.equals(okButton)) {
      dismiss();
    }
  }


}
