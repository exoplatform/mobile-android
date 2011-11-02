package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;

import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ImageDownloader;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.MyActionBar;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.cyrilmottier.android.greendroid.R;

public class SocialAttachedImageActivity extends MyActionBar implements OnClickListener {
  private ImageView imageView;

  private Button    okButton;

  private String    okText;

  private String    imageUrl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.social_attached_image_layout);
    onChangeLanguage();
    String imageName = SocialDetailHelper.getInstance().getAttachedImageName();
    setTitle(imageName);
    imageUrl = SocialDetailHelper.getInstance().getAttachedImageUrl();
    init();
  }

  private void init() {
    imageView = (ImageView) findViewById(R.id.social_attached_image_view);
    SocialDetailHelper.getInstance().imageDownloader.download(imageUrl, imageView,ExoConnectionUtils._strCookie);
    okButton = (Button) findViewById(R.id.social_attached_image_ok_button);
    okButton.setText(okText);
    okButton.setOnClickListener(this);
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      finish();
      if (SocialDetailActivity.socialDetailActivity != null) {
        SocialDetailActivity.socialDetailActivity.finish();
      }
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
    if (view == okButton) {
      finish();
    }
  }

  private void onChangeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    okText = bundle.getString("OK");

  }

}
