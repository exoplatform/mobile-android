package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.controller.social.ComposeMessageController;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.utils.ExoConstants;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class AddPhotoDialog extends Dialog implements android.view.View.OnClickListener {

  private Button                   takePhotoButton;

  private Button                   libraryButton;

  private Button                   cancelButton;

  private Activity                 mContext;

  private ComposeMessageController messageController;

  private DocumentActionDialog     fileActionDialog;

  public AddPhotoDialog(Activity context, DocumentActionDialog activity) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.add_photo_dialog_layout);
    mContext = context;
    fileActionDialog = activity;

    init();

  }

  public AddPhotoDialog(Activity context, ComposeMessageController controller) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.add_photo_dialog_layout);
    mContext = context;
    messageController = controller;

    init();
  }

  private void init() {

    takePhotoButton = (Button) findViewById(R.id.add_photo_take_button);
    takePhotoButton.setOnClickListener(this);
    libraryButton = (Button) findViewById(R.id.add_photo_library_button);
    libraryButton.setOnClickListener(this);
    cancelButton = (Button) findViewById(R.id.add_photo_cancel_button);
    cancelButton.setOnClickListener(this);

  }

  public void onClick(View view) {
    if (view.equals(takePhotoButton)) {
      dismiss();
      if (messageController == null)
        fileActionDialog._documentActionAdapter.initCamera();
      else
        messageController.initCamera();
    }
    // Start the native album photo
    if (view.equals(libraryButton)) {
      dismiss();
      Intent intent = new Intent(Intent.ACTION_PICK,
                                 android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      intent.setType(ExoConstants.PHOTO_ALBUM_IMAGE_TYPE);
      mContext.startActivityForResult(intent, ExoConstants.REQUEST_ADD_PHOTO);
    }
    if (view.equals(cancelButton)) {
      dismiss();
    }
  }

}
