package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;

import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.image.SocialImageLoader;
import org.exoplatform.widget.MyActionBar;

import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

import com.cyrilmottier.android.greendroid.R;

public class SocialAttachedImageActivity extends MyActionBar {
  private ImageView imageView;

  private String    imageUrl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.social_attached_image_layout);
    imageUrl = SocialDetailHelper.getInstance().getAttachedImageUrl();
    String imageName = getImageName(imageUrl);
    setTitle(imageName);
    init();
  }

  private void init() {
    imageView = (ImageView) findViewById(R.id.social_attached_image_view);
    new SocialImageLoader(this).displayImage(imageUrl, imageView);
  }

  private String getImageName(String url) {
    int index = url.lastIndexOf("/");
    String name = url.substring(index + 1);
    return name;
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
    finish();
  }

}
