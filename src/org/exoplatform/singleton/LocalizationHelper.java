package org.exoplatform.singleton;

import android.content.SharedPreferences;

/*
 * This class manage the localization between English and French
 */
public class LocalizationHelper {

  private SharedPreferences         sharedPreference;

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

}
