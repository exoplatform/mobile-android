package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.HttpResponse;
import org.exoplatform.controller.AppController;
import org.exoplatform.controller.document.DocumentAdapter;
import org.exoplatform.controller.document.DocumentTask;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.utils.WebdavMethod;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.WaitingDialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class DocumentActivity extends MyActionBar {
  
  public static DocumentActivity   _documentActivityInstance; 
  
  ListView                         _listViewDocument;

  TextView                         _textViewEmptyPage;

  ImageView                        _imgViewUpLoadPhoto;

  ImageView                        _imgViewEmptyPage;

  Button                           _btnUploadImage;

  Button                           _btnCancelUploadImage;                                                                                                             // "/sdcard/eXo/";

  WaitingDialog                    _progressDialog;
  
  String                           _strCannotBackToPreviousPage;
  String                           _strDownloadFileIntoSDCard;

  public String                    _sdcard_temp_dir;
  public String                    _urlDocumentHome; 
  
  public DocumentAdapter           _documentAdapter;

  // Constructor
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.exofilesview);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    
    _documentActivityInstance = this;
    
    init();

    setViewUploadImage(false);
    
    AccountSetting acc = AccountSetting.getInstance();
    String userName = acc.getUsername();
    String domain = acc.getDomainName();
    
    _urlDocumentHome = ExoDocumentUtils.getDocumentUrl(userName, domain);
    
    DocumentTask documentTask = new DocumentTask(this, this, _urlDocumentHome, null, 0);
    documentTask.execute();
    
  }
  
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    finish();
    return true;
  }
  
//Key down listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    if (keyCode == KeyEvent.KEYCODE_BACK) {
      
      _documentAdapter._urlStr = ExoDocumentUtils.getParentUrl(_documentAdapter._urlStr);
      
      if(_documentAdapter._urlStr.length() < _urlDocumentHome.length())
        finish();
      else
      {
        DocumentTask documentTask = new DocumentTask(this, this, _documentAdapter._urlStr, null, 0); 
        documentTask.execute();
      }
    }

    return false;
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    init();
  }
  
  private void init() {

    if(_listViewDocument == null)
    {
      _listViewDocument = (ListView) findViewById(R.id.ListView_Files);
      
      _btnUploadImage = (Button) findViewById(R.id.ButtonUpImage);
      _btnUploadImage.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {

          setViewUploadImage(false);
          
          String sourceUrl = _documentAdapter._documentActionDialog.myFile.urlStr;
          DocumentTask documentTask = new DocumentTask(_documentActivityInstance, _documentActivityInstance, sourceUrl, null, 4);
          documentTask.execute();
        }
      });
      
      _btnCancelUploadImage = (Button) findViewById(R.id.ButtonCancel);
      _btnCancelUploadImage.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
          setViewUploadImage(false);
        }
      });
      
      _imgViewUpLoadPhoto = (ImageView) findViewById(R.id.ImageView);
      
      _imgViewEmptyPage = (ImageView) findViewById(R.id.ImageViewEmptyPage);
      _imgViewEmptyPage.setVisibility(View.INVISIBLE);
      
      _textViewEmptyPage = (TextView) findViewById(R.id.TextView_EmptyPage);
      _textViewEmptyPage.setVisibility(View.INVISIBLE);  
    }
    
    changeLanguage();
    
  }
  
  public void setDocumentAdapter() {
    
    _listViewDocument.setAdapter(_documentAdapter);
  }
  
  // Show/hide taken photo
  private void setViewUploadImage(boolean isVieweXoImage) {
    int viewImageMode;
    int viewFileMode;
    if (isVieweXoImage) {
      viewImageMode = View.VISIBLE;
      viewFileMode = View.INVISIBLE;
    } else {
      viewImageMode = View.INVISIBLE;
      viewFileMode = View.VISIBLE;
    }

    _listViewDocument.setVisibility(viewFileMode);
    _imgViewUpLoadPhoto.setVisibility(viewImageMode);
    _btnUploadImage.setVisibility(viewImageMode);
    _btnCancelUploadImage.setVisibility(viewImageMode);
  }
  
  // Refresh files view
  Runnable reloadFileAdapter = new Runnable() {

    public void run() {

      onResume();
      
      }
    };

  // Dismiss progress dialog
  public Runnable dismissProgressDialog = new Runnable() {

    public void run() {
      
      _progressDialog.dismiss();
    }
  };
  
  // Show progress dialog
  public void showProgressDialog(boolean isloadingData) {
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
    
    _progressDialog = new WaitingDialog(this, null, strLoadingDataFromServer);
    _progressDialog.show();
  }
  
//Take a photo
  public void takePicture() {
    String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
    _sdcard_temp_dir = parentPath + PhotoUltils.getImageFileName();

    Intent takePictureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    takePictureFromCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                         Uri.fromFile(new File(_sdcard_temp_dir)));
    startActivityForResult(takePictureFromCameraIntent, ExoConstants.TAKE_PICTURE_WITH_CAMERA);

  }

  // Take photo app
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == ExoConstants.TAKE_PICTURE_WITH_CAMERA || resultCode == Activity.RESULT_OK) {
      File file = new File(_sdcard_temp_dir);
      setViewUploadImage(true);
      try {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        FileInputStream fis = new FileInputStream(file);
        Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
        fis.close();
        // Bitmap bmp = (Bitmap) data.getExtras().get("data");
        _imgViewUpLoadPhoto.setImageBitmap(bitmap);
      } catch (Exception e) {
      }

    } else if (resultCode == Activity.RESULT_CANCELED) {
      return;
    }

  }
  
  
  // Delete file/folder method
  public boolean deleteFile(String url) {
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
  public boolean copyFile(String source, String destination) {

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
  
//Move file/folder method
  public boolean moveFile(String source, String destination) {
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

  // Set language
  public void changeLanguage() {
    
    LocalizationHelper local = LocalizationHelper.getInstance();
    
    String strUploadFile = local.getString("Upload");
    String strCancel = local.getString("Cancel");
    String strEmptyPage = local.getString("EmptyPage");
    _strCannotBackToPreviousPage = local.getString("CannotBackToPreviousPage");
    _strDownloadFileIntoSDCard = local.getString("DownloadFileInToSDCard");
    
    _btnUploadImage.setText(strUploadFile);
    _btnCancelUploadImage.setText(strCancel);
    _textViewEmptyPage.setText(strEmptyPage);
  }

}
