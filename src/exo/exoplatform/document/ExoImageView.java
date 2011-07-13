package exo.exoplatform.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import exo.exoplatform.R;
import exo.exoplatform.controller.AppController;

public class ExoImageView extends Activity {

  private EditText  txtFileName;   // Edit image name

  private ImageView imgView;       // Image view

  private Button    btnUploadImage; // upload image

  private String    fileName;      // Name for image

  public static Uri imageUri;      // Image Uri

  /** Called when the activity is first created. */
  // Constructor
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.exoimageview);

    Intent intentTakeImage = new Intent("android.media.action.IMAGE_CAPTURE");
    startActivityForResult(intentTakeImage, 0);

    txtFileName = (EditText) findViewById(R.id.EditTextImageName);

    btnUploadImage = (Button) findViewById(R.id.ButtonUpImage);
    btnUploadImage.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {

        fileName = txtFileName.getText().toString();
        if (fileName == null || fileName.equalsIgnoreCase("")) {
          fileName = txtFileName.getHint().toString();
        }

        String localFile = "file:///sdcard/eXo/";
        saveToLocal(AppController.auth, AppController.credential, imageUri, localFile, fileName);
        putFileToServerFromLocal(AppController.auth,
                                 AppController.credential,
                                 ExoFilesController.myFile.urlStr,
                                 localFile,
                                 fileName,
                                 "image/jpeg");
      }

    });

    imgView = (ImageView) findViewById(R.id.ImageView);

  }

  // Keydown listioner
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      Toast.makeText(ExoImageView.this,
                     "If you want to back, let's use the \"Back\" button on the screen",
                     Toast.LENGTH_LONG).show();
    }
    return false;
  }

  // Display taken photo
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
      Bitmap _bitmap = (Bitmap) data.getExtras().get("data");
      imgView.setImageBitmap(_bitmap);
    }
  }

  // Set image for image view
  public void setImage(Bitmap bmp) {
    imgView.setImageBitmap(bmp);
  }

  // Save file into SDCard
  private boolean saveToLocal(AuthScope auth,
                              UsernamePasswordCredentials credential,
                              Uri uri,
                              String path,
                              String file) {
    boolean returnValue = false;
    // String str2 = AppController._eXoConnection.convertStreamToString(is);
    try {
      InputStream is = getContentResolver().openInputStream(uri);
      File f = new File(path);
      if (!f.exists()) {
        f.mkdir();
      }

      f = new File(path, file);
      if (!f.exists()) {
        f.createNewFile();
      }

      FileOutputStream fos = new FileOutputStream(f);
      // this.openFileOutput(path + file, MODE_PRIVATE);
      byte[] buffer = new byte[1024];
      int len1 = 0;
      while ((len1 = is.read(buffer)) > 0) {
        fos.write(buffer, 0, len1);
      }

      // f.close();

      returnValue = true;

    } catch (Exception e) {

      // String msg = e.getMessage();
      // String str = e.toString();
      // Log.d(str, msg);
    }

    return returnValue;

  }

  // Send file to server
  private boolean putFileToServerFromLocal(AuthScope auth,
                                           UsernamePasswordCredentials credential,
                                           String url,
                                           String path,
                                           String file,
                                           String fileType) {
    boolean returnValue = false;

    DefaultHttpClient client = new DefaultHttpClient();
    client.getCredentialsProvider().setCredentials(auth, credential);
    HttpPut post = new HttpPut(url + "/" + file);

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

      // String msg = e.getMessage();
      // String str = e.toString();
      // Log.d(msg, str);
    }

    return returnValue;
  }

}
