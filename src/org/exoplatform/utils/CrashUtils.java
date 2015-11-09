package org.exoplatform.utils;

import com.crashlytics.android.Crashlytics;

import android.graphics.BitmapFactory;

public class CrashUtils {

  private static final String KEY_USERNAME = "UserName";
  
  private static final String KEY_SERVER = "Server Domain";
  
  private static final String KEY_IMAGE_HEIGHT = "Height Before Shrink";
  
  private static final String KEY_IMAGE_WIDTH = "Width Before Shrink";
  
  private static final String KEY_SHRINK_RATIO = "Shrink Ratio";
  
  private static final String KEY_IMAGE_SIZE = "Resize Image With Size KB";
  
  public static void setUsername(String username) {
    Crashlytics.setString(KEY_USERNAME, username);
  }
  
  public static void setServerDomain(String domain) {
    Crashlytics.setString(KEY_SERVER, domain);
  }
  
  public static void setShrinkInfo(BitmapFactory.Options options) {
    if (options != null) {
      Crashlytics.setInt(KEY_IMAGE_HEIGHT, options.outHeight);
      Crashlytics.setInt(KEY_IMAGE_WIDTH, options.outWidth);
      Crashlytics.setInt(KEY_SHRINK_RATIO, options.inSampleSize);
    } else {
      Crashlytics.setInt(KEY_IMAGE_HEIGHT, -1);
      Crashlytics.setInt(KEY_IMAGE_WIDTH, -1);
      Crashlytics.setInt(KEY_SHRINK_RATIO, -1);
    }
  }
  
  public static void setResizeInfo(int imageSize) {
    Crashlytics.setInt(KEY_IMAGE_SIZE, imageSize / 1024);
  }
  
  public static void setString(String key, String value) {
    Crashlytics.setString(key, value);
  }
  
}
