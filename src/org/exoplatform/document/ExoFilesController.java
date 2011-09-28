package org.exoplatform.document;

import greendroid.widget.ActionBarItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.utils.WebdavMethod;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.WaitingDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;

public class ExoFilesController extends MyActionBar {

  static final String              FILE_CONTENT_TYPE  = "image/bmp image/cgm image/gif image/jpeg image/png image/tiff image/x-icon "
                                                          + "video/mpeg video/quicktime video/x-msvideo "
                                                          + "audio/midi audio/mpeg audio/x-aiff audio/x-mpegurl "
                                                          + "audio/x-pn-realaudio audio/x-wav "
                                                          + "application/msword application/pdf application/vnd.ms-excel "
                                                          + "application/vnd.ms-powerpoint application/zip";

  static ListView                  _lstvFiles;

  TextView                         _textViewEmptyPage;

  // for eXo image View
  // EditText txtFileName;

  ImageView                        imgView;

  ImageView                        imgViewEmptyPage;

  Button                           _btnUploadImage;

  Button                           _btnCancelUploadImage;

  static WaitingDialog             _progressDialog;

  public static String             _rootUrl;

  public static String             localFilePath;                                                           // =
                                                                                                             // "/sdcard/eXo/";

  public static Uri                _uri;

  boolean                          _deleteFile;

  boolean                          _copyFile;

  boolean                          _moveFile;

  public static ExoFilesController eXoFilesControllerInstance;

  // public static ExoApplicationsController2 _delegate;

  static public ExoFile            myFile;

  static public int                positionOfFileItem = 0;

  public static List<ExoFile>      arrFiles;

  public BaseAdapter               fileAdapter;

  String                           strCannotBackToPreviousPage;

  static String                    strDownloadFileIntoSDCard;

  Thread                           thread;

  private static String            sdcard_temp_dir;

  public static String             uploadFileUrl;

  // Constructor
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.exofilesview);

    eXoFilesControllerInstance = this;

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    // addActionBarItem();
    // getActionBar().getItem(0).setDrawable(R.drawable.home);

    localFilePath = Environment.getExternalStorageDirectory() + "/eXo/";

    _btnUploadImage = (Button) findViewById(R.id.ButtonUpImage);
    _btnUploadImage.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {

        Runnable loadingDataRunnable = new Runnable() {
          public void run() {

            File file = new File(sdcard_temp_dir);
            ExoDocumentUtils.putFileToServerFromLocal(uploadFileUrl + "/" + file.getName(),
                                                      file,
                                                      "image/png");
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

    // setTitle(getFolderNameFromUrl(_rootUrl));
    setTitle(getFolderNameFromUrl(myFile.urlStr));

    // txtFileName = (EditText) findViewById(R.id.EditTextImageName);

    setVieweXoImage(false);

    changeLanguage();

    createExoFilesAdapter();
  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    myFile = null;
    _rootUrl = null;
    Intent intent = new Intent(this, HomeActivity.class);
    startActivity(intent);
    finish();
    return true;
  }

  private void destroy() {
    if (myFile.urlStr.equalsIgnoreCase(_rootUrl)) {
      eXoFilesControllerInstance.finish();
      myFile = null;
      _rootUrl = null;
      Intent intent = new Intent(this, HomeActivity.class);
      startActivity(intent);
      finish();
    } else {
      finishMe();
    }
  }

  public void finishMe() {
    Runnable loadingDataRunnable = new Runnable() {
      public void run() {
        int index = myFile.urlStr.lastIndexOf("/");
        myFile.urlStr = myFile.urlStr.substring(0, index);
        arrFiles = getPersonalDriveContent(myFile.urlStr);

        runOnUiThread(closeBackRunnable);
        runOnUiThread(dismissProgressDialog);
      }
    };

    showProgressDialog(true);

    thread = new Thread(null, loadingDataRunnable, "CloseBackFileItem");
    thread.start();

  }

  public void onBackPressed() {
    destroy();
  };

  // Save file to SDCard
  Runnable         cannotAceesSDCard     = new Runnable() {

                                           public void run() {

                                             Toast toast = Toast.makeText(eXoFilesControllerInstance,
                                                                          "SDCard is not available",
                                                                          Toast.LENGTH_SHORT);
                                             toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                             toast.show();
                                           }
                                         };

  // Refresh files view
  Runnable         reloadFileAdapter     = new Runnable() {

                                           public void run() {

                                             // Files List
                                             fileAdapter.notifyDataSetChanged();
                                             setVieweXoImage(false);

                                           }
                                         };

  // Dismiss progress dialog
  public Runnable  dismissProgressDialog = new Runnable() {

                                           public void run() {

                                             if (arrFiles.size() == 0) {
                                               eXoFilesControllerInstance.imgViewEmptyPage.setVisibility(View.VISIBLE);
                                               eXoFilesControllerInstance._textViewEmptyPage.setVisibility(View.VISIBLE);
                                             } else {
                                               if (eXoFilesControllerInstance != null) {
                                                 eXoFilesControllerInstance.imgViewEmptyPage.setVisibility(View.INVISIBLE);
                                                 eXoFilesControllerInstance._textViewEmptyPage.setVisibility(View.INVISIBLE);
                                               }

                                             }

                                             _progressDialog.dismiss();
                                             // thread.stop();

                                           }
                                         };

  // Back to parent directory or close file view
  private Runnable closeBackRunnable     = new Runnable() {

                                           public void run() {

                                             try {
                                               if (myFile.urlStr.equalsIgnoreCase(_rootUrl)) {
                                                 eXoFilesControllerInstance.finish();
                                               } else
                                                 eXoFilesControllerInstance.finish();
                                               eXoFilesControllerInstance.startActivity(new Intent(eXoFilesControllerInstance,
                                                                                                   eXoFilesControllerInstance.getClass()));
                                               ;
                                             } catch (Exception e) {

                                             }
                                           }
                                         };

  // Goto sub directory or view file
  private Runnable fileItemClickRunnable = new Runnable() {

                                           public void run() {

//                                             ExoApplicationsController.webViewMode = 1;

                                             AlertDialog.Builder builder = new AlertDialog.Builder(eXoFilesControllerInstance);
                                             builder.setMessage(strDownloadFileIntoSDCard);
                                             builder.setCancelable(false);

                                             builder.setPositiveButton("YES",
                                                                       new DialogInterface.OnClickListener() {
                                                                         public void onClick(DialogInterface dialog,
                                                                                             int id) {

                                                                           Runnable loadingDataRunnable = new Runnable() {
                                                                             public void run() {
//                                                                               eXoFilesControllerInstance.saveFileToLocal(AppController.auth,
//                                                                                                                          AppController.credential,
//                                                                                                                          myFile.urlStr,
//                                                                                                                          localFilePath,
//                                                                                                                          myFile.fileName.replace("%20",
//                                                                                                                                                  " "),
//                                                                                                                          false);

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

                                         };

  // Take a photo
  public static void takePicture() {
    String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
    sdcard_temp_dir = parentPath + PhotoUltils.getImageFileName();

    Intent takePictureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    takePictureFromCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                         Uri.fromFile(new File(sdcard_temp_dir)));
    eXoFilesControllerInstance.startActivityForResult(takePictureFromCameraIntent,
                                                      ExoConstants.TAKE_PICTURE_WITH_CAMERA);

  }

  // Take photo app
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == ExoConstants.TAKE_PICTURE_WITH_CAMERA || resultCode == Activity.RESULT_OK) {
      File file = new File(sdcard_temp_dir);
      setVieweXoImage(true);
      // _uri = data.getData();
      try {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        FileInputStream fis = new FileInputStream(file);
        Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
        fis.close();
        // Bitmap bmp = (Bitmap) data.getExtras().get("data");
        imgView.setImageBitmap(bitmap);
      } catch (Exception e) {
      }

    } else if (resultCode == Activity.RESULT_CANCELED) {
      return;
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
    imgView.setVisibility(viewImageMode);
    _btnUploadImage.setVisibility(viewImageMode);
    _btnCancelUploadImage.setVisibility(viewImageMode);
  }

  // Get file/folder icon form URL
  static private Bitmap fileFolderIcon(ExoFile file) {
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
    else if (file.contentType.indexOf("application/xls") >= 0)
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

  // Show progress dialog
  public static void showProgressDialog(boolean isloadingData) {
    String strLoadingDataFromServer = "";
    try {
      if (isloadingData)
        strLoadingDataFromServer = "";
      else
        strLoadingDataFromServer = "";

    } catch (Exception e) {

      strLoadingDataFromServer = "";
    }

    // _progressDialog = ProgressDialog.show(eXoFilesControllerInstance,
    // null,
    // strLoadingDataFromServer);
    _progressDialog = new WaitingDialog(eXoFilesControllerInstance, null, strLoadingDataFromServer);
    _progressDialog.show();
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
                  eXoFilesControllerInstance.finish();
                  eXoFilesControllerInstance.startActivity(new Intent(eXoFilesControllerInstance,
                                                                      eXoFilesControllerInstance.getClass()));

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
            myFile = arrFiles.get(positionOfFileItem);

            if (!myFile.isFolder) {
              eXoFilesControllerInstance.runOnUiThread(fileItemClickRunnable);
            } else {
              Runnable loadingDataRunnable = new Runnable() {
                public void run() {

                  arrFiles = getPersonalDriveContent(myFile.urlStr);
                  eXoFilesControllerInstance.finish();
                  eXoFilesControllerInstance.startActivity(new Intent(eXoFilesControllerInstance,
                                                                      eXoFilesControllerInstance.getClass()));

                  eXoFilesControllerInstance.runOnUiThread(dismissProgressDialog);
                }
              };

              showProgressDialog(true);

              thread = new Thread(loadingDataRunnable, "fileItemClickOnIcon");
              thread.start();
            }
          }
        });

        Button btn = (Button) rowView.findViewById(R.id.Button_FileAction);
        btn.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            positionOfFileItem = pos;
            // myFile = arrFiles.get(positionOfFileItem);
            ExoFileActionDialog fileActionDialog = new ExoFileActionDialog(eXoFilesControllerInstance,
                                                                           arrFiles.get(positionOfFileItem));
            // fileAction.setTitle("User guide & language setting");
            fileActionDialog.show();

          }
        });
        bindView(rowView, arrFiles.get(position));
        return (rowView);
      }

      private void bindView(View view, ExoFile file) {
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

      // AppController.configurationInstance.createLocalFileDirectory(path,
      // true);
      // AppController.configurationInstance.createLocalFileDirectory(path + "/"
      // + file, true);

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
    HttpGet get = new HttpGet(url);
    try {

      response = ExoConnectionUtils.httpClient.execute(get);
      is = response.getEntity().getContent();

    } catch (Exception e) {

    }
    return is;

  }

  // Delete file/folder method
  public static boolean deleteMethod(String url) {
    HttpResponse response;
    try {

      WebdavMethod delete = new WebdavMethod("DELETE", url);

      response = ExoConnectionUtils.httpClient.execute(delete);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return true;
      } else
        return false;

    } catch (Exception e) {
      return false;
    }
  }

  // Copy file/folder method
  public static boolean copyMethod(String source, String destination) {

    HttpResponse response;
    try {

      WebdavMethod copy = new WebdavMethod("COPY", source, destination);

      response = ExoConnectionUtils.httpClient.execute(copy);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return true;
      } else
        return false;

    } catch (Exception e) {
      return false;
    }

  }

  // Move file/folder method
  public static boolean moveMethod(String source, String destination) {
    HttpResponse response;
    try {

      WebdavMethod move = new WebdavMethod("MOVE", source, destination);

      response = ExoConnectionUtils.httpClient.execute(move);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return true;
      } else
        return false;

    } catch (Exception e) {
      return false;
    }

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
  public void changeLanguage() {
    LocalizationHelper local = LocalizationHelper.getInstance();
    String strUploadFile = "";
    String strCancel = "";
    String strEmptyPage = "";
    try {
      strUploadFile = local.getString("Upload");
      strCancel = local.getString("Cancel");
      strEmptyPage = local.getString("EmptyPage");
      strCannotBackToPreviousPage = local.getString("CannotBackToPreviousPage");
      strDownloadFileIntoSDCard = local.getString("DownloadFileInToSDCard");
    } catch (Exception e) {

    }

    _btnUploadImage.setText(strUploadFile);
    _btnCancelUploadImage.setText(strCancel);
    _textViewEmptyPage.setText(strEmptyPage);

  }
}
