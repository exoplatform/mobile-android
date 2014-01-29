package org.exoplatform.ui.social;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

import org.exoplatform.poc.tabletversion.R;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.image.SocialImageLoader;


public class SocialAttachedImageActivity extends ActionBarActivity {

  private ImageView imageView;

  private String    imageUrl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);

    setContentView(R.layout.social_attached_image_layout);

    if (savedInstanceState != null)
      finish();
    else {
      imageUrl = SocialDetailHelper.getInstance().getAttachedImageUrl();
      String imageName = getImageName(imageUrl);
      setTitle(imageName);
      init();
    }
  }

  private void init() {
    imageView = (ImageView) findViewById(R.id.social_attached_image_view);
    if (SocialDetailHelper.getInstance().socialImageLoader == null) {
      SocialDetailHelper.getInstance().socialImageLoader = new SocialImageLoader(this);
    }
    SocialDetailHelper.getInstance().socialImageLoader.displayImage(imageUrl, imageView, false);
  }

  private String getImageName(String url) {
    int index = url.lastIndexOf("/");
    String name = url.substring(index + 1);
    return name;
  }


  @Override
  public void onBackPressed() {
    finish();
  }

}
