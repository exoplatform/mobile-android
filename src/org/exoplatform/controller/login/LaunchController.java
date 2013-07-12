package org.exoplatform.controller.login;

import greendroid.util.Config;

import java.util.ArrayList;
import java.util.Locale;

import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ServerConfigurationUtils;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.utils.image.SocialImageLoader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

/**
 * Setup server list and account setting
 */
public class LaunchController {
  private SharedPreferences        sharedPreference;

  private Context                  context;

  /* TODO: removed?
  * now we don't use default server list anymore
  * */
  private ArrayList<ServerObjInfo> _arrDefaulServerList;

  private ArrayList<ServerObjInfo> _arrDeletedServerList;

  private ArrayList<ServerObjInfo> _arrServerList;

  private static final String TAG = "eXoLaunchController";

  public LaunchController(Context c, SharedPreferences prefs) {
    sharedPreference = prefs;
    context = c;
    getLaunchInfo();
    //getServerInfo();

    getServerInfo1();

    /*
     * Initialize SocialImageLoader when application start up and clear all data
     * cache.
     */
    if (SocialDetailHelper.getInstance().socialImageLoader == null) {
      SocialDetailHelper.getInstance().socialImageLoader = new SocialImageLoader(context);
      SocialDetailHelper.getInstance().socialImageLoader.clearCache();
    }
  }

  private void getLaunchInfo() {
    Log.i(TAG, "getLaunchInfo");
    sharedPreference = context.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    String strLocalize = sharedPreference.getString(ExoConstants.EXO_PRF_LOCALIZE, "");
    if (strLocalize.equals("")) {

      strLocalize = Locale.getDefault().getLanguage();
      /*
       * check if locale language is not French then assigned the default locale
       * is English
       */
      if (!strLocalize.equals(ExoConstants.FRENCH_LOCALIZATION)) {
        strLocalize = ExoConstants.ENGLISH_LOCALIZATION;
      }

    }

    Log.i(TAG, "user: " + sharedPreference.getString(ExoConstants.EXO_PRF_USERNAME, ""));
    Log.i(TAG, "pass: " + sharedPreference.getString(ExoConstants.EXO_PRF_PASSWORD, ""));
    Log.i(TAG, "domain index: " + sharedPreference.getString(ExoConstants.EXO_PRF_DOMAIN_INDEX, "-1"));
    Log.i(TAG, "domain name: " + sharedPreference.getString(ExoConstants.EXO_PRF_DOMAIN, ""));

    /*
     * Set the Locale which affect to our application
     */
    SettingUtils.setLocale(context, strLocalize);

    AccountSetting.getInstance()
                  .setUsername(sharedPreference.getString(ExoConstants.EXO_PRF_USERNAME, ""));
    AccountSetting.getInstance()
                  .setPassword(sharedPreference.getString(ExoConstants.EXO_PRF_PASSWORD, ""));
    AccountSetting.getInstance()
                  .setDomainIndex(sharedPreference.getString(ExoConstants.EXO_PRF_DOMAIN_INDEX,
                                                             "-1"));
    AccountSetting.getInstance()
                  .setDomainName(sharedPreference.getString(ExoConstants.EXO_PRF_DOMAIN, ""));
  }

  // TODO: check and remove
  private void getServerInfo() {

    _arrServerList = new ArrayList<ServerObjInfo>();

    String appVer = "";
    String oldVer = ServerConfigurationUtils.getAppVersion(context);
    try {
      appVer = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
      ServerSettingHelper.getInstance().setApplicationVersion(appVer);
    } catch (NameNotFoundException e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("NameNotFoundException", "Get package information is error!");
    }

    if (!(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED))) {

      Log.i(TAG, "get default server list from default configuration");
      _arrDefaulServerList = ServerConfigurationUtils.getDefaultServerList(context);
      if (_arrDefaulServerList.size() > 0)
        _arrServerList.addAll(_arrDefaulServerList);
    } else {

      Log.i(TAG, "get default server list from xml file DefaultServerList.xml");
      ArrayList<ServerObjInfo> defaultServerList = ServerConfigurationUtils.getServerListWithFileName("DefaultServerList.xml");

      _arrDeletedServerList = ServerConfigurationUtils.getServerListWithFileName("DeletedDefaultServerList.xml");
      if (appVer.compareToIgnoreCase(oldVer) > 0) {

        ArrayList<ServerObjInfo> deletedDefaultServers = _arrDeletedServerList;

        ArrayList<ServerObjInfo> tmp = new ArrayList<ServerObjInfo>();
        if (deletedDefaultServers == null)
          tmp = defaultServerList;
        else {
          for (int i = 0; i < defaultServerList.size(); i++) {
            ServerObjInfo newServerObj = defaultServerList.get(i);
            boolean isDeleted = false;
            for (int j = 0; j < deletedDefaultServers.size(); j++) {
              ServerObjInfo deletedServerObj = deletedDefaultServers.get(i);
              if (newServerObj._strServerName.equalsIgnoreCase(deletedServerObj._strServerName)
                  && newServerObj._strServerUrl.equalsIgnoreCase(deletedServerObj._strServerUrl)) {
                isDeleted = true;
                break;
              }
            }
            if (!isDeleted)
              tmp.add(newServerObj);
          }
        }

        ServerConfigurationUtils.createXmlDataWithServerList(tmp, "DefaultServerList.xml", appVer);
      }

      _arrDefaulServerList = ServerConfigurationUtils.getServerListWithFileName("DefaultServerList.xml");

      if (_arrDefaulServerList.size() > 0)
        _arrServerList.addAll(_arrDefaulServerList);
    }

    ServerSettingHelper.getInstance().setServerInfoList(_arrServerList);
  }


  /**
   * Retrieve server list from config file along with application information
   */
  private void getServerInfo1() {
    Log.i(TAG, "getServerInfo1");

    /* get app info */
    String appVer = "";
    String oldVer = ServerConfigurationUtils.getAppVersion(context);
    try {
      appVer = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
      ServerSettingHelper.getInstance().setApplicationVersion(appVer);
    } catch (NameNotFoundException e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("NameNotFoundException", "Get package information is error!");
    }

    /* retrieve server list */
    _arrServerList = ServerConfigurationUtils.getServerListFromFile(context, ExoConstants.EXO_SERVER_SETTING_FILE);
    ServerSettingHelper.getInstance()
        .setServerInfoList( (_arrServerList == null) ? new ArrayList<ServerObjInfo>(): _arrServerList);
  }

}