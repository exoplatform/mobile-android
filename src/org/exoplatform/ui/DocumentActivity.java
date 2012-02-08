package org.exoplatform.ui;

import greendroid.util.Config;
import greendroid.widget.ActionBarItem;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.exoplatform.controller.document.DocumentAdapter;
import org.exoplatform.controller.document.DocumentLoadTask;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.social.SelectedImageActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.utils.URLAnalyzer;
import org.exoplatform.utils.WebdavMethod;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.WaitingDialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class DocumentActivity extends MyActionBar {

  public static DocumentActivity _documentActivityInstance;

  private ListView               _listViewDocument;

  private TextView               _textViewEmptyPage;

  private WaitingDialog          _progressDialog;

  private String                 emptyFolderString;

  public String                  _sdcard_temp_dir;

  public String                  _urlDocumentHome;

  public DocumentAdapter         _documentAdapter;

  private DocumentLoadTask       mLoadTask;

  private View                   empty_stub;

  public ExoFile                 _fileForCurrentActionBar;

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
    init();

    // setViewUploadImage(false);
    _urlDocumentHome = ExoDocumentUtils.repositoryHomeURL;

    onLoad(_urlDocumentHome, null, 0);

  }

  public void setListViewLayoutParam(LinearLayout.LayoutParams lastTxtParams) {
    _listViewDocument.setLayoutParams(lastTxtParams);
    _listViewDocument.invalidate();
  }

  public void setListViewPadding(int l, int t, int r, int b) {
    _listViewDocument.setPadding(l, t, r, b);
    _listViewDocument.invalidate();
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
                                                                          _fileForCurrentActionBar);

      _documentAdapter._documentActionDialog._documentActionAdapter.setSelectedFile(_fileForCurrentActionBar);
      _documentAdapter._documentActionDialog._documentActionAdapter.notifyDataSetChanged();
      _documentAdapter._documentActionDialog.setTileForDialog(_fileForCurrentActionBar.name);
      _documentAdapter._documentActionDialog.myFile = _fileForCurrentActionBar;
      _documentAdapter._documentActionDialog.show();

      break;
    default:

      break;

    }
    return true;
  }

  public void addOrRemoveFileActionButton() {

    if (_fileForCurrentActionBar == null) {
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
      _documentActivityInstance = null;
      finish();
    } else {

      if (_fileForCurrentActionBar == null) {
        _documentActivityInstance = null;
        finish();

      } else if (_fileForCurrentActionBar.currentFolder.equalsIgnoreCase("")) {
        _fileForCurrentActionBar = null;
        onLoad(null, null, 0);

      } else {
        _fileForCurrentActionBar.currentFolder = ExoDocumentUtils.getParentUrl(_fileForCurrentActionBar.currentFolder);
        _fileForCurrentActionBar.name = ExoDocumentUtils.getLastPathComponent(_fileForCurrentActionBar.currentFolder);
        if (_fileForCurrentActionBar.name.equalsIgnoreCase(""))
          _fileForCurrentActionBar.name = _fileForCurrentActionBar.driveName;

        onLoad(_fileForCurrentActionBar.path, null, 0);
      }

    }

  }

  public void onLoad(String source, String destination, int action) {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      if (mLoadTask == null || mLoadTask.getStatus() == DocumentLoadTask.Status.FINISHED) {
        if (Config.GD_INFO_LOGS_ENABLED)
          Log.i("DocumentLoadTask", "onLoad");
        mLoadTask = (DocumentLoadTask) new DocumentLoadTask(this, this, source, destination, action).execute();
      }
    } else {
      new ConnectionErrorDialog(this).show();
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
    onLoad(_documentAdapter._documentActionDialog.myFile.path, null, 4);

  }

  private void init() {

    if (_listViewDocument == null) {
      _listViewDocument = (ListView) findViewById(R.id.ListView_Files);
      _listViewDocument.setDivider(null);
      _textViewEmptyPage = (TextView) findViewById(R.id.TextView_EmptyPage);
      _textViewEmptyPage.setVisibility(View.INVISIBLE);
    }

    changeLanguage();

  }

  public void setDocumentAdapter() {

    _listViewDocument.setAdapter(_documentAdapter);
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
    if (isloadingData)
      strLoadingDataFromServer = local.getString("LoadingDataFromServer");
    else
      strLoadingDataFromServer = local.getString("FileProcessing");
    _progressDialog = new WaitingDialog(this, null, strLoadingDataFromServer);
    _progressDialog.show();
  }

  // Take a photo
  public void takePicture() {
    String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
    _sdcard_temp_dir = parentPath + PhotoUtils.getImageFileName();

    Intent takePictureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    takePictureFromCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                         Uri.fromFile(new File(_sdcard_temp_dir)));
    startActivityForResult(takePictureFromCameraIntent, ExoConstants.TAKE_PICTURE_WITH_CAMERA);

  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
      case ExoConstants.TAKE_PICTURE_WITH_CAMERA:
        Intent intent1 = new Intent(_documentActivityInstance, SelectedImageActivity.class);
        intent1.putExtra(ExoConstants.SELECTED_IMAGE_EXTRA, _sdcard_temp_dir);
        startActivity(intent1);
        break;

      case ExoConstants.REQUEST_ADD_PHOTO:
        Intent intent2 = new Intent(this, SelectedImageActivity.class);
        intent.putExtra(ExoConstants.SELECTED_IMAGE_MODE, 2);
        intent2.setData(intent.getData());
        if (intent.getExtras() != null) {
          intent2.putExtras(intent.getExtras());
        }
        startActivity(intent2);
        break;
      }
    }
  }

  // Delete file/folder method
  public boolean deleteFile(String url) {
    HttpResponse response;
    try {
      url = URLAnalyzer.encodeUrl(url);
      WebdavMethod delete = new WebdavMethod("DELETE", url);
      response = ExoConnectionUtils.httpClient.execute(delete);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return true;
      } else
        return false;

    } catch (IOException e) {
      return false;
    }

  }

  // Copy file/folder method
  public boolean copyFile(String source, String destination) {

    HttpResponse response;
    try {
      source = URLAnalyzer.encodeUrl(source);
      destination = URLAnalyzer.encodeUrl(destination);
      WebdavMethod copy = new WebdavMethod("COPY", source, destination);
      response = ExoConnectionUtils.httpClient.execute(copy);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return true;
      } else
        return false;

    } catch (IOException e) {
      return false;
    }
  }

  // Move file/folder method
  public boolean moveFile(String source, String destination) {
    HttpResponse response;
    try {

      source = URLAnalyzer.encodeUrl(source);
      destination = URLAnalyzer.encodeUrl(destination);
      WebdavMethod move = new WebdavMethod("MOVE", source, destination);
      response = ExoConnectionUtils.httpClient.execute(move);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return true;
      } else
        return false;

    } catch (IOException e) {
      return false;
    }

  }

  public boolean renameFolder(String source, String destination) {
    HttpResponse response;
    try {
      source = URLAnalyzer.encodeUrl(source);
      destination = URLAnalyzer.encodeUrl(destination);
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
    } catch (IOException e) {
      return false;
    }

  }

  public boolean createFolder(String destination) {
    HttpResponse response;
    try {

      destination = URLAnalyzer.encodeUrl(destination);
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

    } catch (IOException e) {
      return false;
    }
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
    String strEmptyPage = local.getString("EmptyPage");
    emptyFolderString = local.getString("EmptyFolder");
    _textViewEmptyPage.setText(strEmptyPage);
  }

}
