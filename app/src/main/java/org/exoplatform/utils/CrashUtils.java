/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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

package org.exoplatform.utils;

import com.crashlytics.android.Crashlytics;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import io.fabric.sdk.android.Fabric;

public class CrashUtils {

  private static final String LOG_TAG            = CrashUtils.class.getName();

  private static final String KEY_USERNAME       = "UserName";

  private static final String KEY_SERVER_DOMAIN  = "Server Domain";

  private static final String KEY_SERVER_VERSION = "Server Version";

  private static final String KEY_IMAGE_HEIGHT   = "Height Before Shrink";

  private static final String KEY_IMAGE_WIDTH    = "Width Before Shrink";

  private static final String KEY_SHRINK_RATIO   = "Shrink Ratio";

  private static final String KEY_IMAGE_SIZE     = "Resize Image With Size KB";

  private static final String KEY_OPEN_FILE_TYPE = "File Open Type";

  public static void initialize(Context ctx) {
    Fabric.with(ctx, new Crashlytics());
  }

  public static void setUsername(String username) {
    setString(KEY_USERNAME, username);
  }

  public static void setServerInfo(String domain, String version) {
    setString(KEY_SERVER_DOMAIN, domain);
    setString(KEY_SERVER_VERSION, version);
  }

  public static void setShrinkInfo(BitmapFactory.Options options) {
    if (options != null) {
      setString(KEY_IMAGE_HEIGHT, String.valueOf(options.outHeight));
      setString(KEY_IMAGE_WIDTH, String.valueOf(options.outWidth));
      setString(KEY_SHRINK_RATIO, String.valueOf(options.inSampleSize));
    } else {
      setString(KEY_IMAGE_HEIGHT, "");
      setString(KEY_IMAGE_WIDTH, "");
      setString(KEY_SHRINK_RATIO, "");
    }
  }

  public static void setResizeInfo(int imageSize) {
    setString(KEY_IMAGE_SIZE, String.valueOf(imageSize / 1024));
  }

  public static void setOpenFileType(String mimeType) {
    setString(KEY_OPEN_FILE_TYPE, mimeType);
  }

  /*
   * Generic Methods
   */

  public static void setString(String key, String value) {
    try {
      Crashlytics.setString(key, value);
    } catch (Exception e) {
      // TODO: Remove this hack that was added so Robolectric wouldn't crash
      // during tests
      Log.d(LOG_TAG, String.format("Error with %s : %s (%s)", key, value, e.getMessage()));
    }
  }

  public static void loge(String tag, String message) {
    Crashlytics.log(Log.ERROR, tag, message);
  }

}
