package org.exoplatform.ui;

import greendroid.util.Config;
import greendroid.widget.ActionBarItem;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.HttpResponse;
import org.exoplatform.controller.document.DocumentAdapter;
import org.exoplatform.controller.document.DocumentLoadTask;
import org.exoplatform.model.ExoFile;
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
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class DocumentActivity extends MyActionBar {

  public static DocumentActivity _documentActivityInstance;

  private ListView               _listViewDocument;

  private TextView               _textViewEmptyPage;

  private ImageView              _imgViewUpLoadPhoto;

  private Button                 _btnUploadImage;

  private Button                 _btnCancelUploadImage;    // "/sdcard/eXo/";

  private WaitingDialog          _progressDialog;

  private String                 emptyFolderString;

  public String                  _sdcard_temp_dir;

  public String                  _urlDocumentHome;

  public DocumentAdapter         _documentAdapter;

  private DocumentLoadTask       mLoadTask;

  private View                   empty_stub;

  public ExoFile                 _fileForCurrnentActionBar;

  public ExoFile                 _fileForCurrnentCell;

  // Constructor
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.exofilesview);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    _documentActivityInstance = this;
    setTitle("Documents");
    init();

    setViewUploadImage(false);

    _urlDocumentHome = ExoDocumentUtils.repositoryHomeURL;

    onLoad(_urlDocumentHome, null, 0);

  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      _documentActivityInstance = null;
      finish();
      break;
    case 0:

      if (_documentAdapter._documentActionDialog == null)
        _documentAdapter._documentActionDialog = new DocumentActionDialog(this,
                                                                          _fileForCurrnentActionBar);

      _documentAdapter._documentActionDialog._documentActionAdapter.setSelectedFile(_fileForCurrnentActionBar);
      _documentAdapter._documentActionDialog._documentActionAdapter.notifyDataSetChanged();
      _documentAdapter._documentActionDialog.setTileForDialog(_fileForCurrnentActionBar.fileName);
      _documentAdapter._documentActionDialog.myFile = _fileForCurrnentActionBar;
      _documentAdapter._documentActionDialog.show();

      break;
    default:

      break;

    }
    return true;
  }

  public void addOrRemoveFileActionButton() {

    if (_fileForCurrnentActionBar.urlStr.equalsIgnoreCase(_urlDocumentHome)) {
      getActionBar().removeItem(0);
    } else {
      if (getActionBar().getItem(0) == null) {

        addActionBarItem();
        getActionBar().getItem(0).setDrawable(R.drawable.actionbar_icon_dodument);
      }
    }

  }

  @Override
  public void onBackPressed() {
    if (_documentAdapter == null) {
      finish();
    } else {
      String url = ExoDocumentUtils.getParentUrl(_fileForCurrnentActionBar.urlStr);
      String name = ExoDocumentUtils.getLastPathComponent(url);
      _fileForCurrnentActionBar = new ExoFile(url, name, true, "text/html");

      if (_fileForCurrnentActionBar.urlStr.length() < _urlDocumentHome.length()) {
        _documentActivityInstance = null;
        finish();
      } else {
        onLoad(_fileForCurrnentActionBar.urlStr, null, 0);
      }
    }

  }

  public void onLoad(String source, String destination, int action) {
    if (mLoadTask == null || mLoadTask.getStatus() == DocumentLoadTask.Status.FINISHED) {
    	if (Config.GD_INFO_LOGS_ENABLED)
    		Log.i("DocumentLoadTask", "onLoad");
      mLoadTask = (DocumentLoadTask) new DocumentLoadTask(this, this, source, destination, action).execute();
    }
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == DocumentLoadTask.Status.RUNNING) {
    	if (Config.GD_INFO_LOGS_ENABLED)
    		Log.i("DocumentLoadTask", "onCancelLoad");
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    init();
  }

  public void uploadFile() {

    setViewUploadImage(false);

    onLoad(_documentAdapter._documentActionDialog.myFile.urlStr, null, 4);

  }

  private void init() {

    if (_listViewDocument == null) {
      _listViewDocument = (ListView) findViewById(R.id.ListView_Files);

      _btnUploadImage = (Button) findViewById(R.id.ButtonUpImage);
      _btnUploadImage.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {

          uploadFile();

        }
      });

      _btnCancelUploadImage = (Button) findViewById(R.id.ButtonCancel);
      _btnCancelUploadImage.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
          setViewUploadImage(false);
        }
      });

      _imgViewUpLoadPhoto = (ImageView) findViewById(R.id.ImageView);

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
  Runnable        reloadFileAdapter     = new Runnable() {

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
    LocalizationHelper local = LocalizationHelper.getInstance();
    String strLoadingDataFromServer = "";
    try {
      if (isloadingData)
        strLoadingDataFromServer = local.getString("LoadingDataFromServer");

      else
        strLoadingDataFromServer = local.getString("FileProcessing");

    } catch (Exception e) {

      strLoadingDataFromServer = "";
    }

    _progressDialog = new WaitingDialog(this, null, strLoadingDataFromServer);
    _progressDialog.show();
  }

  // Add a photo: camera or photo album
  public void addAPhoto() {

  }

  // Take a photo
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

  // Move file/folder method
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

  public boolean renameFolder(String source, String destination) {
    HttpResponse response;
    try {
      WebdavMethod create = new WebdavMethod("HEAD", destination);
      response = ExoConnectionUtils.httpClient.execute(create);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return false;
      } else {
        WebdavMethod move = new WebdavMethod("MOVE", source, destination);

        response = ExoConnectionUtils.httpClient.execute(move);
        status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
          return true;
        } else
          return false;
      }
    } catch (Exception e) {
      return false;
    }

  }

  public boolean createFolder(String destination) {
    HttpResponse response;
    try {

      WebdavMethod create = new WebdavMethod("HEAD", destination);
      response = ExoConnectionUtils.httpClient.execute(create);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return false;

      } else {
        create = new WebdavMethod("MKCOL", destination);
        response = ExoConnectionUtils.httpClient.execute(create);
        status = response.getStatusLine().getStatusCode();

        if (status >= 200 && status < 300) {
          return true;
        }

        return false;
      }

    } catch (Exception e) {
      return false;
    }
    // return false;
  }

  public void setEmptyView(int status) {
    if (empty_stub == null) {
      initStubView();
    }
    empty_stub.setVisibility(status);
  }

  private void initStubView() {
    empty_stub = ((ViewStub) findViewById(R.id.file_empty_stub)).inflate();
    ImageView emptyImage = (ImageView) empty_stub.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_empty_folder);
    TextView emptyStatus = (TextView) empty_stub.findViewById(R.id.empty_status);
    emptyStatus.setText(emptyFolderString);
  }

  // Set language
  public void changeLanguage() {

    LocalizationHelper local = LocalizationHelper.getInstance();

    String strUploadFile = local.getString("Upload");
    String strCancel = local.getString("Cancel");
    String strEmptyPage = local.getString("EmptyPage");
    emptyFolderString = local.getString("EmptyFolder");

    _btnUploadImage.setText(strUploadFile);
    _btnCancelUploadImage.setText(strCancel);
    _textViewEmptyPage.setText(strEmptyPage);
  }

}
