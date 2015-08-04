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
package org.exoplatform;

import org.exoplatform.social.client.api.SocialClientContext;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.LaunchUtils;

import android.content.Intent;
import android.net.Uri;
import greendroid.app.GDApplication;

public class ExoApplication extends GDApplication {
  private String appUrl = "http://github.com/cyrilmottier/GreenDroid";

  @Override
  public Class<?> getHomeActivityClass() {
    return HomeActivity.class;
  }

  @Override
  public Intent getMainApplicationIntent() {
    return new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl));
  }

  @Override
  public void onCreate() {
    super.onCreate();
    LaunchUtils.setAppVersion(this);
    SocialClientContext.setUserAgent(ExoConnectionUtils.getUserAgent());
  }
  
}
