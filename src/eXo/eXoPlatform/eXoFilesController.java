package eXo.eXoPlatform;

import greendroid.app.GDActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.lang.StringEscapeUtils;

public class eXoFilesController extends GDActivity {

  static final String              FILE_CONTENT_TYPE  = "image/bmp image/cgm image/gif image/jpeg image/png image/tiff image/x-icon "
                                                          + "video/mpeg video/quicktime video/x-msvideo "
                                                          + "audio/midi audio/mpeg audio/x-aiff audio/x-mpegurl "
                                                          + "audio/x-pn-realaudio audio/x-wav "
                                                          + "application/msword application/pdf application/vnd.ms-excel "
                                                          + "application/vnd.ms-powerpoint application/zip";

  static ListView                  _lstvFiles;

  TextView                         _textViewEmptyPage;

  // for eXo image View
  EditText                         txtFileName;

  ImageView                        imgView;

  ImageView                        imgViewEmptyPage;

  Button                           _btnUploadImage;

  Button                           _btnCancelUploadImage;

  static ProgressDialog            _progressDialog;

  public static String             _rootUrl;

  public static String             localFilePath      = "/sdcard/eXo/";

  public static Uri                _uri;

  boolean                          _deleteFile;

  boolean                          _copyFile;

  boolean                          _moveFile;

  public static eXoFilesController        eXoFilesControllerInstance;

  static eXoApplicationsController2 _delegate;

  static public eXoFile            myFile;

  static public int                positionOfFileItem = 0;

  public static List<eXoFile>      arrFiles;
  
  public BaseAdapter               fileAdapter;

  String                           strCannotBackToPreviousPage;

  static String                    strDownloadFileIntoSDCard;

  Thread                    thread;

  // Constructor
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
//    setContentView(R.layout.exofilesview);
    
    setActionBarContentView(R.layout.exofilesview);

    eXoFilesControllerInstance = this;

    _btnUploadImage = (Button) findViewById(R.id.ButtonUpImage);
    _btnUploadImage.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {

        Runnable loadingDataRunnable = new Runnable() {
          public void run() {

            String fileName = txtFileName.getText().toString();
            if (fileName == null || fileName.equalsIgnoreCase("")) {
              fileName = txtFileName.getHint().toString();
            }

            String encodedePath = _uri.getEncodedPath();

            AuthScope auth = AppController.auth;
            UsernamePasswordCredentials credential = AppController.credential; 
            
            saveFileToLocal(auth, credential, encodedePath, localFilePath, fileName, true);
            
            String url = myFile.urlStr + "/" + fileName.replace(" ", "%20");
            putFileToServerFromLocal(auth, credential, url, localFilePath, fileName, "image/jpeg");

            runOnUiThread(reloadFileAdapter);
            runOnUiThread(dismissProgressDialog);
          }
        };

        showProgressDialog(true);

        thread = new Thread(null, loadingDataRunnable, "CloseBackFileItem");
        thread.start();

      }

    });

    _btnCancelUploadImage = (Button) findViewById(R.id.ButtonCancel);
    _btnCancelUploadImage.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {

        setVieweXoImage(false);
        // eXoFilesController.this.finish();
      }

    });

    imgView = (ImageView) findViewById(R.id.ImageView);
    imgViewEmptyPage = (ImageView) findViewById(R.id.ImageViewEmptyPage);
    imgViewEmptyPage.setVisibility(View.INVISIBLE);
    // DisplayMetrics dm = new DisplayMetrics();
    // getWindowManager().getDefaultDisplay().getMetrics(dm);
    _textViewEmptyPage = (TextView) findViewById(R.id.TextView_EmptyPage);
    _textViewEmptyPage.setVisibility(View.INVISIBLE);


    _lstvFiles = (ListView) findViewById(R.id.ListView_Files);

    setTitle(getFolderNameFromUrl(_rootUrl));

    txtFileName = (EditText) findViewById(R.id.EditTextImageName);

    setVieweXoImage(false);

    changeLanguage(AppController.bundle);

    createExoFilesAdapter();

  }

  public void finishMe()
  {
    Runnable loadingDataRunnable = new Runnable() {
      public void run() {

        if (myFile.urlStr.equalsIgnoreCase(_rootUrl)) {
          
          GDActivity.TYPE = 0;
          eXoFilesControllerInstance = null;
          finish();
          
        } else {
          int index = myFile.urlStr.lastIndexOf("/");
          myFile.urlStr = myFile.urlStr.substring(0, index);
          arrFiles = getPersonalDriveContent(myFile.urlStr);

          runOnUiThread(closeBackRunnable);
        }

        runOnUiThread(dismissProgressDialog);
      }
    };

    showProgressDialog(true);

    thread = new Thread(null, loadingDataRunnable, "CloseBackFileItem");
    thread.start(); 
    
  }
  // Keydown listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      Toast.makeText(eXoFilesController.this, strCannotBackToPreviousPage, Toast.LENGTH_LONG)
           .show();
    }
    return false;
  }

  // Save file to SDCard
  Runnable        cannotAceesSDCard     = new Runnable() {

                                                 public void run() {

                                                   Toast toast = Toast.makeText(eXoFilesControllerInstance,
                                                                                "SDCard is not available",
                                                                                Toast.LENGTH_SHORT);
                                                   toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                                   toast.show();
                                                 }
                                               };

  // Refresh files view
  Runnable               reloadFileAdapter     = new Runnable() {

                                                 public void run() {

                                                   // Files List
                                                   fileAdapter.notifyDataSetChanged();
                                                   setVieweXoImage(false);

                                                 }
                                               };

  // Dismiss progress dialog
  public Runnable dismissProgressDialog = new Runnable() {

                                                 public void run() {

                                                   if (arrFiles.isEmpty()) {
                                                     eXoFilesControllerInstance.imgViewEmptyPage.setVisibility(View.VISIBLE);
                                                     eXoFilesControllerInstance._textViewEmptyPage.setVisibility(View.VISIBLE);
                                                   } else {
                                                     if(eXoFilesControllerInstance != null)
                                                     {
                                                       eXoFilesControllerInstance.imgViewEmptyPage.setVisibility(View.INVISIBLE);
                                                       eXoFilesControllerInstance._textViewEmptyPage.setVisibility(View.INVISIBLE); 
                                                     }
                                                     
                                                   }

                                                   _progressDialog.dismiss();
                                                   thread.stop();

                                                 }
                                               };

  // Back to parent directory or close file view
  Runnable        closeBackRunnable     = new Runnable() {

                                                 public void run() {

                                                   setTitle(getFolderNameFromUrl(myFile.urlStr));
                                                   // Files List
                                                   fileAdapter.notifyDataSetChanged();
                                                   try {
                                                     if (myFile.urlStr.equalsIgnoreCase(_rootUrl))
//                                                       _btnCloseBack.setText(new String(AppController.bundle.getString("CloseButton")
//                                                                                                            .getBytes("ISO-8859-1"),
//                                                                                        "UTF-8"));
                                                       ;
                                                     else
//                                                       _btnCloseBack.setText(new String(AppController.bundle.getString("BackButton")
//                                                                                                            .getBytes("ISO-8859-1"),
//                                                                                        "UTF-8"));
                                                       ;
                                                   } catch (Exception e) {

                                                     // _btnCloseBack.setText("");
                                                   }
                                                 }
                                               };

  // Goto sub directory or view file
  Runnable fileItemClickRunnable = new Runnable() {

                                                 public void run() {

                                                   // myFile =
                                                   // arrFiles.get(positionOfFileItem);

                                                   try {
//                                                     _btnCloseBack.setText(new String(AppController.bundle.getString("BackButton")
//                                                                                                          .getBytes("ISO-8859-1"),
//                                                                                      "UTF-8"));
                                                     ;
                                                   } catch (Exception e) {

                                                     try {
//                                                       _btnCloseBack.setText(new String(AppController.bundle.getString("CloseButton")
//                                                                                                            .getBytes("ISO-8859-1"),
//                                                                                        "UTF-8"));
                                                       ;
                                                     } catch (Exception e2) {

                                                     }
                                                   }
                                                   if (myFile.isFolder) {
                                                     
                                                     setTitle(myFile.fileName.replace("%20"," "));
                                                     fileAdapter.notifyDataSetChanged();

                                                   } else {
                                                     // _strCurrentDirectory =
                                                     // _strCurrentDirectory +
                                                     // "/" + myFile.fileName;
                                                     eXoApplicationsController.webViewMode = 1;

                                                     AlertDialog.Builder builder = new AlertDialog.Builder(eXoFilesControllerInstance);
                                                     // builder.setMessage("Do you want to download "
                                                     // +
                                                     // myFile.fileName.replace("%20",
                                                     // " ") + " into sdcard?");
                                                     builder.setMessage(strDownloadFileIntoSDCard);
                                                     builder.setCancelable(false);

                                                     builder.setPositiveButton("YES",
                                                                               new DialogInterface.OnClickListener() {
                                                                                 public void onClick(DialogInterface dialog,
                                                                                                     int id) {

                                                                                   Runnable loadingDataRunnable = new Runnable() {
                                                                                     public void run() {
                                                                                       eXoFilesControllerInstance.saveFileToLocal(AppController.auth,
                                                                                                                                  AppController.credential,
                                                                                                                                  myFile.urlStr,
                                                                                                                                  localFilePath,
                                                                                                                                  myFile.fileName.replace("%20",
                                                                                                                                                          " "),
                                                                                                                                  false);

                                                                                       int index = myFile.urlStr.lastIndexOf("/");
                                                                                       myFile.urlStr = myFile.urlStr.substring(0,
                                                                                                                               index);

                                                                                       eXoFilesControllerInstance.runOnUiThread(dismissProgressDialog);
                                                                                     }
                                                                                   };

                                                                                   showProgressDialog(true);

                                                                                   thread = new Thread(loadingDataRunnable,
                                                                                                       "fileItemClickOnIcon");
                                                                                   thread.start();
                                                                                   dialog.dismiss();

                                                                                 }
                                                                               });

                                                     builder.setNegativeButton("NO",
                                                                               new DialogInterface.OnClickListener() {
                                                                                 public void onClick(DialogInterface dialog,
                                                                                                     int id) {

                                                                                   int index = myFile.urlStr.lastIndexOf("/");
                                                                                   myFile.urlStr = myFile.urlStr.substring(0,
                                                                                                                           index);
                                                                                 }
                                                                               });

                                                     AlertDialog alert = builder.create();
                                                     alert.show();

                                                   }
                                                 }
                                               };

  // Take a photo
  public static void takePicture() {
    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    eXoFilesControllerInstance.startActivityForResult(intent, 0);

  }

  // Take photo app
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
      setVieweXoImage(true);
      _uri = data.getData();
      Bitmap bmp = (Bitmap) data.getExtras().get("data");
      imgView.setImageBitmap(bmp);
    } else if (resultCode == Activity.RESULT_CANCELED) {
      return;
      // finish();
    }

  }

  // Show/hide taken photo
  private void setVieweXoImage(boolean isVieweXoImage) {
    int viewImageMode;
    int viewFileMode;
    if (isVieweXoImage) {
      viewImageMode = View.VISIBLE;
      viewFileMode = View.INVISIBLE;
    } else {
      viewImageMode = View.INVISIBLE;
      viewFileMode = View.VISIBLE;
    }

    _lstvFiles.setVisibility(viewFileMode);
    // _btnCloseBack.setVisibility(viewFileMode);

    // for eXo image View
    txtFileName.setVisibility(viewImageMode);
    imgView.setVisibility(viewImageMode);
    _btnUploadImage.setVisibility(viewImageMode);
    _btnCancelUploadImage.setVisibility(viewImageMode);
  }

  // Get file/folder icon form URL
  static private Bitmap fileFolderIcon(eXoFile file) {
    String contentType = "unknown.png";

    if (file.contentType.indexOf("image") >= 0)
      contentType = "image.png";
    else if (file.contentType.indexOf("video") >= 0)
      contentType = "video.png";
    else if (file.contentType.indexOf("audio") >= 0)
      contentType = "music.png";
    else if (file.contentType.indexOf("application/msword") >= 0)
      contentType = "word.png";
    else if (file.contentType.indexOf("application/pdf") >= 0)
      contentType = "pdf.png";
    else if (file.contentType.indexOf("application/vnd.ms-excel") >= 0)
      contentType = "xls.png";
    else if (file.contentType.indexOf("application/vnd.ms-powerpoint") >= 0)
      contentType = "ppt.png";
    else if (file.contentType.indexOf("text") >= 0)
      contentType = "text.png";

    Bitmap bmp = null;
    try {
      bmp = BitmapFactory.decodeStream(eXoFilesControllerInstance.getAssets().open(contentType));
    } catch (Exception e) {

    }

    return bmp;
  }

  // Get file array from URL
  public static List<eXoFile> getPersonalDriveContent(String url) {
    List<eXoFile> arrFilesTmp = new ArrayList<eXoFile>();

    String responseStr = AppController._eXoConnection.sendRequestWithAuthorizationReturnString(url.replace(" ",
                                                                                                           "%20"));

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

          eXoFile file = new eXoFile(urlStr, fileName);
          arrFilesTmp.add(file);
        }

        if (local2 > 0)
          responseStr = responseStr.substring(local2);
      }

    } while (local1 > 0);

    if (arrFilesTmp.isEmpty()) {
      // ImageView imgView = new ImageView(thisClass.getApplicationContext());
      // imgView.setImageResource(R.drawable.emptypage);
      //			
      // _lstvFiles.addView(imgView, 200, 200);
    }

    return arrFilesTmp;
  }

  // Show progress dialog
  public static void showProgressDialog(boolean isloadingData) {
    String strLoadingDataFromServer = "";
    try {
      if (isloadingData)
        strLoadingDataFromServer = new String(AppController.bundle.getString("LoadingDataFromServer")
                                                                  .getBytes("ISO-8859-1"),
                                              "UTF-8");
      else
        strLoadingDataFromServer = new String(AppController.bundle.getString("FileProcessing")
                                                                  .getBytes("ISO-8859-1"), "UTF-8");

    } catch (Exception e) {

      strLoadingDataFromServer = "";
    }

    _progressDialog = ProgressDialog.show(eXoFilesControllerInstance,
                                          null,
                                          strLoadingDataFromServer);
  }

  // Create file adapter
  public void createExoFilesAdapter() {
     fileAdapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = eXoFilesControllerInstance.getLayoutInflater();
        final View rowView = inflater.inflate(R.layout.fileitem, parent, false);
        final int pos = position;

        ImageView icon = (ImageView) rowView.findViewById(R.id.icon);
        icon.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            positionOfFileItem = pos;
            myFile = arrFiles.get(positionOfFileItem);

            if (!myFile.isFolder) {
              eXoFilesControllerInstance.runOnUiThread(fileItemClickRunnable);
            } else {
              Runnable loadingDataRunnable = new Runnable() {
                public void run() {

                  arrFiles = getPersonalDriveContent(myFile.urlStr);

                  eXoFilesControllerInstance.runOnUiThread(fileItemClickRunnable);

                  eXoFilesControllerInstance.runOnUiThread(dismissProgressDialog);
                }
              };

              showProgressDialog(true);

              thread = new Thread(loadingDataRunnable, "fileItemClickOnIcon");
              thread.start();
            }

          }
        });

        TextView lb = (TextView) rowView.findViewById(R.id.label);
        lb.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            positionOfFileItem = pos;
            Runnable loadingDataRunnable = new Runnable() {
              public void run() {

                myFile = arrFiles.get(positionOfFileItem);
                if (myFile.isFolder)
                  arrFiles = getPersonalDriveContent(myFile.urlStr);
                eXoFilesControllerInstance.runOnUiThread(fileItemClickRunnable);

                eXoFilesControllerInstance.runOnUiThread(dismissProgressDialog);
              }
            };

            showProgressDialog(true);

            thread = new Thread(loadingDataRunnable, "fileItemClickOnIcon");
            thread.start();

          }
        });

        Button btn = (Button) rowView.findViewById(R.id.Button_FileAction);
        btn.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            positionOfFileItem = pos;
            // myFile = arrFiles.get(positionOfFileItem);
            eXoFileActionDialog fileActionDialog = new eXoFileActionDialog(eXoFilesControllerInstance,
                                                                           arrFiles.get(positionOfFileItem));
            // fileAction.setTitle("User guide & language setting");
            fileActionDialog.show();

          }
        });
        bindView(rowView, arrFiles.get(position));
        return (rowView);
      }

      private void bindView(View view, eXoFile file) {
        TextView label = (TextView) view.findViewById(R.id.label);

        label.setText(file.fileName.replace("%20", " "));

        ImageView icon = (ImageView) view.findViewById(R.id.icon);

        if (!file.isFolder) {

          icon.setImageBitmap(fileFolderIcon(file));
          // icon.setImageResource(.fileName));
        } else {
          icon.setImageResource(R.drawable.folder);
        }

      }

      public long getItemId(int position) {

        return position;
      }

      public Object getItem(int position) {

        return position;
      }

      public int getCount() {

        int count = arrFiles.size();
        return count;
      }
    };

    _lstvFiles.setAdapter(fileAdapter);
    // _lstvFiles.setOnItemClickListener(test);
  }

  // Save file to SDCard
  private boolean saveFileToLocal(AuthScope auth,
                                  UsernamePasswordCredentials credential,
                                  String url,
                                  String path,
                                  String file,
                                  boolean isTakeImage) {
    boolean returnValue = false;

    try {

      InputStream is;
      if (isTakeImage)
        is = eXoFilesControllerInstance.getContentResolver().openInputStream(_uri);
      else
        is = getInputStreamFromServer(auth, credential, url);

      AppController.configurationInstance.createLocalFileDirectory(path, true);
      AppController.configurationInstance.createLocalFileDirectory(path + "/" + file, true);

      FileOutputStream fos = new FileOutputStream(new File(path, file));
      // this.openFileOutput(path + file, MODE_PRIVATE);
      byte[] buffer = new byte[1024];

      int len1 = 0;
      while ((len1 = is.read(buffer)) > 0) {
        fos.write(buffer, 0, len1);

      }

      // f.close();

      returnValue = true;

    } catch (Exception e) {

      String msg = e.getMessage();
      String str = e.toString();
      Log.e(str, msg);

      eXoFilesControllerInstance.runOnUiThread(cannotAceesSDCard);

    }

    return returnValue;

  }

  // Get InputStream from URL with authentication
  public static InputStream getInputStreamFromServer(AuthScope auth,
                                                     UsernamePasswordCredentials credential,
                                                     String url) {
    InputStream is = null;

    HttpResponse response = null;
    DefaultHttpClient client = new DefaultHttpClient();
    client.getCredentialsProvider().setCredentials(auth, credential);
    HttpGet get = new HttpGet(url);
    try {

      response = client.execute(get);
      is = response.getEntity().getContent();

    } catch (Exception e) {

    }
    // client.getConnectionManager().shutdown();

    return is;

  }

  // Delete file/folder method
  public static boolean deleteMethod(AuthScope auth,
                                     UsernamePasswordCredentials credential,
                                     String url) {
    boolean returnValue = false;

    DefaultHttpClient client = new DefaultHttpClient();
    client.getCredentialsProvider().setCredentials(auth, credential);
    HttpDelete delete = new HttpDelete(url);
    try {
      HttpResponse response = client.execute(delete);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        returnValue = true;
      }
    } catch (Exception e) {

    }

    return returnValue;
  }

  // Send file to server
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

  // Copy file/folder method
  public static boolean copyMethod(AuthScope auth,
                                   UsernamePasswordCredentials credential,
                                   String source,
                                   String destination) {
    boolean returnValue = false;

    DefaultHttpClient client = new DefaultHttpClient();
    client.getCredentialsProvider().setCredentials(auth, credential);

    HttpPut put = new HttpPut(destination);

    put.setHeader("Destination", source);
    put.setHeader("Overwrite", "T");

    try {

      HttpResponse response = client.execute(put);
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

  // Move file/folder method
  public static boolean moveMethod(AuthScope auth,
                                   UsernamePasswordCredentials credential,
                                   String source,
                                   String destination) {
    boolean returnValue = false;
    returnValue = eXoFilesController.copyMethod(auth, credential, source, destination);
    if (returnValue)
      returnValue = eXoFilesController.deleteMethod(auth, credential, source);

    return returnValue;
  }

  // Get folder name from given URL
  private String getFolderNameFromUrl(String url) {
    String folder = "";
    int lastSlashIndex = url.lastIndexOf("/");

    folder = url.substring(lastSlashIndex + 1);

    return folder;
  }

  // Endcode URL
  public String encodeUrl(String urlString) {

    urlString.replace("&", "%26");
    urlString.replace("+", "%2B");
    urlString.replace(",", "%2C");
    // urlString.replace("/", "%2F");
    // urlString.replace(":", "%3A");
    urlString.replace(";", "%3B");
    urlString.replace("=", "%3D");
    urlString.replace("?", "%3F");
    urlString.replace("@", "%40");
    urlString.replace(" ", "%20");
    urlString.replace("\t", "%09");
    urlString.replace("#", "%23");
    urlString.replace("<", "%3C");
    urlString.replace(">", "%3E");
    urlString.replace("\"", "%22");
    urlString.replace("\n", "%0A");

    return urlString;
  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {
    String strCloseBack = "";
    String strUploadFile = "";
    String strCancel = "";
    String strEmptyPage = "";
    try {
      strCloseBack = new String(resourceBundle.getString("CloseButton").getBytes("ISO-8859-1"),
                                "UTF-8");
      strUploadFile = new String(resourceBundle.getString("Upload").getBytes("ISO-8859-1"), "UTF-8");
      strCancel = new String(resourceBundle.getString("Cancel").getBytes("ISO-8859-1"), "UTF-8");
      strEmptyPage = new String(resourceBundle.getString("EmptyPage").getBytes("ISO-8859-1"),
                                "UTF-8");
      strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage")
                                                             .getBytes("ISO-8859-1"), "UTF-8");
      strDownloadFileIntoSDCard = new String(resourceBundle.getString("DownloadFileInToSDCard")
                                                           .getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {

    }
    
    _btnUploadImage.setText(strUploadFile);
    _btnCancelUploadImage.setText(strCancel);
    _textViewEmptyPage.setText(strEmptyPage);

    _delegate.changeLanguage(resourceBundle);
//    _delegate.createAdapter();

  }

}
