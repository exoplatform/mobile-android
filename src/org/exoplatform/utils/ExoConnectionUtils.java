package org.exoplatform.utils;

import greendroid.util.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
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
import android.util.Log;

//interact with server
public class ExoConnectionUtils {

  public static DefaultHttpClient httpClient;

  public static HttpURLConnection con;

  private static String           domainUrl;

  private static int              splitLinesAt = 76;

  public static List<Cookie>      _sessionCookies;      // Cookie array

  public static String            _strCookie   = "";    // Cookie string

  private static String           _strFirstLoginContent; // String data for the
                                                         // first
                                                         // time

  // login
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

  private static byte[] zeroPad(int length, byte[] bytes) {
    byte[] padded = new byte[length]; // initialized to zero by JVM
    System.arraycopy(bytes, 0, padded, 0, bytes.length);
    return padded;
  }

  // Get string data for the first time login
  public static String getFirstLoginContent() {
    return _strFirstLoginContent;
  }

  private static String splitLines(String string) {
    StringBuffer lineBuffer = new StringBuffer();
    for (int i = 0; i < string.length(); i += splitLinesAt) {

      lineBuffer.append(string.substring(i, Math.min(string.length(), i + splitLinesAt)));
      lineBuffer.append("\r\n");
    }
    return lineBuffer.toString();

  }

  // Encode String to Base64String
  public static String stringEncodedWithBase64(String str) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    buffer.append("abcdefghijklmnopqrstuvwxyz");
    buffer.append("0123456789");
    buffer.append("+/");
    String base64code = buffer.toString();
    String encoded = "";

    byte[] stringArray;
    try {
      // use appropriate encoding string!
      stringArray = str.getBytes("UTF-8");
    } catch (UnsupportedEncodingException ignored) {
      // use local default rather than croak
      stringArray = str.getBytes();
    }
    // determine how many padding bytes to add to the output
    int paddingCount = (3 - (stringArray.length % 3)) % 3;
    // add any necessary padding to the input
    stringArray = zeroPad(stringArray.length + paddingCount, stringArray);
    // process 3 bytes at a time, churning out 4 output bytes
    // worry about CRLF insertions later
    StringBuffer encodedBuffer = new StringBuffer();
    for (int i = 0; i < stringArray.length; i += 3) {
      int j = ((stringArray[i] & 0xff) << 16) + ((stringArray[i + 1] & 0xff) << 8)
          + (stringArray[i + 2] & 0xff);
      encodedBuffer.append(base64code.charAt((j >> 18) & 0x3f));
      encodedBuffer.append(base64code.charAt((j >> 12) & 0x3f));
      encodedBuffer.append(base64code.charAt((j >> 6) & 0x3f));
      encodedBuffer.append(base64code.charAt(j & 0x3f));
    }
    encoded = encodedBuffer.toString();
    // replace encoded padding nulls with "="
    return splitLines(encoded.substring(0, encoded.length() - paddingCount)
        + "==".substring(0, paddingCount));
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
    // System.out.println("convertStreamToString "+sb.toString());
    return sb.toString();
  }

  // Send request with authentication
  public static String sendAuthentication(String domain, String username, String password) {

    try {
      HttpResponse response;
      HttpEntity entity;
      CookieStore cookiesStore;
      String strCookie = "";

      String redirectStr = domain.concat(ExoConstants.DOMAIN_SUFFIX);

      HttpParams httpParameters = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
      HttpConnectionParams.setSoTimeout(httpParameters, 30000);
      HttpConnectionParams.setTcpNoDelay(httpParameters, true);

      httpClient = new DefaultHttpClient(httpParameters);

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

      int indexOfPrivate = redirectStr.indexOf("/classic");

      // Request to login
      String loginStr;
      if (indexOfPrivate > 0)
        loginStr = redirectStr.substring(0, indexOfPrivate).concat("/j_security_check");
      else
        loginStr = redirectStr.concat("/j_security_check");
      domainUrl = loginStr;
      HttpPost httpPost = new HttpPost(loginStr);
      List<NameValuePair> nvps = new ArrayList<NameValuePair>(2);
      nvps.add(new BasicNameValuePair("j_username", username));
      nvps.add(new BasicNameValuePair("j_password", password));
      httpPost.setEntity(new UrlEncodedFormEntity(nvps));
      httpPost.setHeader("Cookie", strCookie);
      _strCookie = strCookie;
      response = httpClient.execute(httpPost);
      entity = response.getEntity();
      _sessionCookies = new ArrayList<Cookie>(cookies);

      if (entity != null) {
        InputStream instream = entity.getContent();
        _strFirstLoginContent = convertStreamToString(instream);
        if (_strFirstLoginContent.contains("Sign in failed. Wrong username or password.")) {
          return ExoConstants.LOGIN_NO;
        } else if (_strFirstLoginContent.contains("error', '/main?url")) {
          _strFirstLoginContent = null;
          return "ERROR";
        } else if (_strFirstLoginContent.contains("eXo.env.portal")) {
          return ExoConstants.LOGIN_YES;
        } else {
          return ExoConstants.LOGIN_INVALID;
        }

      } else {
        return null;
      }
      // httpClient.getConnectionManager().shutdown();
    } catch (IOException e) {
      String error = e.getMessage();
      if (error != null && (error.contains("No route"))) {
        return ExoConstants.LOGIN_UNREACHABLE;
      } else
        return null;
    }

  }

  // Send request with authentication
  public static String authorizationHeader(String username, String password) {
    String s = "Basic ";
    String strAuthor = s + stringEncodedWithBase64(username + ":" + password);
    return strAuthor.substring(0, strAuthor.length() - 2);
  }

  // re authenticate when login session timeout
  public static void reAuthenticate() {
    try {
      if (httpClient == null) {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
        HttpConnectionParams.setSoTimeout(httpParameters, 30000);
        HttpConnectionParams.setTcpNoDelay(httpParameters, true);
        httpClient = new DefaultHttpClient(httpParameters);
      }
      String strUserName = AccountSetting.getInstance().getUsername();
      String strPassword = AccountSetting.getInstance().getPassword();
      HttpPost httpPost = new HttpPost(domainUrl);
      List<NameValuePair> nvps = new ArrayList<NameValuePair>(2);
      nvps.add(new BasicNameValuePair("j_username", strUserName));
      nvps.add(new BasicNameValuePair("j_password", strPassword));
      httpPost.setEntity(new UrlEncodedFormEntity(nvps));
      httpClient.execute(httpPost);
    } catch (IOException e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("Exception", "Cannot reauthenticate");
    }
    httpClient.getCredentialsProvider().setCredentials(AccountSetting.getInstance().getAuthScope(),
                                                       AccountSetting.getInstance()
                                                                     .getCredentials());
  }

  // Get input stream from URL with authentication
  public static InputStream sendRequestWithAuthorization(String urlStr) {

    InputStream ipstr = null;
    try {
      String strUserName = AccountSetting.getInstance().getUsername();
      String strPassword = AccountSetting.getInstance().getPassword();

      URL url = new URL(urlStr);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setConnectTimeout(30000);
      // set up url connection to get retrieve information back
      con.setRequestMethod("GET");
      con.setDoInput(true);

      // stuff the Authorization request header

      con.setRequestProperty("Authorization", authorizationHeader(strUserName, strPassword));

      // pull the information back from the URL
      ipstr = con.getInputStream();

    } catch (ClientProtocolException e) {
      e.getMessage();
    } catch (IOException e) {
      e.getMessage();
    }

    return ipstr;
  }

  // public static InputStream getDriveContent(String url) {
  //
  // try {
  // HttpGet get = new HttpGet(url);
  // get.setHeader("Cookie", _strCookie);
  // httpClient.getCredentialsProvider().setCredentials(AccountSetting.getInstance()
  // .getAuthScope(),
  // AccountSetting.getInstance()
  // .getCredentials());
  // HttpResponse response;
  // response = httpClient.execute(get);
  // int status = response.getStatusLine().getStatusCode();
  // if (status >= 200 && status < 300) {
  // HttpEntity entity = response.getEntity();
  // if (entity != null) {
  // InputStream instream = entity.getContent();
  // // String strResult = convertStreamToString(instream);
  // return instream;
  // } else {
  // return null;
  // }
  // } else {
  // return null;
  // }
  //
  // } catch (Exception e) {
  // Log.e(e.toString(), e.getMessage());
  // return null;
  // }
  //
  // }

  // Get input stream from URL
  public static InputStream sendRequest(String strUrlRequest) {
    try {
      HttpResponse response;
      HttpEntity entity;
      httpClient.getCredentialsProvider().setCredentials(AccountSetting.getInstance()
                                                                       .getAuthScope(),
                                                         AccountSetting.getInstance()
                                                                       .getCredentials());
      HttpGet httpGet = new HttpGet(strUrlRequest);
      httpGet.setHeader("Cookie", _strCookie);
      response = httpClient.execute(httpGet);
      entity = response.getEntity();
      if (entity != null) {
        return entity.getContent();
      }

    } catch (IOException e) {
      return null;
    }
    return null;
  }

  // get input stream from URL without authentication
  public static InputStream sendRequestWithoutAuthen(String strUrlRequest) {
    InputStream ipstr = null;
    try {
      HttpResponse response;
      HttpEntity entity;
      HttpParams httpParameters = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
      HttpConnectionParams.setSoTimeout(httpParameters, 60000);
      HttpConnectionParams.setTcpNoDelay(httpParameters, true);

      DefaultHttpClient httpClientPlf = new DefaultHttpClient(httpParameters);
      HttpGet httpGet = new HttpGet(strUrlRequest);
      response = httpClientPlf.execute(httpGet);
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
  public static String sendRequestAndReturnString(String strUrlRequest) {
    return convertStreamToString(sendRequest(strUrlRequest));
  }

  // get the JSONObject string of PLF
  private static String getPLFStream(String url) {
    return convertStreamToString(sendRequestWithoutAuthen(url));
  }

  // create Authorization
  public static void createAuthorization(String url, int port, String userName, String password) {
    AuthScope auth = new AuthScope(url, port);
    AccountSetting.getInstance().setAuthScope(auth);
    UsernamePasswordCredentials credential = new UsernamePasswordCredentials(userName, password);
    AccountSetting.getInstance().setCredentials(credential);
  }

  /*
   * Check the version of PLF return true if version number is >= 3.5 else
   * return false
   */
  public static boolean checkPLFVersion() {
    try {
      String versionUrl = SocialActivityUtil.getDomain() + ExoConstants.DOMAIN_SUFFIX_VERSION;
      String result = getPLFStream(versionUrl);
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

}
