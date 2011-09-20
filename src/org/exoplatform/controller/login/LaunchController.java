package org.exoplatform.controller.login;

import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.exoplatform.proxy.ExoServerConfiguration;
import org.exoplatform.proxy.ServerObj;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

public class LaunchController {
  private SharedPreferences    sharedPreference;

  private ResourceBundle       bundle;

  private Context              context;

  // private ArrayList<ServerObj> _arrUserServerList;
  //
  private ArrayList<ServerObj> _arrDefaulServerList;

  //
  private ArrayList<ServerObj> _arrDeletedServerList;

  private ArrayList<ServerObj> _arrServerList;

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

    // check if localize file name is null or not assigned then default is
    // "LocalizeEN.properties" file
    if (strLocalize == null || strLocalize.equalsIgnoreCase(ExoConstants.EXO_PRF_LOCALIZE))
      strLocalize = "LocalizeEN.properties";

    try {
      bundle = new PropertyResourceBundle(context.getAssets().open(strLocalize));
      LocalizationHelper.getInstance().setLocation(strLocalize);

      LocalizationHelper.getInstance().setResourceBundle(bundle);
      AccountSetting.getInstance()
                    .setUsername(sharedPreference.getString(ExoConstants.EXO_PRF_USERNAME, ""));
      AccountSetting.getInstance()
                    .setPassword(sharedPreference.getString(ExoConstants.EXO_PRF_PASSWORD, ""));
      System.out.println("domain index "
          + sharedPreference.getString(ExoConstants.EXO_PRF_DOMAIN_INDEX, "0"));
      System.out.println("domain name "
                         + sharedPreference.getString(ExoConstants.EXO_PRF_DOMAIN, ""));
      AccountSetting.getInstance()
                    .setDomainIndex(sharedPreference.getString(ExoConstants.EXO_PRF_DOMAIN_INDEX,
                                                               "0"));
      AccountSetting.getInstance()
                    .setDomainName(sharedPreference.getString(ExoConstants.EXO_PRF_DOMAIN, ""));

    } catch (Exception e) {

    }

  }

  private void getServerInfo() {
    _arrServerList = new ArrayList<ServerObj>();

    String appVer = "";
    String oldVer = ExoServerConfiguration.getAppVersion(context);
    try {
      appVer = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
    }

    if (!(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED))) {
      _arrDefaulServerList = ExoServerConfiguration.getDefaultServerList(context);
      if (_arrDefaulServerList.size() > 0)
        _arrServerList.addAll(_arrDefaulServerList);
    } else {
      ArrayList<ServerObj> defaultServerList = ExoServerConfiguration.getServerListWithFileName("DefaultServerList.xml");

      _arrDeletedServerList = ExoServerConfiguration.getServerListWithFileName("DeletedDefaultServerList.xml");
      if (appVer.compareToIgnoreCase(oldVer) > 0) {

        ArrayList<ServerObj> deletedDefaultServers = _arrDeletedServerList;

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

        ExoServerConfiguration.createXmlDataWithServerList(tmp, "DefaultServerList.xml", appVer);
        ServerSettingHelper.getInstance().setVersion(appVer);
      } else {

      }

      _arrDefaulServerList = ExoServerConfiguration.getServerListWithFileName("DefaultServerList.xml");

      if (_arrDefaulServerList.size() > 0)
        _arrServerList.addAll(_arrDefaulServerList);
      // if (_arrUserServerList.size() > 0)
      // _arrServerList.addAll(_arrUserServerList);
    }

    ServerSettingHelper.getInstance().setServerInfoList(_arrServerList);
    // ServerSettingHelper.getInstance().setUserServerList(_arrUserServerList);
    // ServerSettingHelper.getInstance().setDefaultServerList(_arrDefaulServerList);
    // ServerSettingHelper.getInstance().setDeleteServerList(_arrDeletedServerList);
  }

}
