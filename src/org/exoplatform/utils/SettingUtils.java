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
import android.content.res.Configuration;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Mar
 * 2, 2012
 */
public class SettingUtils {
  /*
   * Set and update resource locale configuration
   */

  public static void setLocalization(Context mContext, Configuration config, String localize) {
    String realLocale = "";
    if (localize.equals(ExoConstants.FRENCH_LOCALIZATION)) {
      realLocale = "fr";
    }
    Locale locale = new Locale(realLocale);
    config.locale = locale;
    mContext.getResources()
            .updateConfiguration(config, mContext.getResources().getDisplayMetrics());
  }

}
