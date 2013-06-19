/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import android.util.Log;
import android.webkit.URLUtil;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Aug
 * 25, 2011
 */
public class URLAnalyzer {

  public URLAnalyzer() {

  }

  public static boolean isValidUrl(String urlStr) {
    return URLUtil.isValidUrl(urlStr);
  }

  public static boolean isUrlValid(String str) {
    try {
      URL url = new URL(str.replaceAll(" ", "%20"));
      url.toURI();
      return true;
    } catch (MalformedURLException e) {
      return false;
    } catch (URISyntaxException e) {
      return false;
    }
  }

  public String parserURL(String urlStr) {

    if (urlStr == null || urlStr.length() == 0)
      return "";

    String url = urlStr = urlStr.toLowerCase(Locale.US);

    boolean isHTTPSUrl = false;

    try {

      int indexOfProtocol;

      indexOfProtocol = urlStr.indexOf(ExoConstants.HTTP_PROTOCOL);
      if (indexOfProtocol == 0)
        url = urlStr.substring(ExoConstants.HTTP_PROTOCOL.length() + 3);

      indexOfProtocol = urlStr.indexOf(ExoConstants.HTTPS_PROTOCOL);
      if (indexOfProtocol == 0) {
        url = urlStr.substring(ExoConstants.HTTPS_PROTOCOL.length() + 3);
        isHTTPSUrl = true;
      }

      while (url.charAt(0) == '/') {
        if (url.length() > 1)
          url = url.substring(1);
        else
          url = null;
      }

    } catch (Exception e) {
      url = null;
    }

    try {

      URI uri;
      StringBuffer urlBuffer = new StringBuffer();

      if (!(isHTTPSUrl)) {
        urlBuffer.append(ExoConstants.HTTP_PROTOCOL);
        urlBuffer.append("://");
        urlBuffer.append(url);
        uri = new URI(ExoConstants.HTTP_PROTOCOL + urlBuffer.toString());
        urlBuffer = new StringBuffer();
        urlBuffer.append(ExoConstants.HTTP_PROTOCOL);
        urlBuffer.append("://");
        urlBuffer.append(uri.getHost());
      } else {
        urlBuffer.append(ExoConstants.HTTPS_PROTOCOL);
        urlBuffer.append("://");
        urlBuffer.append(url);
        uri = new URI(ExoConstants.HTTPS_PROTOCOL + urlBuffer.toString());
        urlBuffer = new StringBuffer();
        urlBuffer.append(ExoConstants.HTTPS_PROTOCOL);
        urlBuffer.append("://");
        urlBuffer.append(uri.getHost());
      }

      int port = uri.getPort();
      if (port > 0) {
        urlBuffer.append(":");
        urlBuffer.append(port);
      }
      url = urlBuffer.toString();
    } catch (URISyntaxException e) {
      if (Log.isLoggable("URISyntaxException", Log.ERROR)) {
        String msg = e.getMessage();
        String reason = e.getReason();
        Log.e("URISyntaxException", msg + "  " + reason);

      }

      url = null;
    }

    return url;
  }

  public static String encodeUrl(String urlString) {

    try {

      URL url = new URL(urlString);
      URI uri = new URI(url.getProtocol(),
                        url.getUserInfo(),
                        url.getHost(),
                        url.getPort(),
                        url.getPath(),
                        url.getQuery(),
                        url.getRef());

      return uri.toASCIIString();

    } catch (MalformedURLException e) {
      return null;
    } catch (URISyntaxException e) {
      return null;
    }

  }
}
