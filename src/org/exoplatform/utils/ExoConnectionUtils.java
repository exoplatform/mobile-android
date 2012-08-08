package org.exoplatform.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

//import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//import android.util.Base64;

//interact with server
public class ExoConnectionUtils {

  public static final int         LOGIN_WRONG              = 0;

  public static final int         LOGIN_SUSCESS            = 1;

  public static final int         LOGIN_UNAUTHORIZED       = 2;

  public static final int         LOGIN_INVALID            = 3;

  public static final int         LOGIN_FAILED             = 4;

  // Default connection and socket timeout of 60 seconds. Tweak to taste.
  private static final int        SOCKET_OPERATION_TIMEOUT = 30 * 1000;

  public static DefaultHttpClient httpClient;

  public static CookieStore       cookiesStore;

  /*
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

    HttpResponse response = httpClient.execute(httpGet);
    return response;
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

  /*
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
      if (isComplicant.equalsIgnoreCase("true")) {
        String editionObject = json.get(ExoConstants.PLATFORM_EDITION).toString();
        ServerSettingHelper.getInstance().setServerEdition(editionObject);
        String verObject = json.get(ExoConstants.PLATFORM_VERSION).toString();
        ServerSettingHelper.getInstance().setServerVersion(verObject);
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

}
