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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import org.apache.http.ParseException;

import android.util.Patterns;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Jun 24, 2014  
 */
public class ExoUtils {
  
  public static final String[]    WRONG_CLOUD_URLS         = new String[] {
    "http://exoplatform.net", "http://wks-acc.exoplatform.org", "http://netstg.exoplatform.org" };

  /**
   * Validate an URL with pattern http://developer.android.com/reference/android/util/Patterns.html#WEB_URL or #IP_ADDRESS
   * @param url The URL to validate
   * @return true if the URL matches the pattern, false otherwise
   */
  public static boolean isUrlValid(String url) {
    return (url == null) ? false :
                                    (Patterns.WEB_URL.matcher(url).matches() ||
                                     Patterns.IP_ADDRESS.matcher(url).matches());
  }
  
  public static boolean isDocumentUrlValid(String str) {
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
  
  public static String encodeDocumentUrl(String urlString) {
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
  
  /**
   * Removes unnecessary parts of the given URL.<br/>
   * The returned URL has the format: http(s)://host(:port)
   * @param url The URL to reformat.
   * @return The reformated URL.
   */
  public static String stripUrl(String url) {
    String sUrl;
    try {
      ExoWebAddress webaddress = new ExoWebAddress(url);
      String scheme = webaddress.getScheme();
      String host = webaddress.getHost();
      int portNumber = webaddress.getPort();
      String port = "";
      if (portNumber != -1) {
          port = ":" + Integer.toString(portNumber);
      }
      sUrl = scheme + "://" + host + port;
    } catch (ParseException pe) {
      sUrl = null;
    }
    return sUrl;
  }
  
  /**
   * Verifies that a server name doesn't contain any special characters.
   * @param serverName the server name to verify
   * @return true if no special characters are found, false otherwise.
   */
  public static boolean isServerNameValid(String serverName) {
    return (serverName == null) ? false : ExoDocumentUtils.isContainSpecialChar(serverName, ExoConstants.SPECIAL_CHAR_NAME_SET);
  }
  
  /**
   * TODO Validate a username.
   * @param username
   * @return
   */
  public static boolean isUsernameValid(String username) {
    return false;
  }
  
  /** 
   * Validate an email address with http://developer.android.com/reference/android/util/Patterns.html#EMAIL_ADDRESS
   * @param email
   * @return true if the given email matches the pattern, false otherwise
   */
  public static boolean isEmailValid(String email) {
    return (email == null) ? false : Patterns.EMAIL_ADDRESS.matcher(email).matches();
  }
  
  /**
   * Check whether a given URL is forbidden or not.<br/>
   * URL cannot be one of "http://exoplatform.net", "http://wks-acc.exoplatform.org", "http://netstg.exoplatform.org"
   * @param url the URL to check
   * @return true if the URL is forbidden, false otherwise
   */
  public static boolean urlHasWrongTenant(String url) {
    return (url == null) ? false : // false if url is null
                           (Arrays.asList(WRONG_CLOUD_URLS).contains( // true if the given url is in the list
                                !url.startsWith("http://") ? "http://" + url : url) // add http:// to the url if it's missing
                                                                    );
  }
  
}
