/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.utils;

import java.util.Locale;

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
    final Locale locale = new Locale(localize);
    Locale.setDefault(locale);

    final Resources res = mContext.getResources();
    Configuration config = res.getConfiguration();
    config.locale = locale;
    res.updateConfiguration(config, res.getDisplayMetrics());
    SharedPreferences.Editor editor = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0)
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

}
