package org.exoplatform.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.ExoConnectionUtils;

import android.util.Log;

public class ExoDocumentUtils {

  public static String repositoryHomeURL = null;

  public static boolean putFileToServerFromLocal(String url, File fileManager, String fileType) {
    HttpResponse response = null;
    try {

      HttpPut put = new HttpPut(url);
      FileEntity fileEntity = new FileEntity(fileManager, fileType);
      put.setEntity(fileEntity);
      fileEntity.setContentType(fileType);

      response = ExoConnectionUtils.httpClient.execute(put);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return true;
      } else
        return false;

    } catch (Exception e) {
      return false;
    }

  }

  public static boolean setRepositoryHomeUrl(String userName, String domain) {

    if (repositoryHomeURL == null) {
      StringBuffer buffer = new StringBuffer();
      buffer.append(domain);
      buffer.append(ExoConstants.DOCUMENT_PATH);

      int length = userName.length();
      if (length < 4) {
        for (int i = 1; i < length; i++) {
          String userNameLevel = userName.substring(0, i);
          buffer.append("/");
          buffer.append(userNameLevel);
          buffer.append("___");
        }
      } else {
        for (int i = 1; i < 4; i++) {
          String userNameLevel = userName.substring(0, i);
          buffer.append("/");
          buffer.append(userNameLevel);
          buffer.append("___");
        }
      }

      buffer.append("/");
      buffer.append(userName);

      try {

        WebdavMethod copy = new WebdavMethod("HEAD", buffer.toString());
        int status = ExoConnectionUtils.httpClient.execute(copy).getStatusLine().getStatusCode();

        if (status >= 200 && status < 300)
          repositoryHomeURL = buffer.toString();
        else
          repositoryHomeURL = domain + ExoConstants.DOCUMENT_PATH + "/" + userName;
        return true;
      } catch (Exception e) {

        repositoryHomeURL = null;
        return false;
      }

    }
    return true;
    // Log.e("123: ", repositoryHomeURL);

  }

  // Get file array from URL
  public static List<ExoFile> getPersonalDriveContent(String url) {

    List<ExoFile> arrFilesTmp = new ArrayList<ExoFile>();
    String responseStr = ExoConnectionUtils.getDriveContent(url);
    if (responseStr != null) {
      int local1;
      int local2;
      do {
        local1 = responseStr.indexOf("alt=\"\"> ");

        if (local1 > 0) {
          int local3 = responseStr.indexOf("<a href=\"");
          int local4 = responseStr.indexOf("\"><img src");
          String urlStr = responseStr.substring(local3 + 9, local4);

          responseStr = responseStr.substring(local1 + 8);
          local2 = responseStr.indexOf("</a>");
          String fileName = responseStr.substring(0, local2);
          if (!fileName.equalsIgnoreCase("..")) {
            fileName = StringEscapeUtils.unescapeHtml(fileName);
            fileName = StringEscapeUtils.unescapeJava(fileName);
            try {
              fileName = new String(fileName.getBytes(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
            ExoFile file = new ExoFile(urlStr, fileName);
            arrFilesTmp.add(file);
          }

          if (local2 > 0)
            responseStr = responseStr.substring(local2);
        }

      } while (local1 > 0);
    }
    return arrFilesTmp;
  }

  // Get file/folder icon file name form content type
  static public String getFileFolderIconName(String contentType) {
    String strIconFileName = "documenticonforunknown";
    if (contentType != null) {
      if (contentType.indexOf("image") >= 0)
        strIconFileName = "documenticonforimage";
      else if (contentType.indexOf("video") >= 0)
        strIconFileName = "documenticonforvideo";
      else if (contentType.indexOf("audio") >= 0)
        strIconFileName = "documenticonformusic";
      else if (contentType.indexOf("application/msword") >= 0)
        strIconFileName = "documenticonforword";
      else if (contentType.indexOf("application/pdf") >= 0)
        strIconFileName = "documenticonforpdf";
      else if (contentType.indexOf("application/xls") >= 0)
        strIconFileName = "documenticonforxls";
      else if (contentType.indexOf("application/vnd.ms-powerpoint") >= 0)
        strIconFileName = "documenticonforppt";
      else if (contentType.indexOf("text") >= 0)
        strIconFileName = "documenticonfortxt";
    } else
      strIconFileName = "documenticonforunknown";

    return strIconFileName;
  }

  public static int getPicIDFromName(String name) {
    int id = 0;
    if (name != null) {
      if (name.equalsIgnoreCase("documenticonforimage"))
        id = R.drawable.documenticonforimage;
      else if (name.equalsIgnoreCase("documenticonforvideo"))
        id = R.drawable.documenticonforvideo;
      else if (name.equalsIgnoreCase("documenticonformusic"))
        id = R.drawable.documenticonformusic;
      else if (name.equalsIgnoreCase("documenticonforword"))
        id = R.drawable.documenticonforword;
      else if (name.equalsIgnoreCase("documenticonforpdf"))
        id = R.drawable.documenticonforpdf;
      else if (name.equalsIgnoreCase("documenticonforxls"))
        id = R.drawable.documenticonforxls;
      else if (name.equalsIgnoreCase("documenticonforppt"))
        id = R.drawable.documenticonforppt;
      else if (name.equalsIgnoreCase("documenticonfortxt"))
        id = R.drawable.documenticonfortxt;
      else
        id = R.drawable.documenticonforunknown;
    } else
      id = R.drawable.documenticonforunknown;

    return id;

  }

  public static String getParentUrl(String url) {

    return url.substring(0, url.lastIndexOf("/"));
  }

  public static String getLastPathComponent(String url) {

    return url.substring(url.lastIndexOf("/") + 1, url.length());
  }

}
