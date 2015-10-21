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

import java.util.Locale;

import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.image.FileCache;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Mar
 * 2, 2012
 */
public class SettingUtils {
    /*
     * Set and update resource locale configuration
     */

    public static void setLocale(Context mContext, String localize) {
        final Locale locale;
        // Back to English if invalid locale is given
        if (localize == null || "".equals(localize))
            localize = "en";
        // Support for locales with country code, e.g. pt_BR
        if (localize.length() > 2 && localize.charAt(2) == '_') {
            String[] language = localize.split("_");
            locale = new Locale(language[0], language[1]);
        } else {
            locale = new Locale(localize);
        }
        Locale.setDefault(locale);

        final Resources res = mContext.getResources();
        Configuration config = res.getConfiguration();
        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());
        SharedPreferences.Editor editor = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE,
                                                                        0)
                                                  .edit();
        editor.putString(ExoConstants.EXO_PRF_LOCALIZE, localize);
        editor.commit();
    }

    public static String getLanguage(Context context) {
        return Locale.getDefault().getLanguage();
    }

    public static String getPrefsLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
        return prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, "");
    }

    public static void setDefaultLanguage(Context context) {
        String languageCode = SettingUtils.getPrefsLanguage(context);
        if (!SettingUtils.getLanguage(context).equals(languageCode)) {
            SettingUtils.setLocale(context, languageCode);
        }
    }

    /**
     * Utility to persist server configuration after adding new server to server
     * list Only saves domain index to shared pref
     * 
     * @param context
     */
    public static void persistServerSetting(Context context) {
        ServerSettingHelper settingHelper = ServerSettingHelper.getInstance();

        // Persist the configuration
        ServerConfigurationUtils.generateXmlFileWithServerList(context,
                                                               settingHelper.getServerInfoList(context),
                                                               ExoConstants.EXO_SERVER_SETTING_FILE,
                                                               "");

        modifySharedPerf(context);
        clearDownloadRepository(context);
    }

    public static void modifySharedPerf(Context context) {
        // Modify pref
        SharedPreferences.Editor editor = context.getSharedPreferences(ExoConstants.EXO_PREFERENCE,
                                                                       0).edit();
        editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, AccountSetting.getInstance()
                                                                          .getDomainIndex());
        /*
         * disable saving social filter when login with difference account and
         * clear the download repository
         */
        editor.putBoolean(ExoConstants.SETTING_SOCIAL_FILTER, false);
        editor.commit();
    }

    private static void clearDownloadRepository(Context context) {
        FileCache filecache = new FileCache(context, ExoConstants.DOCUMENT_FILE_CACHE);
        filecache.clear();
    }

    /**
     * Disable auto login for the current account and persist the change
     * 
     * @param ctx
     */
    public static void disableAutoLogin(Context ctx) {
      // Jul 21, 2015, should use application context?
        int currentAccountIdx = Integer.parseInt(AccountSetting.getInstance().getDomainIndex());
        int numberOfAccounts = ServerSettingHelper.getInstance().getServerInfoList(ctx).size();
        if (currentAccountIdx >= 0 && currentAccountIdx < numberOfAccounts)
            ServerSettingHelper.getInstance().getServerInfoList(ctx).get(currentAccountIdx).isAutoLoginEnabled = false;
        persistServerSetting(ctx);
    }
}
