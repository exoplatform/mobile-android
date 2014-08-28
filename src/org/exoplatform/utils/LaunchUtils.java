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
package org.exoplatform.utils;

import greendroid.util.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.image.SocialImageLoader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

/**
 * Entry point of application
 * load setting and redirect application to appropriate screen
 *
 * if no account is configured, welcome screen shows up
 * if account is configured and auto-login disabled, login screen shows up
 * if account is configured and auto-login enabled, home screen shows up
 */
public class LaunchUtils {

  private Context                  mContext;

  private SharedPreferences        mSharedPreference;

  private AccountSetting           mSetting;

  private static final String TAG = "eXoLaunchController";

  public LaunchUtils(Activity context) {
    mContext = context;
    mSharedPreference = context.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    mSetting = AccountSetting.getInstance();

    initAssets();
    setAppVersion();
    setLocalize();
    initSocialImageLoader();
    String oldConfigFile = ServerConfigurationUtils.checkPreviousAppConfig(mContext);
    if (oldConfigFile != null) setOldServerList(oldConfigFile);
    else setServerList();
  }


  /**
   * Init assets utils
   */
  private void initAssets() {
    AssetUtils.setContext(mContext);
  }

  /**
   * Retrieve server list from config file and set it up for server setting helper
   */
  private void setServerList() {
    ArrayList<ServerObjInfo> _serverList =
        ServerConfigurationUtils.getServerListFromFile(mContext, ExoConstants.EXO_SERVER_SETTING_FILE);
    ServerSettingHelper.getInstance().setServerInfoList(_serverList);

    int selectedServerIdx = Integer.parseInt(mSharedPreference.getString(ExoConstants.EXO_PRF_DOMAIN_INDEX, "-1"));
    mSetting.setDomainIndex(String.valueOf(selectedServerIdx));
    mSetting.setCurrentServer((selectedServerIdx == -1 || selectedServerIdx >= _serverList.size()) ? null : _serverList.get(selectedServerIdx));
  }


  /**
   * Get server list from previous config of app
   *
   * @param oldConfigFile
   */
  private void setOldServerList(String oldConfigFile) {
    ArrayList<ServerObjInfo> _serverList =
        ServerConfigurationUtils.getServerListFromOldConfigFile(oldConfigFile);
    ServerSettingHelper.getInstance().setServerInfoList(_serverList);
    if (_serverList.size() == 0) return;
    /* force app to start login screen */
    mSetting.setDomainIndex("0");
    mSetting.setCurrentServer(_serverList.get(0));
    _serverList.get(0).isAutoLoginEnabled = false;

    /* persist the configuration */
    new Thread(new Runnable() {
      @Override
      public void run() {
        Log.i(TAG, "persisting config");
        SettingUtils.persistServerSetting(mContext);
      }
    }).start();
  }

  /**
   * Provide app version for setting
   */
  private void setAppVersion() {
    try {
      String appVer = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
      ServerSettingHelper.getInstance().setApplicationVersion(appVer);
    } catch (NameNotFoundException e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("NameNotFoundException", "Error of getting package information!");
    }
  }

  /**
   * Set localize
   */
  private void setLocalize() {
    String strLocalize = mSharedPreference.getString(ExoConstants.EXO_PRF_LOCALIZE, "");

    /** no app language */
    if (strLocalize.equals("")) {
      String[] currentLanguagesSupported = new String[]{
          ExoConstants.ENGLISH_LOCALIZATION,
          ExoConstants.FRENCH_LOCALIZATION,
          ExoConstants.GERMAN_LOCALIZATION,
          ExoConstants.SPANISH_LOCALIZATION
      };

      strLocalize = Locale.getDefault().getLanguage();
      if (!Arrays.asList(currentLanguagesSupported).contains(strLocalize))
        strLocalize = ExoConstants.ENGLISH_LOCALIZATION;
    }
    SettingUtils.setLocale(mContext, strLocalize);
  }

  /**
  * Initialize SocialImageLoader when application start up and clear all data
  * cache.
  */
  private void initSocialImageLoader() {
    if (SocialDetailHelper.getInstance().socialImageLoader == null) {
      SocialDetailHelper.getInstance().socialImageLoader = new SocialImageLoader(mContext);
      SocialDetailHelper.getInstance().socialImageLoader.clearCache();
    }
  }
}