package org.exoplatform.social.image;

import greendroid.widget.ActionBarItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;

import org.exoplatform.controller.AppController;
import org.exoplatform.social.ComposeMessageActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
  
  private String selectedPhotoTitle;
  
  private String okText;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.social_selected_image_layout);
    onChangeLanguage(AppController.bundle);
    init();
  }

  private void init() {
    imageView = (ImageView) findViewById(R.id.social_selected_image);
    filePath = getIntent().getStringExtra(ExoConstants.SELECTED_IMAGE_EXTRA);

    try {
      FileInputStream fis = new FileInputStream(new File(filePath));
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = 8;
      Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
      imageView.setImageBitmap(bitmap);
      fis.close();
    } catch (Exception e) {
    }

    okButton = (Button) findViewById(R.id.social_selected_image_ok_button);
    okButton.setText(okText);
    okButton.setOnClickListener(this);

  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      finish();
      SocialImageLibrary.socialImageLibrary.finish();
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

  @Override
  public void onClick(View view) {
    if (view == okButton) {
      if (filePath != null) {
        File file = new File(filePath);
        ComposeMessageActivity.addImageToMessage(file);
        finish();
        SocialImageLibrary.socialImageLibrary.finish();
      }

    }
  }
  
  private void onChangeLanguage(ResourceBundle resourceBundle) {
    selectedPhotoTitle = resourceBundle.getString("SelectedPhoto");
    setTitle(selectedPhotoTitle);
    okText = resourceBundle.getString("OK");
    
  }

}
