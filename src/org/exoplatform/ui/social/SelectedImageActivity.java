package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;

import java.io.File;

import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.cyrilmottier.android.greendroid.R;

public class SelectedImageActivity extends MyActionBar implements OnClickListener {

  private static final int SCALE_WIDTH  = 1024;

  private static final int SCALE_HEIGHT = 860;

  private static final int SELECT_MODE  = 2;

  private static final int EDIT_MODE    = 1;

  private ImageView        imageView;

  private Button           okButton;

  private Button           removeButton;

  private String           filePath     = null;

  private File             file;

  private String           okText;

  private String           removeText;

  private String           cancelText;

  private int              modeId;

  private Uri              mImageUri;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.social_selected_image_layout);
    modeId = getIntent().getIntExtra(ExoConstants.SELECTED_IMAGE_MODE, 0);
    onChangeLanguage();
    init();
  }

  private void init() {
    if (modeId == SELECT_MODE) {
      mImageUri = getIntent().getData();
      filePath = PhotoUtils.extractFilenameFromUri(mImageUri, this);
    } else
      filePath = getIntent().getStringExtra(ExoConstants.SELECTED_IMAGE_EXTRA);
    
    okButton = (Button) findViewById(R.id.social_selected_image_ok_button);
    okButton.setText(okText);
    okButton.setOnClickListener(this);
    
    imageView = (ImageView) findViewById(R.id.social_selected_image);
    try {
      file = new File(filePath);
      setTitle(file.getName());
      Bitmap bitmap = PhotoUtils.shrinkBitmap(filePath, SCALE_WIDTH, SCALE_HEIGHT);
      imageView.setImageBitmap(bitmap);
    } catch (NullPointerException e) {
      setTitle("");
      okButton.setClickable(false);
    }

    removeButton = (Button) findViewById(R.id.social_selected_image_remove_button);
    if (modeId == EDIT_MODE) {
      removeButton.setText(removeText);
    } else {
      removeButton.setText(cancelText);
    }

    removeButton.setOnClickListener(this);

  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      finish();
      if (ComposeMessageActivity.composeMessageActivity != null)
        ComposeMessageActivity.composeMessageActivity.finish();
      if (SocialActivity.socialActivity != null)
        SocialActivity.socialActivity.finish();
      break;

    case 0:
      break;
    }
    return true;
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  public void onClick(View view) {
    if (view.equals(okButton)) {
      if (filePath != null) {
        if (DocumentActivity._documentActivityInstance != null) {
          DocumentActivity._documentActivityInstance._sdcard_temp_dir = filePath;
          DocumentActivity._documentActivityInstance.uploadFile();
        } else
          ComposeMessageActivity.addImageToMessage(file);
      }

    }

    if (view.equals(removeButton)) {
      if (modeId == EDIT_MODE) {
        ComposeMessageActivity.removeImageFromMessage();
      }
      if (modeId == SELECT_MODE) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ExoConstants.PHOTO_ALBUM_IMAGE_TYPE);
        if (DocumentActivity._documentActivityInstance != null) {
          DocumentActivity._documentActivityInstance.startActivityForResult(intent,
                                                                            ExoConstants.REQUEST_ADD_PHOTO);
        } else if (ComposeMessageActivity.composeMessageActivity != null) {
          ComposeMessageActivity.composeMessageActivity.startActivityForResult(intent,
                                                                               ExoConstants.REQUEST_ADD_PHOTO);
        }
      }
    }
    finish();

  }

  private void onChangeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    okText = bundle.getString("OK");
    cancelText = bundle.getString("Cancel");
    removeText = bundle.getString("Remove");

  }

}
