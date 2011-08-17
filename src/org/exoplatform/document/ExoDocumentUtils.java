package org.exoplatform.document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.exoplatform.controller.AppController;
import org.exoplatform.utils.ExoConnectionUtils;

import android.util.Log;

public class ExoDocumentUtils {
  public static boolean putFileToServerFromLocal(String url, File fileManager, String fileType) {

    HttpPut post = new HttpPut(url);
//    post.setHeader("Content-Type", "image/jpeg");
    // File fileManager = new File(path + file);
    FileEntity entity = new FileEntity(fileManager, fileType);

    // binary/octet-stream

    post.setEntity(entity);

    try {
      HttpResponse response = ExoConnectionUtils.httpClient.execute(post);
      HttpEntity e = response.getEntity();
      InputStream is = e.getContent();
      String result = ExoConnectionUtils.convertStreamToString(is);
      System.out.println("result" + result);
      int status = response.getStatusLine().getStatusCode();
      System.out.println("status " + status);
      if (status >= 200 && status < 300) {
        return true;
      } else
        return false;

    } catch (Exception e) {
      String msg = e.getMessage();
      String str = e.toString();
      Log.d(msg, str);
      return false;
    }

  }

  public static void put(File file, String urlString) {
    HttpURLConnection urlconnection = null;
    try {
      String strUserName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME,
                                                                    "exo_prf_username");
      String strPassword = AppController.sharedPreference.getString(AppController.EXO_PRF_PASSWORD,
                                                                    "exo_prf_password");
      URL url = new URL(urlString);
      urlconnection = (HttpURLConnection) url.openConnection();
      urlconnection.setDoOutput(true);
      urlconnection.setDoInput(true);
      try {
        urlconnection.setRequestMethod("PUT");
        urlconnection.setRequestProperty("Content-type", "image/jpeg");
        urlconnection.setRequestProperty("Authorization",
                                         ExoConnectionUtils.authorizationHeader(strUserName,
                                                                                strPassword));
        urlconnection.connect();
      } catch (ProtocolException e) {
        System.out.println("Exception  "+e.getMessage());
      }
      System.out.println("response   " + urlconnection.getResponseMessage());
    } catch (Exception e) {
      System.out.println("Exception  "+e.getMessage());
    }finally{
      urlconnection.disconnect();
    }

  }
}
