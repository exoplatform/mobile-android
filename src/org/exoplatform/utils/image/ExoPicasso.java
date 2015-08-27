/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.utils.image;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;

import android.content.Context;
import android.util.Log;

import com.squareup.picasso.Picasso;

/**
 * Created by The eXo Platform SAS <br/>
 * Creates a {@link Picasso} instance that uses a
 * {@link ExoPicassoDownloader}
 * 
 * @author Philippe Aristote paristote@exoplatform.com <br/>
 *         May 13, 2015
 */
public class ExoPicasso {

  private static final String TAG = "eXo___ExoPicasso___";

  private static Picasso      sPicasso;

  /**
   * Creates a {@link Picasso} instance that uses a
   * {@link ExoPicassoDownloader}
   * 
   * @param ctx Context
   * @return the Picasso instance
   */
  public static Picasso picasso(Context ctx) {
    if (sPicasso == null) {
      Picasso.Builder b = new Picasso.Builder(ctx);
      b.downloader(new ExoPicassoDownloader(ctx));
      sPicasso = b.build();
    }
    return sPicasso;
  }

  /**
   * Shutdowns the Picasso instance, and sets it to null
   */
  public static void clear() {
    if (sPicasso != null) {
      try {
        clearDownloaderCookies();
        sPicasso.shutdown();
      } catch (UnsupportedOperationException e) {
        Log.e(TAG, e.getMessage(), e);
      }
      sPicasso = null;
    }
  }

  private static void clearDownloaderCookies() {
    CookieHandler handler = CookieHandler.getDefault();
    if (handler != null && handler instanceof CookieManager) {
      CookieStore cookies = ((CookieManager) handler).getCookieStore();
      cookies.removeAll();
      CookieHandler.setDefault(null);
    }
  }

}
