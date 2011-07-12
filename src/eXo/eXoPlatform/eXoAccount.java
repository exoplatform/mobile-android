package eXo.eXoPlatform;

import android.app.Activity;
import android.content.SharedPreferences;
import eXo.eXoPlatform.Activity.Authenticate.AppController;

// Hold activing account
public class eXoAccount extends Activity {
  // Instance
  public static eXoAccount _instance;

  // Username/password
  private String           _strUserName;

  private String           _strPassword;

  // Singleton constructor
  public static eXoAccount instance() {
    if (_instance == null) {
      return newInstance();
    } else {
      return _instance;
    }
  }

  // Singleton constructor
  public static eXoAccount newInstance() {
    if (_instance != null) {
      _instance = null;
    }
    _instance = new eXoAccount();
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
