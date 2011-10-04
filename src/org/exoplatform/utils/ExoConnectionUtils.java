package org.exoplatform.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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

import android.util.Log;

//interact with server
public class ExoConnectionUtils {

  public static DefaultHttpClient httpClient;

  public static HttpURLConnection con;

  private static int              splitLinesAt = 76;

  public static List<Cookie>      _sessionCookies;      // Cookie array

  public static String            _strCookie   = "";    // Cookie string

  private static String           _strFirstLoginContent; // String data for the
                                                         // first
                                                         // time

  // login

  private static String           _fullDomainStr;       // Host

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
    String lines = "";
    for (int i = 0; i < string.length(); i += splitLinesAt) {
      lines += string.substring(i, Math.min(string.length(), i + splitLinesAt));
      lines += "\r\n";
    }
    return lines;

  }

  // Encode String to Base64String
  public static String stringEncodedWithBase64(String str) {
    String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789"
        + "+/";
    String encoded = "";
    byte[] stringArray;
    try {
      // use appropriate encoding string!
      stringArray = str.getBytes("UTF-8");
    } catch (Exception ignored) {
      // use local default rather than croak
      stringArray = str.getBytes();
    }
    // determine how many padding bytes to add to the output
    int paddingCount = (3 - (stringArray.length % 3)) % 3;
    // add any necessary padding to the input
    stringArray = zeroPad(stringArray.length + paddingCount, stringArray);
    // process 3 bytes at a time, churning out 4 output bytes
    // worry about CRLF insertions later
    for (int i = 0; i < stringArray.length; i += 3) {
      int j = ((stringArray[i] & 0xff) << 16) + ((stringArray[i + 1] & 0xff) << 8)
          + (stringArray[i + 2] & 0xff);

      encoded = encoded + base64code.charAt((j >> 18) & 0x3f) + base64code.charAt((j >> 12) & 0x3f)
          + base64code.charAt((j >> 6) & 0x3f) + base64code.charAt(j & 0x3f);
    }
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
      System.out.println("" + e.getMessage());
      // e.printStackTrace();
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    // System.out.println("convertStreamToString "+sb.toString());
    return sb.toString();
  }

  // Get sub URL path
  private static String getExtend(String domain) {

    return "/portal/private/intranet";

  }

  // Send request with authentication
  public static String sendAuthentication(String domain, String username, String password) {

    try {
      HttpResponse response;
      HttpEntity entity;
      CookieStore cookiesStore;
      String strCookie = "";

      _fullDomainStr = getExtend(domain);

      if (_fullDomainStr.equalsIgnoreCase("ERROR"))
        return "ERROR";

      String redirectStr = domain.concat(_fullDomainStr);

      HttpParams httpParameters = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
      HttpConnectionParams.setSoTimeout(httpParameters, 60000);
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
          return "NO";
        } else if (_strFirstLoginContent.contains("error', '/main?url")) {
          _strFirstLoginContent = null;
          return "ERROR";
        } else if (_strFirstLoginContent.contains("eXo.env.portal")) {
          return "YES";
        } else
          return null;
      } else {
        return null;
      }
      // httpClient.getConnectionManager().shutdown();
    } catch (Exception e) {
      return null;
    }

  }

  // Standalone gadget requset
  private String loginForStandaloneGadget(String domain, String username, String password) {
    try {
      HttpResponse response;
      HttpEntity entity;
      CookieStore cookiesStore;
      String strCookie = "";

      _fullDomainStr = getExtend(domain);

      if (_fullDomainStr.equalsIgnoreCase("ERROR"))
        return "ERROR";

      String redirectStr = domain.concat(_fullDomainStr);

      HttpParams httpParameters = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
      HttpConnectionParams.setSoTimeout(httpParameters, 60000);
      HttpConnectionParams.setTcpNoDelay(httpParameters, true);

      // DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

      HttpGet httpGet = new HttpGet(redirectStr);

      response = httpClient.execute(httpGet);

      cookiesStore = httpClient.getCookieStore();
      List<Cookie> cookies = cookiesStore.getCookies();

      if (!cookies.isEmpty()) {
        for (int i = 0; i < cookies.size(); i++) {
          strCookie = cookies.get(i).getName().toString() + "=";
          strCookie = strCookie + cookies.get(i).getValue().toString()
              + ";domain=mobile.demo.exoplatform.org";
        }
      }

      /*
       * int indexOfPrivate = redirectStr.indexOf("/classic"); //Request to
       * login String loginStr =
       * "http://mobile.demo.exoplatform.org/portal/login"; if(indexOfPrivate >
       * 0) loginStr = redirectStr.substring(0,
       * indexOfPrivate).concat("/j_security_check"); else loginStr =
       * redirectStr.concat("/j_security_check");
       */

      HttpPost httpPost = new HttpPost("http://mobile.demo.exoplatform.org/portal/login");
      List<NameValuePair> nvps = new ArrayList<NameValuePair>(2);
      nvps.add(new BasicNameValuePair("username", username));
      nvps.add(new BasicNameValuePair("password", password));
      httpPost.setEntity(new UrlEncodedFormEntity(nvps));
      httpPost.setHeader("Cookie", strCookie);
      _strCookie = strCookie;
      response = httpClient.execute(httpPost);

      cookiesStore = httpClient.getCookieStore();
      cookies = cookiesStore.getCookies();

      if (!cookies.isEmpty()) {
        for (int i = 0; i < cookies.size(); i++) {
          strCookie = cookies.get(i).getName().toString() + "="
              + cookies.get(i).getValue().toString();
        }
      }
      _sessionCookies = cookies;

      entity = response.getEntity();

      if (entity != null) {
        InputStream instream = entity.getContent();
        _strFirstLoginContent = convertStreamToString(instream);
        if (_strFirstLoginContent.contains("Sign in failed. Wrong username or password.")) {
          return "NO";
        } else if (_strFirstLoginContent.contains("error', '/main?url")) {
          _strFirstLoginContent = null;
          return "ERROR";
        } else if (_strFirstLoginContent.contains("eXo.env.portal")) {
          return "YES";
        }
      } else {
        return "ERROR";
      }

    } catch (ClientProtocolException e) {
      return e.getMessage();
    } catch (IOException e) {
      return e.getMessage();
    }

    return "ERROR";
  }

  // Normal gadget request
  public static String sendRequestToGetGadget(String urlStr, String username, String password) {
    try {
      HttpResponse response;
      HttpEntity entity;
      CookieStore cookiesStore;
      String strCookie = "";

      String strRedirectUrl = urlStr;
      // DefaultHttpClient httpClient = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet(strRedirectUrl);

      response = httpClient.execute(httpGet);
      cookiesStore = httpClient.getCookieStore();
      List<Cookie> cookies = cookiesStore.getCookies();

      if (!cookies.isEmpty()) {
        for (int i = 0; i < cookies.size(); i++) {
          strCookie = cookies.get(i).getName().toString() + "="
              + cookies.get(i).getValue().toString();
        }
      }

      String strMarked = "/classic";
      int r = strRedirectUrl.indexOf(strMarked);
      String strLoginUrl = "";
      if (r >= 0)
        strLoginUrl = urlStr.substring(0, r).concat("/j_security_check");
      else {
        strLoginUrl = urlStr.concat("/j_security_check");
      }

      HttpPost httpPost = new HttpPost(strLoginUrl);
      List<NameValuePair> nvps = new ArrayList<NameValuePair>(2);
      nvps.add(new BasicNameValuePair("j_username", username));
      nvps.add(new BasicNameValuePair("j_password", password));
      httpPost.setEntity(new UrlEncodedFormEntity(nvps));
      httpPost.setHeader("Cookie", strCookie);
      _strCookie = strCookie;
      response = httpClient.execute(httpPost);
      entity = response.getEntity();

      if (entity != null) {
        InputStream instream = entity.getContent();
        String strResult = convertStreamToString(instream);
        return strResult;
      } else {
        return "ERROR";
      }
    } catch (ClientProtocolException e) {
      return e.getMessage();
    } catch (IOException e) {
      return e.getMessage();
    }

  }

  // Send request with authentication
  public static String authorizationHeader(String username, String password) {
    String s = "Basic ";
    String strAuthor = s + stringEncodedWithBase64(username + ":" + password);
    return strAuthor.substring(0, strAuthor.length() - 2);
  }

  // Get input stream from URL with authentication
  private InputStream sendRequestWithAuthorization(String urlStr) {

    InputStream ipstr = null;
    try {
      String strUserName = AccountSetting.getInstance().getUsername();
      String strPassword = AccountSetting.getInstance().getPassword();

      urlStr = urlStr.replace(" ", "%20");

      URL url = new URL(urlStr);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      // set up url connection to get retrieve information back
      con.setRequestMethod("GET");
      con.setDoInput(true);

      // stuff the Authorization request header

      con.setRequestProperty("Authorization", authorizationHeader(strUserName, strPassword));

      // pull the information back from the URL
      ipstr = con.getInputStream();

      StringBuffer buf = new StringBuffer();

      int c;
      while ((c = ipstr.read()) != -1) {
        buf.append((char) c);
      }

      con.disconnect();

    } catch (ClientProtocolException e) {
      e.getMessage();
    } catch (IOException e) {
      e.getMessage();
    }

    return ipstr;
  }

  public static String getDriveContent(String url) {

    try {
      HttpGet get = new HttpGet(url);
      HttpResponse response;
      response = httpClient.execute(get);
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        InputStream instream = entity.getContent();
        String strResult = convertStreamToString(instream);
        return strResult;
      } else {
        return null;
      }

    } catch (Exception e) {
      return null;
    }

  }

  // Get input stream from URL
  private static InputStream sendRequest(String strUrlRequest) {
    InputStream ipstr = null;
    try {
      HttpResponse response;
      HttpEntity entity;
      // DefaultHttpClient httpClient = new DefaultHttpClient();
      httpClient.getCredentialsProvider().setCredentials(AccountSetting.getInstance()
                                                                       .getAuthScope(),
                                                         AccountSetting.getInstance()
                                                                       .getCredentials());
      HttpGet httpGet = new HttpGet(strUrlRequest);
      httpGet.setHeader("Cookie", _strCookie);
      response = httpClient.execute(httpGet);
      entity = response.getEntity();
      if (entity != null) {
        ipstr = entity.getContent();
      }
      // httpClient.getConnectionManager().shutdown();
    } catch (ClientProtocolException e) {
      e.getMessage();
    } catch (IOException e) {
      e.getMessage();
    }
    return ipstr;
  }
//get input stream from URL without authentication
  private static InputStream sendRequestWithoutAuthen(String strUrlRequest) {
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

  /*
   * Check the version of PLF return true if version number is >= 3.5 else
   * return false
   */
  public static boolean checkPLFVersion() {
    try {
      String versionUrl = SocialActivityUtil.getDomain() + "/portal/rest/platform/version";
      Log.i("versionUrl", versionUrl);
      String result = getPLFStream(versionUrl);
      JSONObject json = (JSONObject) JSONValue.parse(result);
      String verObject = json.get("platformVersion").toString();
      int index = verObject.lastIndexOf(".");
      String verNumber = verObject.substring(0, index);
      float num = Float.parseFloat(verNumber);
      if (num < 3.5) {
        ServerSettingHelper.getInstance().setServerVersion("0");
        return false;
      } else {
        if (verObject != null) {
          ServerSettingHelper.getInstance().setServerVersion(verObject);
        }
        return true;
      }
    } catch (RuntimeException e) {
      ServerSettingHelper.getInstance().setServerVersion("0");
      Log.i("Check PLF", e.toString());
      return false;
    }

  }
  
}
