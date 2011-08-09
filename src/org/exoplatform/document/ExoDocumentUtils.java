package org.exoplatform.document;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class ExoDocumentUtils {
  public static boolean putFileToServerFromLocal(AuthScope auth,
                                                 UsernamePasswordCredentials credential,
                                                 String url,
                                                 String path,
                                                 String file,
                                                 String fileType) {
    boolean returnValue = false;

    DefaultHttpClient client = new DefaultHttpClient();
    client.getCredentialsProvider().setCredentials(auth, credential);
    // try {
    // url = URLEncoder.encode(url, "UTF-8");
    // } catch (Exception e) {
    //
    // }

    HttpPut post = new HttpPut(url);

    File fileManager = new File(path + file);
    FileEntity entity = new FileEntity(fileManager, fileType);
    // binary/octet-stream

    post.setEntity(entity);

    try {
      HttpResponse response = client.execute(post);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        returnValue = true;
      }

    } catch (Exception e) {

      String msg = e.getMessage();
      String str = e.toString();
      Log.d(msg, str);
    }

    return returnValue;
  }
}
