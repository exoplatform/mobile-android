package exo.exoplatform.model;

import android.app.Activity;
import android.content.SharedPreferences;
import exo.exoplatform.controller.AppController;

// Hold activing account
public class ExoAccount extends Activity {
  // Instance
  public static ExoAccount _instance;

  // Username/password
  private String           _strUserName;

  private String           _strPassword;

  // Singleton constructor
  public static ExoAccount instance() {
    if (_instance == null) {
      return newInstance();
    } else {
      return _instance;
    }
  }

  // Singleton constructor
  public static ExoAccount newInstance() {
    if (_instance != null) {
      _instance = null;
    }
    _instance = new ExoAccount();
    return _instance;
  }

  // Gettors and settors
  public void setUserName(String username) {
    SharedPreferences sharedPreference = getSharedPreferences(AppController.EXO_PREFERENCE, 0);
    SharedPreferences.Editor editor = sharedPreference.edit();
    editor.putString(AppController.EXO_PRF_USERNAME, _strUserName);
    editor.commit();
  }

  public void setPassword(String username) {
    SharedPreferences sharedPreference = getSharedPreferences(AppController.EXO_PREFERENCE, 0);
    SharedPreferences.Editor editor = sharedPreference.edit();
    editor.putString(AppController.EXO_PRF_PASSWORD, _strPassword);
    editor.commit();
  }

  public String getUserName() {
    SharedPreferences sharedPreference = getSharedPreferences(AppController.EXO_PREFERENCE, 0);
    return sharedPreference.getString(AppController.EXO_PRF_USERNAME, "exo_prf_username");
  }

  public String getPassword() {
    SharedPreferences sharedPreference = getSharedPreferences(AppController.EXO_PREFERENCE, 0);
    return sharedPreference.getString(AppController.EXO_PRF_PASSWORD, "exo_prf_password");
  }
}
