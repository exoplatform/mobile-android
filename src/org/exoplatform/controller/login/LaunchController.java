package org.exoplatform.controller.login;

import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.exoplatform.proxy.ExoServerConfiguration;
import org.exoplatform.proxy.ServerObj;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

public class LaunchController {
  private SharedPreferences sharedPreference;

  private ResourceBundle    bundle;

  private Context           context;

  public LaunchController(Context c, SharedPreferences prefs) {
    sharedPreference = prefs;
    context = c;
    getLaunchInfo();
    getServerInfo();
  }

  private void getLaunchInfo() {
    if (sharedPreference == null) {
      sharedPreference = context.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    }
    LocalizationHelper.getInstance().setSharePrefs(sharedPreference);
    String strLocalize;
    strLocalize = sharedPreference.getString(ExoConstants.EXO_PRF_LOCALIZE,
                                             ExoConstants.EXO_PRF_LOCALIZE);
    if (strLocalize == null || strLocalize.equalsIgnoreCase(ExoConstants.EXO_PRF_LOCALIZE))
      strLocalize = "LocalizeEN.properties";

    try {
      bundle = new PropertyResourceBundle(context.getAssets().open(strLocalize));
    } catch (Exception e) {

    }
    LocalizationHelper.getInstance().setLocation(strLocalize);

    LocalizationHelper.getInstance().setResourceBundle(bundle);

    AccountSetting.getInstance()
                  .setUsername(sharedPreference.getString(ExoConstants.EXO_PRF_USERNAME, ""));
    AccountSetting.getInstance()
                  .setPassword(sharedPreference.getString(ExoConstants.EXO_PRF_PASSWORD, ""));
    AccountSetting.getInstance()
                  .setDomainIndex(sharedPreference.getInt(ExoConstants.EXO_PRF_DOMAIN_INDEX, 0));
    AccountSetting.getInstance()
                  .setDomainName(sharedPreference.getString(ExoConstants.EXO_PRF_DOMAIN, ""));

  }

  private void getServerInfo() {
    ExoServerConfiguration configurationInstance = new ExoServerConfiguration(context);

    String appVer = "";
    String oldVer = configurationInstance.getAppVersion();
    try {
      appVer = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
    }

    if (!(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED))) {
      configurationInstance._arrDefaulServerList = configurationInstance.getDefaultServerList();
      if (configurationInstance._arrDefaulServerList.size() > 0)
        configurationInstance._arrServerList.addAll(configurationInstance._arrDefaulServerList);
    } else {
      ArrayList<ServerObj> defaultServerList = configurationInstance.getServerListWithFileName("DefaultServerList.xml");

      configurationInstance._arrDeletedServerList = configurationInstance.getServerListWithFileName("DeletedDefaultServerList.xml");
      if (appVer.compareToIgnoreCase(oldVer) > 0) {

        ArrayList<ServerObj> deletedDefaultServers = configurationInstance._arrDeletedServerList;

        ArrayList<ServerObj> tmp = new ArrayList<ServerObj>();
        if (deletedDefaultServers == null)
          tmp = defaultServerList;
        else {
          for (int i = 0; i < defaultServerList.size(); i++) {
            ServerObj newServerObj = defaultServerList.get(i);
            boolean isDeleted = false;
            for (int j = 0; j < deletedDefaultServers.size(); j++) {
              ServerObj deletedServerObj = deletedDefaultServers.get(i);
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

        configurationInstance.createXmlDataWithServerList(tmp, "DefaultServerList.xml", appVer);
        configurationInstance.version = appVer;
      } else {

      }

      configurationInstance._arrUserServerList = configurationInstance.getServerListWithFileName("UserServerList.xml");
      configurationInstance._arrDefaulServerList = configurationInstance.getServerListWithFileName("DefaultServerList.xml");

      if (configurationInstance._arrDefaulServerList.size() > 0)
        configurationInstance._arrServerList.addAll(configurationInstance._arrDefaulServerList);
      if (configurationInstance._arrUserServerList.size() > 0)
        configurationInstance._arrServerList.addAll(configurationInstance._arrUserServerList);
    }

    AccountSetting.getInstance().setServerInfoList(configurationInstance._arrServerList);
  }

}
