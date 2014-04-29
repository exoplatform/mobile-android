/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;

import org.exoplatform.R;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.image.SocialImageLoader;
import org.exoplatform.widget.MyActionBar;

import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class SocialAttachedImageActivity extends MyActionBar {
  private ImageView imageView;

  private String    imageUrl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.social_attached_image_layout);
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

  @Override
  public void onBackPressed() {
    finish();
  }

}
