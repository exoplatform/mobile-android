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
package org.exoplatform;

import org.exoplatform.social.client.api.SocialClientContext;
import org.exoplatform.utils.AssetUtils;
import org.exoplatform.utils.CrashUtils;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.LaunchUtils;
import org.exoplatform.utils.Log;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import java.lang.reflect.Method;

public class ExoApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    initCrashlytics();
    if ("samsung".equalsIgnoreCase(Build.MANUFACTURER))
      initSamsungClipboardManager();
    LaunchUtils.setAppVersion(this);
    SocialClientContext.setUserAgent(ExoConnectionUtils.getUserAgent());
    AssetUtils.setContext(this);
  }

  protected void initSamsungClipboardManager() {
    // Workaround for a leak in Samsung devices
    // https://gist.github.com/pepyakin/8d2221501fd572d4a61c
    try {
      Log.i(this.getClass().getName(), "Initializing Samsung ClipboardUIManager with ApplicationContext");
      Class<?> cls = Class.forName("android.sec.clipboard.ClipboardUIManager");
      Method m = cls.getDeclaredMethod("getInstance", Context.class);
      Object o = m.invoke(null, this);
    } catch (Exception ignored) { }
  }

  protected void initCrashlytics() {
    CrashUtils.initialize(this);
  }

}
