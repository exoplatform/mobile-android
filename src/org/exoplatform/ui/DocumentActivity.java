/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.R;
import org.exoplatform.controller.document.DocumentAdapter;
import org.exoplatform.controller.document.DocumentLoadTask;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.social.SelectedImageActivity;
import org.exoplatform.utils.CrashUtils;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.ConnectionErrorDialog;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DocumentActivity extends Activity {
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

  private MenuItem               mDocAction;

  private static final String    DOCUMENT_HELPER  = "document_helper";

  private static final String    ACCOUNT_SETTING  = "account_setting";

  private static final String    CURRENT_FILE     = "current_file";

  public static DocumentActivity _documentActivityInstance;

  public ListView                _listViewDocument;

  private TextView               _textViewEmptyPage;

  private String                 emptyFolderString;

  public String                  _sdcard_temp_dir;

  public DocumentAdapter         _documentAdapter;

  private DocumentLoadTask       mLoadTask;

  private View                   empty_stub;

  public ExoFile                 _fileForCurrentActionBar;

  private static final String    TAG              = "eXo____DocumentActivity____";

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    setContentView(R.layout.exofilesview);
    setTitle(R.string.Documents);
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
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, AccountSetting.getInstance().cookiesList);
      _fileForCurrentActionBar = bundle.getParcelable(CURRENT_FILE);
    } else {
      /*
       * Initialize 2 dictionaries for mapping each time document starting
       */
      DocumentHelper.getInstance().folderToChildrenMap = new Bundle();
      DocumentHelper.getInstance().folderToParentMap = new Bundle();
      _fileForCurrentActionBar = new ExoFile();
      setTitle(R.string.Documents);
    }
    onLoad(_fileForCurrentActionBar, null, ACTION_DEFAULT);

  }

  /*
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.document, menu);
    mDocAction = menu.findItem(R.id.menu_doc_action);
    addOrRemoveFileActionButton();
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

    case android.R.id.home:
      _documentActivityInstance = null;
      finish();
      break;
    case R.id.menu_doc_action:

      _documentAdapter._documentActionDialog = new DocumentActionDialog(this, _fileForCurrentActionBar, true);
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
     * If at the document level or driver level, make the action bar button
     * invisible
     */
    if (mDocAction == null)
      return;

    if (_fileForCurrentActionBar == null) {
      mDocAction.setVisible(false);
    } else {
      if (_fileForCurrentActionBar.name == null) {
        mDocAction.setVisible(false);
      } else if ("".equals(_fileForCurrentActionBar.name)) {
        mDocAction.setVisible(false);
      } else {
        mDocAction.setVisible(true);
      }
    }
  }

  @Override
  public void onBackPressed() {
    onCancelLoad();
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
      if ("".equals(_fileForCurrentActionBar.name)) {
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
        updateContent(getParentFolderAndChildren());
      }
    }
  }

  private ExoFile getParentFolderAndChildren() {
    ArrayList<ExoFile> documentList = null;

    if ("".equals(_fileForCurrentActionBar.currentFolder)) {
      // "" as currentFolder means we're at the root of the drive
      // so we go back to the list of drives
      _fileForCurrentActionBar = new ExoFile();
      documentList = DocumentHelper.getInstance().folderToChildrenMap.getParcelableArrayList(ExoConstants.DOCUMENT_JCR_PATH);
    } else {
      String currPath = _fileForCurrentActionBar.path;
      // replace the current folder by its parent
      _fileForCurrentActionBar = DocumentHelper.getInstance().folderToParentMap.getParcelable(currPath);
      // remove the former current folder from memory
      DocumentHelper.getInstance().folderToParentMap.remove(currPath);
      // get the list of children of the new current folder
      if ("".equals(_fileForCurrentActionBar.name)) {
        documentList = DocumentHelper.getInstance().folderToChildrenMap.getParcelableArrayList("");
      } else {
        documentList = DocumentHelper.getInstance().folderToChildrenMap.getParcelableArrayList(_fileForCurrentActionBar.path);
      }
    }
    // link the current folder with the list of its children
    _fileForCurrentActionBar.children = documentList;
    return _fileForCurrentActionBar;
  }

  public void loadFolderContent(ExoFile parent) {
    onLoad(parent, null, DocumentActivity.ACTION_DEFAULT);
  }

  public void deleteFile(ExoFile fileToDelete) {
    onLoad(fileToDelete, fileToDelete.path, DocumentActivity.ACTION_DELETE);
  }

  public void pasteFile(ExoFile fileToPaste, String destination, int action) {
    onLoad(fileToPaste, destination, action);
  }

  public void uploadFile() {
    onLoad(_documentAdapter._documentActionDialog.myFile, null, ACTION_ADD_PHOTO);
  }

  public void createFile(ExoFile fileToCreate, String destination) {
    onLoad(fileToCreate, destination, DocumentActivity.ACTION_CREATE);
  }

  public void renameFile(ExoFile fileToRename, String destination) {
    onLoad(fileToRename, destination, DocumentActivity.ACTION_RENAME);
  }

  private void onLoad(ExoFile source, String destination, int action) {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      if (mLoadTask == null || mLoadTask.getStatus() == DocumentLoadTask.Status.FINISHED) {
        Log.i("DocumentLoadTask", "onLoad");
        mLoadTask = (DocumentLoadTask) new DocumentLoadTask(this, source, destination, action).execute();
      }
    } else {
      new ConnectionErrorDialog(this).show();
    }
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == DocumentLoadTask.Status.RUNNING) {
      Log.i("DocumentLoadTask", "onCancelLoad");
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }
  
  @Override
  protected void onPause() {
    onCancelLoad();
    super.onPause();
  }
  
  @Override
  protected void onDestroy() {
    _documentActivityInstance = null;
    super.onDestroy();
  }

  private void init() {
    _listViewDocument = (ListView) findViewById(R.id.ListView_Files);
    _listViewDocument.setDivider(null);
    _textViewEmptyPage = (TextView) findViewById(R.id.TextView_EmptyPage);
    _textViewEmptyPage.setVisibility(View.INVISIBLE);

    changeLanguage();

  }

  public void updateContent(ExoFile newFolder) {
    _fileForCurrentActionBar = newFolder;
    if ("".equals(_fileForCurrentActionBar.name)) {
      setTitle(getResources().getString(R.string.Documents));
    } else {
      setTitle(_fileForCurrentActionBar.getName());
    }
    List<ExoFile> documentList = newFolder.children;
    if (documentList == null) {
      CrashUtils.loge(TAG, String.format("Null list of children for folder '%s'", newFolder.path));
      documentList = new ArrayList<ExoFile>(0);
    }
    if (documentList.size() == 0) {
      setEmptyView(View.VISIBLE);
    } else
      setEmptyView(View.GONE);

    _documentAdapter = new DocumentAdapter(this, documentList);
    _listViewDocument.setAdapter(_documentAdapter);
    addOrRemoveFileActionButton();
  }

  /**
   * Take a photo and store it into /sdcard/eXo/DocumentCache
   **/
  public void takePicture() {
    String parentPath = PhotoUtils.getParentImagePath(this);
    _sdcard_temp_dir = parentPath + "/" + PhotoUtils.getImageFileName();

    Intent takePictureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    takePictureFromCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(_sdcard_temp_dir)));
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
