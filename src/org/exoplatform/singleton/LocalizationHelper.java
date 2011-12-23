package org.exoplatform.singleton;

import greendroid.util.Config;

import java.util.ResourceBundle;

import android.content.SharedPreferences;
import android.util.Log;

/*
 * This class manage the localization between English and French
 */
public class LocalizationHelper {

  private SharedPreferences         sharedPreference;

  private ResourceBundle            resBundle;

  private String                    localization;

  private static LocalizationHelper bundle = new LocalizationHelper();

  private LocalizationHelper() {

  }

  public static LocalizationHelper getInstance() {
    return bundle;
  }

  public void setSharePrefs(SharedPreferences prefs) {
    sharedPreference = prefs;
  }

  public SharedPreferences getSharePrefs() {
    return sharedPreference;
  }

  public void setLocation(String local) {
    localization = local;
  }

  public String getLocation() {
    return localization;
  }

  public ResourceBundle getResourceBundle() {
    return resBundle;
  }

  public void setResourceBundle(ResourceBundle bl) {
    resBundle = bl;
  }

  /*
   * get key value in localization resource return null if the key is not exist
   */
  public String getString(String key) {
    try {
      return resBundle.getString(key);
    } catch (Exception e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("LOCALIZATION", "Missing key " + key + " in localization resources");
      return null;
    }

  }

}
