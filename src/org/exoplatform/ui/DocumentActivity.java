package org.exoplatform.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
//import greendroid.util.Config;
//import greendroid.widget.ActionBarItem;

import java.io.File;
import java.util.ArrayList;

import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import org.exoplatform.R;
import org.exoplatform.controller.document.DocumentAdapter;
import org.exoplatform.controller.document.DocumentLoadTask;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.setting.SettingActivity;
import org.exoplatform.ui.social.SelectedImageActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.ConnTimeOutDialog;
import org.exoplatform.widget.ConnectionErrorDialog;
//import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.exoplatform.widget.UnreadableFileDialog;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import static android.support.v7.widget.GridLayout.ALIGN_BOUNDS;

public class DocumentActivity extends ActionBarActivity implements DocumentLoadTask.AsyncTaskListener {

  //extends MyActionBar {

  // add photo
  public static final int        ACTION_ADD_PHOTO = 0;

  // copy file
  public static final int        ACTION_COPY      = 1;

  // move file
  public static final int        ACTION_MOVE      = 2;

  // paste file
  public static final int        ACTION_PASTE     = 3;

  // delete file or folder
  public static final int        ACTION_DELETE    = 4;

  // rename folder
  public static final int        ACTION_RENAME    = 5;

  // create new folder
  public static final int        ACTION_CREATE    = 6;

  // open in
  public static final int        ACTION_OPEN_IN   = 7;

  // default
  public static final int        ACTION_DEFAULT   = 8;

  private static final String    DOCUMENT_HELPER  = "document_helper";

  private static final String    ACCOUNT_SETTING  = "account_setting";

  private static final String    CURRENT_FILE     = "current_file";

  //public static DocumentActivity _documentActivityInstance;

  public static DocumentActivity instance;

  private TextView               _textViewEmptyPage;

  private String                 emptyFolderString;

  public String                  _sdcard_temp_dir;


  private View                   empty_stub;

  public ExoFile                 _fileForCurrentActionBar;

  public DocumentAdapter         mDocumentAdapter;

  private DocumentLoadTask       mLoadTask;

  private WaitingDialog          mProgressDialog;

  private ArrayList<ExoFile>     mDocumentList;

  private Menu                   mOptionsMenu;

  private SharedPreferences      mSharedPreference;

  private ViewGroup              mRootView;

  /** list view of document list */
  private ListView               mDocumentListView;

  /** grid view of document list - a grid view contained inside a scroll view */
  private ScrollView             mDocumentGridView;

  /** view mode for documents: list or grid */
  private int                    mViewMode;

  /**=== DATA FOR AsyncTask ===*/
  public static final String     DOC_SOURCE              = "DOC_SOURCE";

  public static final String     DOC_DESTINATION         = "DOC_DESTINATION";

  public static final String     ACTION_ID               = "ACTION_ID";

  public static final String     CURRENT_ACTION_BAR_FILE = "CURRENT_ACTION_BAR_FILE";

  public static final String     CURRENT_MENU_FILE       = "CURRENT_MENU_FILE";

  public static final String     SDCARD_DIR              = "SDCARD_DIR";

  private static final String    TAG = "eXo____DocumentActivity____";


  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    setTitle(getString(R.string.Documents));

    mRootView = (ViewGroup) getLayoutInflater().inflate(R.layout.exofilesview, null, false);
    setContentView(mRootView);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    //setTheme(R.style.Theme_eXo);
    //setActionBarContentView(R.layout.exofilesview);
    //getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    //_documentActivityInstance = this;

    instance = this;
    mSharedPreference = getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    /** Default view is list view */
    mViewMode         = mSharedPreference.getInt(ExoConstants.EXO_DOCUMENT_VIEW, ExoConstants.VIEW_AS_LIST);

    init();

    /*
     * Restore the current state of activity
     */
    if (bundle != null) {
      DocumentHelper helper = bundle.getParcelable(DOCUMENT_HELPER);
      DocumentHelper.getInstance().setInstance(helper);
      AccountSetting accountSetting = bundle.getParcelable(ACCOUNT_SETTING);
      AccountSetting.getInstance().setInstance(accountSetting);
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore,
                                        AccountSetting.getInstance().cookiesList);
      _fileForCurrentActionBar = bundle.getParcelable(CURRENT_FILE);
    } else {
      /** Initialize 2 dictionaries for mapping each time document starting */
      DocumentHelper.getInstance().childFilesMap = new Bundle();
      DocumentHelper.getInstance().currentFileMap = new Bundle();
      _fileForCurrentActionBar = new ExoFile();
      setTitle(getResources().getString(R.string.Documents));
    }

    startLoadingDocuments(DocumentHelper.getInstance().getRepositoryHomeUrl(), null, ACTION_DEFAULT);
  }


  private void init() {
    //mDocumentListView = (ListView) findViewById(R.id.ListView_Files);
    //mDocumentListView.setDivider(null);
    _textViewEmptyPage = (TextView) findViewById(R.id.TextView_EmptyPage);
    _textViewEmptyPage.setVisibility(View.INVISIBLE);

    changeLanguage();
  }

  /**
   * (non-Javadoc)
   * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
   */
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(DOCUMENT_HELPER, DocumentHelper.getInstance());
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
    outState.putParcelable(CURRENT_FILE, _fileForCurrentActionBar);
  }


  /**    TODO - replace
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      _documentActivityInstance = null;
      finish();
      break;
    case 0:

      _documentAdapter._documentActionDialog = new DocumentActionDialog(this,
                                                                        _fileForCurrentActionBar,
                                                                        true);
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
   **/


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.i(TAG, "onCreateOptionsMenu");
    getMenuInflater().inflate(R.menu.documents, menu);
    mOptionsMenu = menu;

    /** Current icon should be opposite of view mode */
    MenuItem viewItem = mOptionsMenu.findItem(R.id.menu_view);
    viewItem.setIcon(mViewMode == ExoConstants.VIEW_AS_LIST
        ? R.drawable.ic_action_view_as_grid : R.drawable.ic_action_view_as_list);

    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;

      case R.id.menu_view:
        mViewMode = mViewMode == ExoConstants.VIEW_AS_LIST ? ExoConstants.VIEW_AS_GRID
            : ExoConstants.VIEW_AS_LIST;

        /** Change icon */
        item.setIcon(mViewMode == ExoConstants.VIEW_AS_LIST ? R.drawable.ic_action_view_as_grid
          : R.drawable.ic_action_view_as_list);

        switchViewMode(false);
        break;

      case R.id.menu_add:
        /** Open up Add menu */
        if ("".equals(_fileForCurrentActionBar.name)) return true;

        mDocumentAdapter.mActionDialog = new DocumentActionDialog(this, _fileForCurrentActionBar, true);
        mDocumentAdapter.mActionDialog._documentActionAdapter.setSelectedFile(_fileForCurrentActionBar);
        mDocumentAdapter.mActionDialog._documentActionAdapter.notifyDataSetChanged();
        mDocumentAdapter.mActionDialog.setTileForDialog(_fileForCurrentActionBar.name);
        mDocumentAdapter.mActionDialog.myFile = _fileForCurrentActionBar;
        mDocumentAdapter.mActionDialog.show();
        break;

      case R.id.menu_refresh:
        startLoadingDocuments(_fileForCurrentActionBar.path, null, ACTION_DEFAULT);
        return true;

      case R.id.menu_settings:
        redirectToSetting();
        break;

    }

    return super.onOptionsItemSelected(item);
  }


  public void startLoadingDocuments(String source, String destination, int action) {
    Log.i(TAG, "startLoadingDocuments - source : " + source + " - dest : " + destination);
    Log.i(TAG, "startLoadingDocuments - action : " + action);

    if (_fileForCurrentActionBar != null) {
      Log.i(TAG, "startLoadingDocuments - file for action bar folder : " + _fileForCurrentActionBar.currentFolder);
      Log.i(TAG, "startLoadingDocuments - file for action bar name : " + _fileForCurrentActionBar.name);
    }

    if (!ExoConnectionUtils.isNetworkAvailableExt(this)) {
      new ConnectionErrorDialog(this).show();
      return ;
    }

    if (mLoadTask == null || mLoadTask.getStatus() == DocumentLoadTask.Status.FINISHED) {
      mProgressDialog = new WaitingDialog (this, null, getString(R.string.LoadingData)) {

        @Override
        public void onBackPressed() {
          super.onBackPressed();
          onCancelLoad();
        }

      };

      mProgressDialog.show();

      Bundle requestData = new Bundle();
      requestData.putString(DOC_SOURCE, source);
      requestData.putString(DOC_DESTINATION, destination);
      requestData.putInt(ACTION_ID, action);
      requestData.putParcelable(CURRENT_ACTION_BAR_FILE, _fileForCurrentActionBar);

      if (action == ACTION_RENAME)
        requestData.putParcelable(CURRENT_MENU_FILE, mDocumentAdapter.mActionDialog.myFile);
      else if (action == ACTION_ADD_PHOTO)
        requestData.putString(SDCARD_DIR, _sdcard_temp_dir);

      //mLoadTask = new DocumentLoadTask(this, source, destination, action);
      mLoadTask = new DocumentLoadTask(this, requestData);
      mLoadTask.setListener(this);
      mLoadTask.execute();
    }
  }


  @Override
  public void onLoadingDocumentsFinished(int result, int actionId, ArrayList<ExoFile> documentList) {

    if (result == DocumentLoadTask.RESULT_OK) {

      mDocumentList = documentList;
      switchViewMode(true);
    }
    else if (result == DocumentLoadTask.RESULT_ERROR) {

      String warningStr = "";
      switch (actionId) {

        case DocumentActivity.ACTION_DELETE:
          warningStr = getString(R.string.DocumentCannotDelete);
          break;

        case DocumentActivity.ACTION_COPY:
          warningStr = getString(R.string.DocumentCopyPasteError);
          break;

        case DocumentActivity.ACTION_MOVE:
          warningStr = getString(R.string.DocumentCopyPasteError);
          break;

        case DocumentActivity.ACTION_ADD_PHOTO:
          warningStr = getString(R.string.DocumentUploadError);
          break;

        case DocumentActivity.ACTION_RENAME:
          warningStr = getString(R.string.DocumentRenameError);
          break;

        case DocumentActivity.ACTION_CREATE:
          warningStr = getString(R.string.DocumentCreateFolderError);
          break;
      }

      new WarningDialog(this, getString(R.string.Warning), warningStr, getString(R.string.OK)).show();

    } else if (result == DocumentLoadTask.RESULT_TIMEOUT) {
      new ConnTimeOutDialog(this, getString(R.string.Warning), getString(R.string.OK)).show();
    } else if (result == DocumentLoadTask.RESULT_FALSE) {
      new WarningDialog(this, getString(R.string.Warning), getString(R.string.LoadingDataError), getString(R.string.OK)).show();
    }

    mProgressDialog.dismiss();
  }


  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == DocumentLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }


  private void redirectToSetting() {
    Intent next = new Intent(this, SettingActivity.class);
    next.putExtra(ExoConstants.SETTING_TYPE, SettingActivity.PERSONAL_TYPE);
    startActivity(next);
  }


  // TODO
  public void addOrRemoveFileActionButton() {
    /*
     * If at the document level or driver level, make the action bar button
     * invisible
     */
    if (_fileForCurrentActionBar == null) {
      //getActionBar().removeItem(0);
    } else {
      if (_fileForCurrentActionBar.name == null) {
        //getActionBar().removeItem(0);
      } else if ("".equals(_fileForCurrentActionBar.name)
          || "".equals(_fileForCurrentActionBar.path)) {
        //getActionBar().removeItem(0);
      } else {
        //if (getActionBar().getItem(0) == null) {
          //addActionBarItem();
          //getActionBar().getItem(0).setDrawable(R.drawable.actionbar_icon_dodument);
        //}
      }
    }
  }

  /**
   * Each time press on back button, return to parent folder and get parents
   * list file which is retrieved from 2 mapping dictionaries {@see
   * currentFileMap} and {@see childFileMap}
   */
  @Override
  public void onBackPressed() {
    Log.i(TAG, "onBackPressed");
    onCancelLoad();

    Log.i(TAG, "_fileForCurrentActionBar.name: " + _fileForCurrentActionBar.name);
    Log.i(TAG, "_fileForCurrentActionBar.path: " + _fileForCurrentActionBar.path);

    /** root folder */
    if (_fileForCurrentActionBar.name.equals("")) {
      instance = null;
      finish();
      return ;
    }

    /** Set animation for list view when press back button */
    //if (mDocumentListView != null)
    //mDocumentListView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_left_to_right));

    /** Reset the document list */
    mDocumentList = getCurrentDocumentList();
    switchViewMode(true);
  }


  private ArrayList<ExoFile> getCurrentDocumentList() {
    ExoFile parent;
    ArrayList<ExoFile> documentList;
    DocumentHelper helper = DocumentHelper.getInstance();

    Log.i(TAG, "getCurrentDocumentList");
    Log.i(TAG, "_fileForCurrentActionBar.currentFolder: " + _fileForCurrentActionBar.currentFolder);

    /** parent is root folder */
    if ("".equals(_fileForCurrentActionBar.currentFolder)) {
      _fileForCurrentActionBar = new ExoFile();
      //parent       = helper.currentFileMap.getParcelable("");
      return helper.childFilesMap.getParcelableArrayList(ExoConstants.DOCUMENT_JCR_PATH);
    }

    parent = helper.currentFileMap.getParcelable(_fileForCurrentActionBar.path);
    helper.currentFileMap.remove(_fileForCurrentActionBar.path);
    _fileForCurrentActionBar = parent;

    if (parent != null) {
      Log.i(TAG, "parent.name: " + parent.name);
      Log.i(TAG, "parent.path: " + parent.path);
    }
    else
      Log.i(TAG, "parent is null");

    if (parent == null || "".equals(parent.name)) {
      documentList = helper.childFilesMap.getParcelableArrayList("");
    } else {
      documentList = helper.childFilesMap.getParcelableArrayList(parent.path);
    }

    return documentList;
  }


  public void uploadFile() {
    startLoadingDocuments(mDocumentAdapter.mActionDialog.myFile.path, null, ACTION_ADD_PHOTO);
  }


  private void switchViewMode(boolean forceReloadView) {
    if ("".equals(_fileForCurrentActionBar.name)) {
      setTitle(getString(R.string.Documents));
    } else {
      setTitle(_fileForCurrentActionBar.name);
    }

    setEmptyView(mDocumentList.size() == 0 ? View.VISIBLE : View.GONE);

    /** remove the last child to populate new view */
    View lastChild = mRootView.getChildAt(mRootView.getChildCount() - 1);
    if (lastChild instanceof ScrollView || lastChild instanceof ListView) {
      mRootView.removeView(lastChild);
    }

    if (mDocumentList.size() > 0) {
      if      (mViewMode == ExoConstants.VIEW_AS_LIST) switchDocumentToListView(forceReloadView);
      else if (mViewMode == ExoConstants.VIEW_AS_GRID) switchDocumentToGridView(forceReloadView);
    }
  }


  private void switchDocumentToListView(boolean forceReloadView) {
    Log.i(TAG, "switchDocumentToListView: " + forceReloadView);

    /** Replace current grid view by old list view */
    if (mDocumentListView != null && !forceReloadView) {
      mRootView.addView(mDocumentListView);
      return ;
    }

    /** To make sure we create a new grid view */
    if (forceReloadView) mDocumentGridView = null;
    mDocumentListView = new ListView(this);
    mDocumentListView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,
        ListView.LayoutParams.MATCH_PARENT));
    mDocumentListView.setCacheColorHint(Color.WHITE);
    mDocumentListView.setDividerHeight(0);
    mDocumentListView.setFadingEdgeLength(0);
    //mDocumentListView.setPadding(5, 0, 5, 0);

    mRootView.addView(mDocumentListView);

    mDocumentAdapter = new DocumentAdapter(this, mDocumentList);
    mDocumentListView.setAdapter(mDocumentAdapter);

    /** Set animation for list view when access to folder */
    mDocumentListView.setAnimation(AnimationUtils.loadAnimation(this,
        R.anim.anim_right_to_left));

    //addOrRemoveFileActionButton();

  }


  private void switchDocumentToGridView(boolean forceReloadView) {
    Log.i(TAG, "switchDocumentToGridView: " + forceReloadView);

    /** Replace current list view by old grid view */
    if (mDocumentGridView != null && !forceReloadView) {
      mRootView.addView(mDocumentGridView);
      return ;
    }

    /** To make sure we create a new list view */
    if (forceReloadView) mDocumentListView = null;
    mDocumentGridView = new ScrollView(this);
    mDocumentGridView.setBackgroundColor(android.R.color.transparent);
    mDocumentGridView.setLayoutParams(new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

    GridLayout gridLayout = new GridLayout(this);
    gridLayout.setUseDefaultMargins(true);
    gridLayout.setAlignmentMode(ALIGN_BOUNDS);
    gridLayout.setPadding(0, 0, 5, 0);
    gridLayout.setColumnOrderPreserved(false);
    gridLayout.setColumnCount(2);
    gridLayout.setRowCount(Math.round(mDocumentList.size() / 2) + 1);

    GridLayout.Spec col0 = GridLayout.spec(0);
    GridLayout.Spec col1 = GridLayout.spec(1);

    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View documentView;

    Point size = new Point();
    getWindowManager().getDefaultDisplay().getSize(size);
    int screenWidth     = size.x;
    int halfScreenWidth = (int) (screenWidth * 0.5);

    /** Counting the number of element shown */
    int elementCount = 0;
    for (ExoFile file: mDocumentList) {

      /** view is tab with text - does not show */
      if ("".equals(file.name) && "".equals(file.path)) {
        continue;
      }
      /** view is a folder or file */
      else {

        documentView = inflater.inflate(R.layout.file_item, gridLayout, false);

        //Button btnAction = (Button)    documentView.findViewById(R.id.Button_FileAction);
        ImageView icon   = (ImageView) documentView.findViewById(R.id.file_icon);
        TextView lb      = (TextView)  documentView.findViewById(R.id.file_label);
        lb.setText(file.name);
        lb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        lb.setEllipsize(TextUtils.TruncateAt.END);
        lb.setMaxLines(2);

        documentView.setBackgroundResource(R.drawable.dashboard_single_background_shape);

        /**
        if (i == 0) {
          if (mDocumentList.size() == 1)
            documentView.setBackgroundResource(R.drawable.dashboard_single_background_shape);
          else {
            documentView.setBackgroundResource(R.drawable.dashboard_top_background_shape);
          }
        } else {

          if (i + 1 == mDocumentList.size())
            documentView.setBackgroundResource(R.drawable.dasboard_bottom_background_shape);
          else {
            ExoFile previousItem = mDocumentList.get(i - 1);
            ExoFile nextItem = mDocumentList.get(i + 1);

            if ("".equals(previousItem.name) && "".equals(previousItem.path)
                && "".equals(nextItem.name) && "".equals(nextItem.path)) {
              documentView.setBackgroundResource(R.drawable.dashboard_single_background_shape);
            } else if ("".equals(previousItem.name) && "".equals(previousItem.path)) {
              documentView.setBackgroundResource(R.drawable.dashboard_top_background_shape);
            } else if ("".equals(nextItem.name) && "".equals(nextItem.path))
              documentView.setBackgroundResource(R.drawable.dasboard_bottom_background_shape);
            else
              documentView.setBackgroundResource(R.drawable.dashboard_middle_background_shape);
          }
        }
        **/

        if (!file.isFolder) {
          //btnAction.setVisibility(View.VISIBLE);
          icon.setImageResource(ExoDocumentUtils.getIconFromType(file.nodeType));
        } else {
          icon.setImageResource(R.drawable.documenticonforfolder);
        }

        final ExoFile _file  = file;
        final ExoFile parent = _fileForCurrentActionBar;

        documentView.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            /** Open file or folder using appropriate app */
            if (!_file.isFolder) {
              if (ExoDocumentUtils.isFileReadable(_file.nodeType)) {
                ExoDocumentUtils.fileOpen(DocumentActivity.this, _file.nodeType, _file.path, _file.name);
              } else {
                new UnreadableFileDialog(DocumentActivity.this, null).show();
              }
            } else {
              _fileForCurrentActionBar = _file;

              /** Put the selected file and its parent to mapping dictionary */
              DocumentHelper.getInstance().currentFileMap.putParcelable(_file.path, parent);
              DocumentActivity.this.startLoadingDocuments(_file.path, null, DocumentActivity.ACTION_DEFAULT);
            }
          }
        });


        if (!"".equals(file.currentFolder) || !file.isFolder) {

          /** If current folder is null, then file is in root folder, then action button invisible */
          documentView.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View view) {
              DocumentActionDialog actionDialog = new DocumentActionDialog(DocumentActivity.this, _file, false);
              actionDialog.myFile = _file;
              actionDialog._documentActionAdapter.setSelectedFile(_file);
              actionDialog._documentActionAdapter.notifyDataSetChanged();
              actionDialog.setTileForDialog(_file.name);
              actionDialog.show();
              return true;
            }
          });
        }

        /**
         btnAction.setOnClickListener(new View.OnClickListener() {

         public void onClick(View v) {
         ExoFile file = mDocumentList.get(pos);
         mActionDialog = new DocumentActionDialog(mContext, file, false);
         mActionDialog.myFile = file;
         mActionDialog._documentActionAdapter.setSelectedFile(file);
         mActionDialog._documentActionAdapter.notifyDataSetChanged();
         mActionDialog.setTileForDialog(file.name);
         mActionDialog.show();
         }
         });
         return convertView;
         **/

      }

      GridLayout.Spec row  = GridLayout.spec(  (elementCount - (elementCount % 2)) / 2 );
      GridLayout.LayoutParams layoutParams = elementCount % 2 == 0 ? new GridLayout.LayoutParams(row, col0)
          : new GridLayout.LayoutParams(row, col1);
      layoutParams.width  = halfScreenWidth - 10;
      layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
      elementCount++;
      gridLayout.addView(documentView, layoutParams);
    }

    mDocumentGridView.addView(gridLayout);
    mRootView.addView(mDocumentGridView);
  }


  @Override
  protected void onPause() {
    super.onPause();

    /** save view mode */
    SharedPreferences.Editor editor = getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0).edit();
    editor.putInt(ExoConstants.EXO_DOCUMENT_VIEW, mViewMode);
    editor.commit();
  }


  /**
   * Take a photo and store it into /sdcard/eXo/DocumentCache
   */
  public void takePicture() {
    String parentPath = PhotoUtils.getParentImagePath(this);
    _sdcard_temp_dir = parentPath + "/" + PhotoUtils.getImageFileName();

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
          Intent intent1 = new Intent(this, SelectedImageActivity.class);
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

    /*
     * Set default language to our application setting language
     */
    SettingUtils.setDefaultLanguage(this);
  }

  private void setEmptyView(int status) {
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
  private void changeLanguage() {
    Resources resource = getResources();
    emptyFolderString = resource.getString(R.string.EmptyFolder);
    _textViewEmptyPage.setText(emptyFolderString);
  }

}
