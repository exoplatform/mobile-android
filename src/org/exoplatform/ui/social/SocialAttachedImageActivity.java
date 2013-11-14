package org.exoplatform.ui.social;

//import greendroid.widget.ActionBarItem;

import android.support.v7.app.ActionBarActivity;
import org.exoplatform.R;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.image.SocialImageLoader;
//import org.exoplatform.widget.MyActionBar;

import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class SocialAttachedImageActivity
    extends ActionBarActivity {
    //extends MyActionBar {
  private ImageView imageView;

  private String    imageUrl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);

    //setActionBarContentView(R.layout.social_attached_image_layout);
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


  /**   TODO replace
  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      finish();
      if (SocialDetailActivity.socialDetailActivity != null) {
        SocialDetailActivity.socialDetailActivity.finish();
      }
      if (SocialTabsActivity.instance != null)
        SocialTabsActivity.instance.finish();
      break;

    case 0:
      break;
    }
    return true;
  }
   **/

  @Override
  public void onBackPressed() {
    finish();
  }

}
