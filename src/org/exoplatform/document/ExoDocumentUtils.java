package org.exoplatform.document;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.exoplatform.controller.AppController;
import org.exoplatform.utils.ExoConnectionUtils;

public class ExoDocumentUtils {

  public static boolean putFileToServerFromLocal(String url, File fileManager, String fileType) {
    HttpResponse response = null;
    try {

      HttpPut post = new HttpPut(url);
      FileEntity fileEntity = new FileEntity(fileManager, fileType);
      post.setEntity(fileEntity);
      fileEntity.setContentType(fileType);

      response = ExoConnectionUtils.httpClient.execute(post);
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
    buffer.append("/rest/private/jcr/repository/collaboration/Users");

    if (AppController.isNewVersion == true) {
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

}
