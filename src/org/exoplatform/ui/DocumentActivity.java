package org.exoplatform.ui;

import greendroid.util.Config;
import greendroid.widget.ActionBarItem;

import java.io.File;
import java.util.ArrayList;

import org.exoplatform.controller.document.DocumentAdapter;
import org.exoplatform.controller.document.DocumentLoadTask;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.AccountSetting;
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
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class DocumentActivity extends MyActionBar {
  private static final String    DOCUMENT_HELPER      = "document_helper";

  private static final String    DOCUMENT_SOURCE_HOME = "mSourceHome";

  private static final String    DOCUMENT_SOURCE      = "mSource";

  private static final String    DOCUMENT_DESTINATION = "mDestination";

  private static final String    DOCUMENT_ACTION      = "mAction";

  private static final String    ACCOUNT_SETTING      = "account_setting";

  private static final String    COOKIESTORE          = "cookie_store";

  private static final String    CURRENT_FILE         = "current_file";

  private String                 mSource;

  private String                 mDestination;

  private int                    mAction;

  public static DocumentActivity _documentActivityInstance;

  public ListView                _listViewDocument;

  private TextView               _textViewEmptyPage;

  private DocumentWaitingDialog  _progressDialog;

  private String                 emptyFolderString;

  public String                  _sdcard_temp_dir;

  public String                  _urlDocumentHome;

  public DocumentAdapter         _documentAdapter;

  private DocumentLoadTask       mLoadTask;

  private View                   empty_stub;

  public ExoFile                 _fileForCurrentActionBar;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.exofilesview);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    _documentActivityInstance = this;
    init();

    /*
     * Restore the current state of activity
     */
    if (bundle != null) {
      DocumentHelper helper = bundle.getParcelable(DOCUMENT_HELPER);
      DocumentHelper.getInstance().setInstance(helper);
      AccountSetting accountSetting = bundle.getParcelable(ACCOUNT_SETTING);
      AccountSetting.getInstance().setInstance(accountSetting);
      ArrayList<String> cookieList = bundle.getStringArrayList(COOKIESTORE);
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
      _fileForCurrentActionBar = bundle.getParcelable(CURRENT_FILE);
      _urlDocumentHome = bundle.getString(DOCUMENT_SOURCE_HOME);
      mSource = bundle.getString(DOCUMENT_SOURCE);
      mDestination = bundle.getString(DOCUMENT_DESTINATION);
      mAction = bundle.getInt(DOCUMENT_ACTION);
      onLoad(mSource, mDestination, mAction);
//       setDocumentAdapter(getCurrentDocumentList());
    } else {
      /*
       * Initialize 2 dictionaries for mapping each time document starting
       */
      DocumentHelper.getInstance().childFilesMap = new Bundle();
      DocumentHelper.getInstance().currentFileMap = new Bundle();
      _urlDocumentHome = ExoDocumentUtils.repositoryHomeURL;
      onLoad(_urlDocumentHome, null, 0);
    }

  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
   */
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(DOCUMENT_HELPER, DocumentHelper.getInstance());
    outState.putString(DOCUMENT_SOURCE_HOME, _urlDocumentHome);
    outState.putString(DOCUMENT_SOURCE, mSource);
    outState.putString(DOCUMENT_DESTINATION, mDestination);
    outState.putInt(DOCUMENT_ACTION, mAction);
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
    outState.putStringArrayList(COOKIESTORE,
                                ExoConnectionUtils.getCookieList(ExoConnectionUtils.cookiesStore));
    outState.putParcelable(CURRENT_FILE, _fileForCurrentActionBar);

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
    /*
     * If at the document level, make the action bar button invisible
     */
    if (_fileForCurrentActionBar == null) {
      getActionBar().removeItem(0);
    } else {
      // At the driver level, remove the action bar button
      if (_fileForCurrentActionBar.path == null) {
        getActionBar().removeItem(0);
      } else {
        if (getActionBar().getItem(0) == null) {
          addActionBarItem();
          getActionBar().getItem(0).setDrawable(R.drawable.actionbar_icon_dodument);
        }
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
        /*
         * Set animation for listview when press back button
         */
        _listViewDocument.setAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_left_to_right));

        /*
         * Reset ListView
         */

        setDocumentAdapter(getCurrentDocumentList());
      }

    }

  }

  private ArrayList<ExoFile> getCurrentDocumentList() {
    ExoFile parent = null;
    ArrayList<ExoFile> documentList = null;

    if (_fileForCurrentActionBar.currentFolder.equalsIgnoreCase("")) {
      _fileForCurrentActionBar = null;
      parent = DocumentHelper.getInstance().currentFileMap.getParcelable(null);
      documentList = DocumentHelper.getInstance().childFilesMap.getParcelableArrayList(ExoConstants.DOCUMENT_PATH);

    } else {
      parent = DocumentHelper.getInstance().currentFileMap.getParcelable(_fileForCurrentActionBar.path);
      DocumentHelper.getInstance().currentFileMap.remove(_fileForCurrentActionBar.path);
      _fileForCurrentActionBar = parent;
      if (parent == null) {
        documentList = DocumentHelper.getInstance().childFilesMap.getParcelableArrayList(null);
      } else {
        documentList = DocumentHelper.getInstance().childFilesMap.getParcelableArrayList(parent.path);
      }
    }
    return documentList;
  }

  public void onLoad(String source, String destination, int action) {
    mSource = source;
    mDestination = destination;
    mAction = action;
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
  public void finish() {
    if (_progressDialog != null) {
      _progressDialog.dismiss();
    }
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

  public void setDocumentAdapter(ArrayList<ExoFile> documentList) {
    if (_fileForCurrentActionBar == null) {
      setListViewPadding(5, 0, 5, 0);
      setTitle(getResources().getString(R.string.Documents));
    } else {
      setListViewPadding(-2, 0, -2, 0);
      setTitle(_fileForCurrentActionBar.name);
    }

    _documentAdapter = new DocumentAdapter(this, documentList);
    _listViewDocument.setAdapter(_documentAdapter);
    addOrRemoveFileActionButton();

    if (_fileForCurrentActionBar == null)
      setTitle(getResources().getString(R.string.Documents));
    else
      setTitle(_fileForCurrentActionBar.name);
    if (documentList.size() == 0) {
      setEmptyView(View.VISIBLE);
    } else
      setEmptyView(View.GONE);

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
