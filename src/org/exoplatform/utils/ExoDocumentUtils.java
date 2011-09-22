package org.exoplatform.utils;

import java.io.File;
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

public class ExoDocumentUtils {

  public static boolean putFileToServerFromLocal(String url, File fileManager, String fileType) {
    HttpResponse response = null;
    try {

      HttpPut put = new HttpPut(url);
      FileEntity fileEntity = new FileEntity(fileManager, fileType);
      put.setEntity(fileEntity);
      fileEntity.setContentType(fileType);

      response = ExoConnectionUtils.httpClient.execute(put);
      int status = response.getStatusLine().getStatusCode();
      System.out.println("status " + status);
      if (status >= 200 && status < 300) {
        return true;
      } else
        return false;

    } catch (Exception e) {
      return false;
    }

  }

  public static String getDocumentUrl(String userName, String domain) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(domain);
    buffer.append(ExoConstants.DOCUMENT_PATH);

    if (AccountSetting.getInstance().getIsNewVersion() == true) {
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

    }
    buffer.append("/");
    buffer.append(userName);

    return buffer.toString();
  }

  // Get file array from URL
  public static List<ExoFile> getPersonalDriveContent(String url) {

    List<ExoFile> arrFilesTmp = new ArrayList<ExoFile>();
    String responseStr = ExoConnectionUtils.getDriveContent(url.replace(" ", "%20"));
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
    String strIconFileName = "unknown.png";

    if (contentType.indexOf("image") >= 0)
      strIconFileName = "image.png";
    else if (contentType.indexOf("video") >= 0)
      strIconFileName = "video.png";
    else if (contentType.indexOf("audio") >= 0)
      strIconFileName = "music.png";
    else if (contentType.indexOf("application/msword") >= 0)
      strIconFileName = "word.png";
    else if (contentType.indexOf("application/pdf") >= 0)
      strIconFileName = "pdf.png";
    else if (contentType.indexOf("application/xls") >= 0)
      strIconFileName = "xls.png";
    else if (contentType.indexOf("application/vnd.ms-powerpoint") >= 0)
      strIconFileName = "ppt.png";
    else if (contentType.indexOf("text") >= 0)
      strIconFileName = "txt.png";

    return strIconFileName;
  }
  
  public static int getPicIDFromName(String name)
  {
    int id = 0;
    if(name.equalsIgnoreCase("image.png"))
      id = R.drawable.image;
    else if(name.equalsIgnoreCase("video.png"))
      id = R.drawable.video;
    else if(name.equalsIgnoreCase("music.png"))
      id = R.drawable.music;
    else if(name.equalsIgnoreCase("word.png"))
      id = R.drawable.word;
    else if(name.equalsIgnoreCase("pdf.png"))
      id = R.drawable.pdf;
    else if(name.equalsIgnoreCase("xls.png"))
      id = R.drawable.xls;
    else if(name.equalsIgnoreCase("ppt.png"))
      id = R.drawable.ppt;
    else if(name.equalsIgnoreCase("txt.png"))
      id = R.drawable.txt;
    else
      id = R.drawable.unknown;
    
    return id;
    
  }

  public static String getParentUrl(String url) {
    
    return url.substring(0, url.lastIndexOf("/"));
  }
}
