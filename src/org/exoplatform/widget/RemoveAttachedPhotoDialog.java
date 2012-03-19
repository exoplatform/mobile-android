package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.ui.social.ComposeMessageActivity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class RemoveAttachedPhotoDialog extends Dialog implements android.view.View.OnClickListener {
  private Button removePhotoButton;

  public RemoveAttachedPhotoDialog(Context context) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.remove_photo_dialog_layout);
    setCanceledOnTouchOutside(true);
    removePhotoButton = (Button) findViewById(R.id.remove_photo_remove_button);
    removePhotoButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    if (view.equals(removePhotoButton)) {
      ComposeMessageActivity.removeImageFromMessage();
    }
    dismiss();
  }

}
