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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.ui.login.tasks.LogoutTask;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

//interact with server
public class ExoConnectionUtils {

  public static final int         LOGIN_WRONG              = 0;

  public static final int         LOGIN_SUSCESS            = 1;

  public static final int         LOGIN_UNAUTHORIZED       = 2;

  public static final int         LOGIN_INVALID            = 3;

  public static final int         LOGIN_FAILED             = 4;

  public static final int         LOGIN_INCOMPATIBLE       = 5;

  public static final int         LOGIN_SERVER_RESUMING    = 6;

  // Default connection and socket timeout of 30 seconds. Tweak to taste.
  public static final int         SOCKET_OPERATION_TIMEOUT  = 30 * 1000;

  public static DefaultHttpClient httpClient;

  public static CookieStore       cookiesStore;

  public static final int         SIGNUP_OK                = 10;

  /** internal server problem, strange response status code */
  public static final int         SIGNUP_INVALID           = 11;

  /** domain for the email is invalid, such as gmail, yahoo ... */
  public static final int         SIGNUP_WRONG_DOMAIN      = 12;

  /** an account already exists for this email */
  public static final int         SIGNUP_ACCOUNT_EXISTS    = 13;

  /** can not connect server, probably down or wrong address */
  public static final int         SIGNUP_SERVER_NAV        = 14;

  /** maximum number of users for the tenant has been reached */
  public static final int         SIGNUP_MAX_USERS         = 15;

  private static final String     SIGNUP_MAX_USERS_MSG     = "The request to create or join a workspace from ";

  public static final int         SIGNIN_OK                = 20;

  public static final int         SIGNIN_INVALID           = 21;

  public static final int         SIGNIN_NO_ACCOUNT        = 22;

  /** cloud can not find tenant for this email */
  public static final int         SIGNIN_NO_TENANT_FOR_EMAIL  = 23;

  /** like SIGNUP_SERVER_NAV */
  public static final int         SIGNIN_SERVER_NAV       = 24;

  public static final int         SIGNIN_SERVER_ONLINE    = 25;

  public static final int         SIGNIN_SERVER_SUSPENDED = 26;

  public static final int         SIGNIN_CONNECTION_ERR   = 27;

  public  static final int        TENANT_OK               = 30;

  /**=== Tenant status ===*/
  public static final String      ONLINE                  = "ONLINE";

  public static final String      STOPPED                 = "STOPPED";



  public static final String      HTTP                    = "http://";

  public static final String      HTTPS                   = "https://";

  /** eXo cloud workspace url */
  public static final String      EXO_CLOUD_WS_DOMAIN      = "exoplatform.net"; // "wks-acc.exoplatform.org";  // "netstg.exoplatform.org";

  /** eXo cloud base service url */
  public static final String      SERVICE_BASE_URL         = "/rest/cloud-admin/cloudworkspaces/tenant-service";

  public static final String      MARKETO_URL              = "learn.exoplatform.com/index.php/leadCapture/save";

  private static final String     TAG                      = "ExoConnectionUtils";

  /**
   * Check mobile network and wireless status
   */
  public static boolean isNetworkAvailableExt(Context paramContext) {
    ConnectivityManager localConnectivityManager = (ConnectivityManager) paramContext.getSystemService("connectivity");
    if (localConnectivityManager == null) {
      return false;
    }
    while (true) {
      //
      NetworkInfo localNetworkInfo = localConnectivityManager.getActiveNetworkInfo();
      if ((localNetworkInfo == null)
          || (localNetworkInfo.getState() != NetworkInfo.State.CONNECTED))
        return false;
      if (localNetworkInfo.getType() == 1) {
        return true;
      }
      if (localNetworkInfo.getType() == 0) {
        return true;
      }
      return true;
    }
  }

  // Convert stream to String
  public static String convertStreamToString(InputStream is) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();

    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
    } catch (IOException e) {
      return null;
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        return null;
      }
    }
    return sb.toString();
  }

  /*
   * check session timeout
   */

  public static int checkTimeout(String url) {
    HttpGet httpGet = new HttpGet(url);
    try {
      if (httpClient == null) {
        httpClient = initHttpClient();
      }
      HttpResponse response = httpClient.execute(httpGet);
      int statusCode = checkPlatformRespose(response);
      if (statusCode == LOGIN_SUSCESS) {
        return LOGIN_SUSCESS;
      } else {
        String username = AccountSetting.getInstance().getUsername();
        String password = AccountSetting.getInstance().getPassword();
        StringBuilder buffer = new StringBuilder(username);
        buffer.append(":");
        buffer.append(password);
        httpGet.setHeader("Authorization",
                          "Basic " + Base64.encodeBytes(buffer.toString().getBytes()));
        response = httpClient.execute(httpGet);
        cookiesStore = httpClient.getCookieStore();
        AccountSetting.getInstance().cookiesList = getCookieList(cookiesStore);
        return checkPlatformRespose(response);
      }

    } catch (IOException e) {
      return LOGIN_WRONG;
    } catch (IllegalStateException ise) {
      return LOGIN_INVALID;
    }

  }

  public static DefaultHttpClient initHttpClient() {
    HttpParams httpParameters = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParameters, SOCKET_OPERATION_TIMEOUT);
    HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_OPERATION_TIMEOUT);
    HttpConnectionParams.setTcpNoDelay(httpParameters, true);

    return new DefaultHttpClient(httpParameters);
  }

  public static HttpResponse getRequestResponse(String strUrlRequest) throws IOException {
    HttpGet httpGet = new HttpGet(strUrlRequest);
    if (httpClient == null) {
      httpClient = initHttpClient();
    }

    return httpClient.execute(httpGet);
  }

  // Get input stream from url
  public static InputStream sendRequest(HttpResponse response) {
    try {
      HttpEntity entity;
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
        entity = response.getEntity();
        if (entity != null) {
          return entity.getContent();
        }
      } else {
        return null;
      }

    } catch (IOException e) {
      return null;
    }
    return null;
  }

  /*
   * Get response from platform url
   */
  public static HttpResponse getPlatformResponse(String username,
                                                 String password,
                                                 String strUrlRequest) throws IOException {
    if (httpClient == null) {
      httpClient = initHttpClient();
    }
    StringBuilder buffer = new StringBuilder(username);
    buffer.append(":");
    buffer.append(password);
    HttpGet httpGet = new HttpGet(strUrlRequest);
    httpGet.setHeader("Authorization", "Basic " + Base64.encodeBytes(buffer.toString().getBytes()));
    HttpResponse response = httpClient.execute(httpGet);
    cookiesStore = httpClient.getCookieStore();
    AccountSetting.getInstance().cookiesList = getCookieList(cookiesStore);

    return response;
  }

  /**
   * Make a Sign up request to eXo cloud
   *
   * @param email
   */
  public static HttpResponse makeCloudSignUpRequest(String email) throws IOException {
    Log.d(TAG, "make cloud sign up request with " + email);
    if (httpClient == null) {
      httpClient = initHttpClient();
    }

    HttpPost httpPost = new HttpPost(HTTP + EXO_CLOUD_WS_DOMAIN + SERVICE_BASE_URL + "/signup");
    List<NameValuePair> requestParameters = new ArrayList<NameValuePair>(1);
    requestParameters.add(new BasicNameValuePair("user-mail", email));
    httpPost.setEntity(new UrlEncodedFormEntity(requestParameters));
    return httpClient.execute(httpPost);
  }

  public static int checkSignUpResponse(HttpResponse response, String email) {
    int statusCode = response.getStatusLine().getStatusCode();
    Log.d(TAG, "status: " + statusCode);
    String message = getPLFStream(response);
    /* code 309 */
    if (statusCode == ExoConstants.UNKNOWN) {
      if (response.getLastHeader("Location").getValue().contains("tryagain.jsp"))
        return ExoConnectionUtils.SIGNUP_WRONG_DOMAIN;
      else return ExoConnectionUtils.SIGNUP_ACCOUNT_EXISTS;
    }
    /* code 202 */  // TODO: check CLDINT-1197 if any change to response code
    else if (statusCode == HttpStatus.SC_ACCEPTED
        || (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
            && message!= null && message.startsWith(SIGNUP_MAX_USERS_MSG + email)))
      return ExoConnectionUtils.SIGNUP_MAX_USERS;

    if (statusCode != HttpStatus.SC_OK)
      return ExoConnectionUtils.SIGNUP_SERVER_NAV;

    /* code 200 */
    return ExoConnectionUtils.SIGNUP_OK;
  }

  /**
   * Request tenant and username for an email
   *
   * @param email
   * @return
   * @throws IOException
   */
  public static HttpResponse requestTenantForEmail(String email) throws IOException {
    return getRequestResponse(HTTP + EXO_CLOUD_WS_DOMAIN + SERVICE_BASE_URL + "/usermailinfo/" + email);
  }

  public static String[] checkRequestTenant(HttpResponse response) {

    String[] results = new String[2];
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) return null;

    try {
      String result   = getPLFStream(response);
      JSONObject json = (JSONObject) JSONValue.parse(result);
      results[0]      = json.get(ExoConstants.USERNAME).toString();
      results[1]      = json.get(ExoConstants.TENANT).toString();
      Log.d(TAG, "user:   " + results[0] + " - tenant: " + results[1]);
      return results;
    } catch (RuntimeException e) {
      Log.d(TAG, "RuntimeException: " + e.getLocalizedMessage());
      return null;
    }
  }

  /**
   * Requesting if username exists within a cloud server
   *
   * @param user
   * @param tenant
   * @return
   */
  public static boolean requestAccountExistsForUser(String user, String tenant) {
    String url = HTTP + EXO_CLOUD_WS_DOMAIN + SERVICE_BASE_URL + "/isuserexist/" + tenant + "/" + user;
    try {
      HttpResponse response = getRequestResponse(url);
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) return false;

      return convertStreamToString(response.getEntity().getContent())
          .replace("\n", "").replace("\r", "").replace("\r\n", "")
          .equalsIgnoreCase("true");
    } catch (IOException e) {
      Log.d(TAG, "IOException: " + e.getLocalizedMessage());
      return false;
    }
  }

  /**
   * Check status of tenant
   *
   * @param tenant
   * @return
   */
  public static int requestTenantStatus(String tenant) {
    String url = HTTP + EXO_CLOUD_WS_DOMAIN + SERVICE_BASE_URL + "/status/" + tenant;
    try {
      HttpResponse response = getRequestResponse(url);
      /** 404 - tenant does not exist */
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_NOT_FOUND) return SIGNIN_NO_TENANT_FOR_EMAIL;
      if (statusCode != HttpStatus.SC_OK)        return SIGNIN_SERVER_NAV;

      /** 200 - tenant exists - check status */
      if (response.getEntity() != null) {
        String tenantStatus = convertStreamToString(response.getEntity().getContent())
            .replace("\n", "").replace("\r", "").replace("\r\n", "");

        Log.d(TAG, "tenant status: " + tenantStatus);
        if      (tenantStatus.equalsIgnoreCase(ONLINE))  return SIGNIN_SERVER_ONLINE;
        else if (tenantStatus.equalsIgnoreCase(STOPPED)) return SIGNIN_SERVER_SUSPENDED;

        return LOGIN_SERVER_RESUMING;
      }

      return SIGNIN_SERVER_NAV;
    } catch (IOException e) {
      Log.d(TAG, "IOException: " + e.getLocalizedMessage());
      return SIGNIN_SERVER_NAV;
    }
  }

  /**
   * Create marketo lead
   */
  public static HttpResponse requestCreatingMarketo(String email) throws IOException {
    int idx1 = email.indexOf("@");
    int idx2 = email.lastIndexOf(".");
    String tenant = (idx1 > 0 && idx2 > 2) ? email.substring(idx1 + 1, idx2) : "";
    HttpPost httpPost = new HttpPost(HTTP + MARKETO_URL);
    List<NameValuePair> requestParameters = new ArrayList<NameValuePair>(1);
    requestParameters.add(new BasicNameValuePair("Email", email));
    requestParameters.add(new BasicNameValuePair("eXo_Cloud_Tenant_Name__c", tenant));
    requestParameters.add(new BasicNameValuePair("lpId" , "1967"));
    requestParameters.add(new BasicNameValuePair("subId", "46"));
    requestParameters.add(new BasicNameValuePair("munchkinId", "577-PCT-880"));
    requestParameters.add(new BasicNameValuePair("formid", "1167"));
    requestParameters.add(new BasicNameValuePair("returnLPId", "-1"));
    httpPost.setEntity(new UrlEncodedFormEntity(requestParameters));
    return httpClient.execute(httpPost);
  }


  /**
   * Checking the response status code
   */
  public static int checkPlatformRespose(HttpResponse response) {
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
      return LOGIN_SUSCESS;
    } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
      return LOGIN_UNAUTHORIZED;
    } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
      return LOGIN_INVALID;
    } else if (statusCode == ExoConstants.UNKNOWN) {
      /* 309 code - server is resuming */
      return LOGIN_SERVER_RESUMING;
    } else
      return LOGIN_FAILED;
  }

  // get input stream from URL without authentication
  public static InputStream sendRequestWithoutAuthen(HttpResponse response) {
    InputStream ipstr = null;
    try {
      HttpEntity entity;
      entity = response.getEntity();
      if (entity != null) {
        ipstr = entity.getContent();
      }
    } catch (ClientProtocolException e) {
      e.getMessage();
    } catch (IOException e) {
      e.getMessage();
    }
    return ipstr;
  }

  // Get string input stream from URL
  public static String sendRequestAndReturnString(HttpResponse response) {
    return convertStreamToString(sendRequest(response));
  }

  // get the JSONObject string of PLF
  private static String getPLFStream(HttpResponse response) {
    return convertStreamToString(sendRequestWithoutAuthen(response));
  }

  /*
   * Check the version of PLF is mobile compatible or not
   */
  public static boolean checkPLFVersion(HttpResponse response) {
    try {

      String result = getPLFStream(response);
      JSONObject json = (JSONObject) JSONValue.parse(result);

      String isComplicant = json.get(ExoConstants.IS_MOBILE_COMPLIANT).toString();
      if ("true".equalsIgnoreCase(isComplicant)) {
        String editionObject = json.get(ExoConstants.PLATFORM_EDITION).toString();
        ServerSettingHelper.getInstance().setServerEdition(editionObject);
        String verObject = json.get(ExoConstants.PLATFORM_VERSION).toString();
        ServerSettingHelper.getInstance().setServerVersion(verObject);

        /*
         * Get repository name
         */
        String repository = ExoConstants.DOCUMENT_REPOSITORY;
        if (json.containsKey(ExoConstants.PLATFORM_CURRENT_REPO_NAME)) {
          repository = json.get(ExoConstants.PLATFORM_CURRENT_REPO_NAME).toString();
          if (repository == null || "".equals(repository.trim())) {
            repository = ExoConstants.DOCUMENT_REPOSITORY;
          }
        }
        DocumentHelper.getInstance().repository = repository;
        return true;
      } else
        return false;
    } catch (RuntimeException e) {
      return false;
    }

  }

  public static ArrayList<String> getCookieList(CookieStore cookieStore) {
    ArrayList<String> cookieList = new ArrayList<String>();
    List<Cookie> cookies = cookieStore.getCookies();
    String strCookie = "";
    if (!cookies.isEmpty()) {
      for (int i = 0; i < cookies.size(); i++) {
        strCookie = cookies.get(i).getName().toString() + "="
            + cookies.get(i).getValue().toString();
        cookieList.add(strCookie);
      }
    }
    return cookieList;
  }

  public static void setCookieStore(CookieStore cookieStore, ArrayList<String> list) {
    cookieStore = new BasicCookieStore();
    for (String cookieStr : list) {
      String[] keyValue = cookieStr.split("=");
      String key = keyValue[0];
      String value = "";
      if (keyValue.length > 1)
        value = keyValue[1];
      cookieStore.addCookie(new BasicClientCookie(key, value));
    }
  }

  /**
   * Clean up connection data to log out
   */
  public static void loggingOut() {
    if (ExoConnectionUtils.httpClient != null) {
      new LogoutTask(ExoConnectionUtils.httpClient).execute();
      ExoConnectionUtils.httpClient = null;
    }
    AccountSetting.getInstance().cookiesList = null;

    /** Clear all social service data */
    SocialServiceHelper.getInstance().clearData();
  }
  
}
