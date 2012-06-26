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
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

//interact with server
public class ExoConnectionUtils {
  // Default connection and socket timeout of 60 seconds. Tweak to taste.
  private static final int        SOCKET_OPERATION_TIMEOUT = 30 * 1000;

  public static DefaultHttpClient httpClient;

  public static CookieStore       cookiesStore;

  // String login data
  private static String           _strFirstLoginContent;

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
   * Call onPrepareLogin() method to login system again
   */

  public static boolean onReLogin() throws IOException {
    String domain = AccountSetting.getInstance().getDomainName();
    String username = AccountSetting.getInstance().getUsername();
    String password = AccountSetting.getInstance().getPassword();
    HttpResponse response = onPrepareLogin(domain, username, password);
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
      return true;
    } else
      return false;
  }

  /*
   * Init DefaultHttpClient
   */

  public static void initHttpClient() {
    HttpParams httpParameters = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParameters, SOCKET_OPERATION_TIMEOUT);
    HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_OPERATION_TIMEOUT);
    HttpConnectionParams.setTcpNoDelay(httpParameters, true);
    /*
     * Manage a pool of connection, avoid crash apps when we reuse the default
     * http client
     */
    SchemeRegistry registry = new SchemeRegistry();
    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
    ClientConnectionManager connman = new ThreadSafeClientConnManager(httpParameters, registry);

    httpClient = new DefaultHttpClient(connman, httpParameters);
  }

  /*
   * This method to get the login response and refresh cookies session
   */

  public static HttpResponse onPrepareLogin(String domain, String username, String password) throws IOException {
    HttpResponse response;
    String strCookie = "";
    StringBuffer domainBuffer = new StringBuffer(domain);
    domainBuffer.append(ExoConstants.DOMAIN_SUFFIX);

    String redirectStr = domainBuffer.toString();
    if (httpClient == null) {
      initHttpClient();
    }
    HttpGet httpGet = new HttpGet(redirectStr);
    response = httpClient.execute(httpGet);
    cookiesStore = httpClient.getCookieStore();
    List<Cookie> cookies = cookiesStore.getCookies();
    if (!cookies.isEmpty()) {
      for (int i = 0; i < cookies.size(); i++) {
        strCookie = cookies.get(i).getName().toString() + "="
            + cookies.get(i).getValue().toString();
      }
    }
    AccountSetting.getInstance().cookiesList = getCookieList(cookiesStore);
    int indexOfPrivate = redirectStr.indexOf("/classic");

    // Request to login
    if (indexOfPrivate > 0) {
      domainBuffer = new StringBuffer();
      domainBuffer.append(redirectStr.substring(0, indexOfPrivate));
    } else {
      domainBuffer = new StringBuffer();
      domainBuffer.append(redirectStr);
    }
    domainBuffer.append("/j_security_check");
    HttpPost httpPost = new HttpPost(domainBuffer.toString());
    List<NameValuePair> nvps = new ArrayList<NameValuePair>(2);
    nvps.add(new BasicNameValuePair("j_username", username));
    nvps.add(new BasicNameValuePair("j_password", password));
    httpPost.setEntity(new UrlEncodedFormEntity(nvps));
    httpPost.setHeader("Cookie", strCookie);
    response = httpClient.execute(httpPost);
    return response;
  }

  /*
   * Checking the login response content
   */
  public static String sendAuthentication(HttpResponse httpResponse) {
    try {
      HttpEntity entity;
      entity = httpResponse.getEntity();
      if (entity != null) {
        InputStream instream = entity.getContent();
        _strFirstLoginContent = convertStreamToString(instream);
        if (_strFirstLoginContent == null) {
          return null;
        } else {
          if (_strFirstLoginContent.contains("Sign in failed. Wrong username or password.")) {
            return ExoConstants.LOGIN_NO;
          } else if (_strFirstLoginContent.contains("error', '/main?url")) {
            _strFirstLoginContent = null;
            return ExoConstants.LOGIN_ERROR;
          } else if (_strFirstLoginContent.contains("eXo.env.portal")) {
            return ExoConstants.LOGIN_YES;
          } else {
            return ExoConstants.LOGIN_INVALID;
          }
        }

      } else {
        return null;
      }
    } catch (IOException e) {
      String error = e.getMessage();
      if (error != null && (error.contains("No route"))) {
        return ExoConstants.LOGIN_UNREACHABLE;
      } else
        return null;
    }

  }

  public static HttpResponse getRequestResponse(String strUrlRequest) throws IOException {
    HttpGet httpGet = new HttpGet(strUrlRequest);
    if (httpClient == null) {
      initHttpClient();
      if (cookiesStore == null) {
        setCookieStore(cookiesStore, AccountSetting.getInstance().cookiesList);
      }
      httpClient.setCookieStore(cookiesStore);
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

  // Checking the http status
  public static int getResponseCode(String url) throws IOException {
    if (url != null) {
      url = url.replaceAll(" ", "%20");
      HttpGet httpGet = new HttpGet(url);
      if (httpClient == null) {
        initHttpClient();
        if (cookiesStore == null) {
          setCookieStore(cookiesStore, AccountSetting.getInstance().cookiesList);
        }
        httpClient.setCookieStore(cookiesStore);
      }
      HttpResponse response = httpClient.execute(httpGet);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
        return 1;
      } else {
        return 0;
      }
    } else
      return -1;

  }

  public static HttpResponse getPlatformResponse(String strUrlRequest) {
    try {
      if (httpClient == null) {
        initHttpClient();
      }
      HttpGet httpGet = new HttpGet(strUrlRequest);
      HttpResponse response = httpClient.execute(httpGet);
      return response;

    } catch (IOException e) {
      return null;
    }

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
