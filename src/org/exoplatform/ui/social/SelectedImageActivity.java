package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;

import java.io.File;

import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.widget.MyActionBar;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.cyrilmottier.android.greendroid.R;

public class SelectedImageActivity extends MyActionBar implements OnClickListener {

  private ImageView imageView;

  private Button    okButton;

  private String    filePath = null;
  
  private File file;

  private String    okText;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.social_selected_image_layout);
    onChangeLanguage();
    init();
  }

  private void init() {
    imageView = (ImageView) findViewById(R.id.social_selected_image);
    filePath = getIntent().getStringExtra(ExoConstants.SELECTED_IMAGE_EXTRA);
    file = new File(filePath);
    setTitle(file.getName());
    Bitmap bitmap = PhotoUltils.shrinkBitmap(filePath, 1024, 860);
    imageView.setImageBitmap(bitmap);
    okButton = (Button) findViewById(R.id.social_selected_image_ok_button);
    okButton.setText(okText);
    okButton.setOnClickListener(this);

  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      finish();
      if (SocialImageLibrary.socialImageLibrary != null)
        SocialImageLibrary.socialImageLibrary.finish();
      if (SocialPhotoAlbums.socialPhotoAlbums != null)
        SocialPhotoAlbums.socialPhotoAlbums.finish();
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
    super.onBackPressed();
    finish();
  }

  public void onClick(View view) {
    if (view.equals(okButton)) {
      if (filePath != null) {
        if (DocumentActivity._documentActivityInstance != null) {
          DocumentActivity._documentActivityInstance._sdcard_temp_dir = filePath;
          DocumentActivity._documentActivityInstance.uploadFile();
        }

        else
          ComposeMessageActivity.addImageToMessage(file);

        finish();

        if (SocialImageLibrary.socialImageLibrary != null)
          SocialImageLibrary.socialImageLibrary.finish();
        if (SocialPhotoAlbums.socialPhotoAlbums != null)
          SocialPhotoAlbums.socialPhotoAlbums.finish();
      }

    }
  }

  private void onChangeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    okText = bundle.getString("OK");

  }

}
