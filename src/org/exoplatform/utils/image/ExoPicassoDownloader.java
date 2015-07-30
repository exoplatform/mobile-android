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

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.ExoConnectionUtils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.UrlConnectionDownloader;

/**
 * Created by The eXo Platform SAS<br/>
 * A custom {@link Picasso} {@link UrlConnectionDownloader} that:
 * <ul>
 * <li>uses the cookies synchronized from {@link ExoConnectionUtils} to get permissions/restrictions about targeted resources</li>
 * <li>sets the custom eXo/$version (Android) User-Agent header in the request</li>
 * </ul>
 * 
 * @author Philippe Aristote paristote@exoplatform.com May 13, 2015
 */
public class ExoPicassoDownloader extends UrlConnectionDownloader {

  private static final String TAG = "eXo___CookiesAwarePicassoDownloader___";

  // private static final String RESPONSE_SOURCE = "X-Android-Response-Source";

  public ExoPicassoDownloader(Context context) {
    super(context);
  }

  /**
   * Creates a new CookieManager if none already exists and sets it as the
   * default CookieHandler
   * 
   * @return the CookieManager newly created, or the existing one if it's
   *         already the default CookieHandler
   */
  private CookieManager initCookieManager() {
    CookieHandler handler = CookieHandler.getDefault();
    CookieManager manager;
    if (handler == null || !(handler instanceof CookieManager)) {
      manager = new CookieManager();
      CookieHandler.setDefault(manager);
      // Sync cookies from ExoConnectionUtils only
      // when the Cookies Manager is created
      syncCookies(manager);
    } else {
      manager = (CookieManager) handler;
    }
    return manager;
  }

  /**
   * Syncs all cookies from ExoConnectionUtils cookieStore from Apache's
   * HttpClient to HttpURLConnection.
   * 
   * @param manager the CookieManager in which to store the retrieved cookies
   */
  private void syncCookies(CookieManager manager) {
    CookieStore store = ExoConnectionUtils.cookiesStore;
    if (store == null)
      return;

    for (Cookie cookie : store.getCookies()) {
      HttpCookie c = new HttpCookie(cookie.getName(), cookie.getValue());
      c.setDomain(cookie.getDomain());
      c.setPath(cookie.getPath());
      c.setVersion(cookie.getVersion());
      String url = AccountSetting.getInstance().getDomainName() + "/" + cookie.getPath();
      try {
        manager.getCookieStore().add(new URI(url), c);
      } catch (URISyntaxException e) {
        Log.e(TAG, e.getMessage(), e);
      }
    }
  }

  /**
   * Creates a Http Connection which contains any existing cookie
   * 
   * @param path The URL to connect to
   * @return the HttpURLConnection
   * @throws IOException
   */
  private HttpURLConnection connection(Uri path) throws IOException {
    HttpURLConnection connection = super.openConnection(path);
    ExoConnectionUtils.setUserAgent(connection);
    initCookieManager();
    return connection;
  }

  @Override
  public Response load(Uri uri, int networkPolicy) throws IOException {
    // TODO use networkPolicy as in com.squareup.picasso.UrlConnectionDownloader
    // https://github.com/square/picasso/blob/picasso-parent-2.5.2/
    // picasso/src/main/java/com/squareup/picasso/UrlConnectionDownloader.java
    HttpURLConnection connection = connection(uri);
    connection.setUseCaches(true);

    int responseCode = connection.getResponseCode();
    if (responseCode >= 300) {
      connection.disconnect();
      throw new ResponseException(responseCode + " " + connection.getResponseMessage(), networkPolicy, responseCode);
    }

    long contentLength = connection.getHeaderFieldInt("Content-Length", -1);
    // boolean fromCache =
    // parseResponseSourceHeader(connection.getHeaderField(RESPONSE_SOURCE));
    boolean fromCache = false;

    return new Response(connection.getInputStream(), fromCache, contentLength);
  }

}
