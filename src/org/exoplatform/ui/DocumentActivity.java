package org.exoplatform.ui;

import greendroid.util.Config;
import greendroid.widget.ActionBarItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.exoplatform.controller.document.DocumentAdapter;
import org.exoplatform.controller.document.DocumentLoadTask;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.social.SelectedImageActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.DocumentWaitingDialog;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.content.res.Resources;
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

  private DocumentWaitingDialog  _progressDialog;

  private String                 emptyFolderString;

  public String                  _sdcard_temp_dir;

  public String                  _urlDocumentHome;

  public DocumentAdapter         _documentAdapter;

  private DocumentLoadTask       mLoadTask;

  private View                   empty_stub;

  public ExoFile                 _fileForCurrentActionBar;

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
    _urlDocumentHome = ExoDocumentUtils.repositoryHomeURL;
    /*
     * Initialize 2 dictionaries for mapping each time document starting
     */
    DocumentHelper.getInstance().childFilesMap = new HashMap<ExoFile, ArrayList<ExoFile>>();
    DocumentHelper.getInstance().currentFileMap = new HashMap<ExoFile, ExoFile>();
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
      /*
       * Each time press on back button, return to parent folder and get parents
       * list file which is retrieved from 2 mapping dictionaries {@link
       * currentFileMap} and {@link childFileMap}
       * @param: parent The parent folder
       * @param: documentList The parents list file
       */

      if (_fileForCurrentActionBar == null) {
        _documentActivityInstance = null;
        finish();
      } else {
        if (DocumentActivity._documentActivityInstance._fileForCurrentActionBar == null)
          DocumentActivity._documentActivityInstance.setListViewPadding(5, 0, 5, 0);
        else
          DocumentActivity._documentActivityInstance.setListViewPadding(-2, 0, -2, 0);

        ExoFile parent = null;
        ArrayList<ExoFile> documentList = null;

        if (_fileForCurrentActionBar.currentFolder.equalsIgnoreCase("")) {
          _fileForCurrentActionBar = null;
          parent = DocumentHelper.getInstance().currentFileMap.get(null);

        } else {
          parent = DocumentHelper.getInstance().currentFileMap.get(_fileForCurrentActionBar);
          documentList = DocumentHelper.getInstance().childFilesMap.get(parent);
          DocumentHelper.getInstance().currentFileMap.remove(_fileForCurrentActionBar);
          _fileForCurrentActionBar = parent;
        }
        /*
         * Reset ListView
         */
        documentList = DocumentHelper.getInstance().childFilesMap.get(parent);
        _documentAdapter = new DocumentAdapter(this, documentList);
        setDocumentAdapter();
        addOrRemoveFileActionButton();

        if (_fileForCurrentActionBar == null)
          setTitle(getResources().getString(R.string.Documents));
        else
          setTitle(_fileForCurrentActionBar.name);
        setEmptyView(View.GONE);
      }

    }

  }

  private void clearMappingCache() {
    DocumentHelper.getInstance().childFilesMap.clear();
    DocumentHelper.getInstance().currentFileMap.clear();
  }

  public void onLoad(String source, String destination, int action) {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      if (mLoadTask == null || mLoadTask.getStatus() == DocumentLoadTask.Status.FINISHED) {
        if (Config.GD_INFO_LOGS_ENABLED)
          Log.i("DocumentLoadTask", "onLoad");
        mLoadTask = (DocumentLoadTask) new DocumentLoadTask(this,
                                                            this,
                                                            source,
                                                            destination,
                                                            action,
                                                            _progressDialog).execute();
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

  @Override
  public void finish() {
    if (_progressDialog != null) {
      _progressDialog.dismiss();
    }
    clearMappingCache();
    super.finish();
  }

  public void uploadFile() {
    onLoad(_documentAdapter._documentActionDialog.myFile.path, null, 4);
  }

  private void init() {

    _listViewDocument = (ListView) findViewById(R.id.ListView_Files);
    _listViewDocument.setDivider(null);
    _textViewEmptyPage = (TextView) findViewById(R.id.TextView_EmptyPage);
    _textViewEmptyPage.setVisibility(View.INVISIBLE);

    changeLanguage();

  }

  public void setDocumentAdapter() {

    _listViewDocument.setAdapter(_documentAdapter);
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
    Resources resource = getResources();
    emptyFolderString = resource.getString(R.string.EmptyFolder);
    _textViewEmptyPage.setText(emptyFolderString);
  }

}
