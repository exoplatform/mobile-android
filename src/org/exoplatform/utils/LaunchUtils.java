package org.exoplatform.utils;

import android.app.Activity;
import android.content.Intent;
import greendroid.util.Config;

import java.util.ArrayList;
import java.util.Locale;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.ui.WelcomeActivity;
import org.exoplatform.utils.AssetUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ServerConfigurationUtils;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.utils.image.SocialImageLoader;

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

  private Activity                 mCurrentActivity;

  private static final String TAG = "eXoLaunchController";

  public LaunchUtils(Activity context) {
    mContext = context;
    mCurrentActivity = context;
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
    mSetting.setCurrentServer((selectedServerIdx == -1) ? null : _serverList.get(selectedServerIdx));
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