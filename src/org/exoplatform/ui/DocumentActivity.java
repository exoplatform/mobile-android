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

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import greendroid.util.Config;
import greendroid.widget.ActionBarItem;

import java.io.File;
import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.document.DocumentAdapter;
import org.exoplatform.controller.document.DocumentLoadTask;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.social.SelectedImageActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DocumentActivity extends MyActionBar {
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

  public static DocumentActivity _documentActivityInstance;

  public ListView                _listViewDocument;

  private TextView               _textViewEmptyPage;

  private String                 emptyFolderString;

  public String                  _sdcard_temp_dir;

  public DocumentAdapter         _documentAdapter;

  private DocumentLoadTask       mLoadTask;

  private View                   empty_stub;

  public ExoFile                 _fileForCurrentActionBar;

  private static final String TAG = "eXo____DocumentActivity____";


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
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore,
                                        AccountSetting.getInstance().cookiesList);
      _fileForCurrentActionBar = bundle.getParcelable(CURRENT_FILE);
    } else {
      /*
       * Initialize 2 dictionaries for mapping each time document starting
       */
      DocumentHelper.getInstance().childFilesMap = new Bundle();
      DocumentHelper.getInstance().currentFileMap = new Bundle();
      _fileForCurrentActionBar = new ExoFile();
      setTitle(getResources().getString(R.string.Documents));
    }
    onLoad(DocumentHelper.getInstance().getRepositoryHomeUrl(), null, ACTION_DEFAULT);

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

  public void addOrRemoveFileActionButton() {
    /*
     * If at the document level or driver level, make the action bar button
     * invisible
     */
    if (_fileForCurrentActionBar == null) {
      getActionBar().removeItem(0);
    } else {
      if (_fileForCurrentActionBar.name == null) {
        getActionBar().removeItem(0);
      } else if ("".equals(_fileForCurrentActionBar.name)
          || "".equals(_fileForCurrentActionBar.path)) {
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

      if (_fileForCurrentActionBar.name.equals("")) {
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

    if ("".equals(_fileForCurrentActionBar.currentFolder)) {
      _fileForCurrentActionBar = new ExoFile();
      parent = DocumentHelper.getInstance().currentFileMap.getParcelable("");
      documentList = DocumentHelper.getInstance().childFilesMap.getParcelableArrayList(ExoConstants.DOCUMENT_JCR_PATH);

    } else {
      parent = DocumentHelper.getInstance().currentFileMap.getParcelable(_fileForCurrentActionBar.path);
      DocumentHelper.getInstance().currentFileMap.remove(_fileForCurrentActionBar.path);
      _fileForCurrentActionBar = parent;
      if ("".equals(parent.name)) {
        documentList = DocumentHelper.getInstance().childFilesMap.getParcelableArrayList("");
      } else {
        documentList = DocumentHelper.getInstance().childFilesMap.getParcelableArrayList(parent.path);
      }
    }
    return documentList;
  }

  public void onLoad(String source, String destination, int action) {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      if (mLoadTask == null || mLoadTask.getStatus() == DocumentLoadTask.Status.FINISHED) {
        if (Config.GD_INFO_LOGS_ENABLED)
          Log.i("DocumentLoadTask", "onLoad");
        mLoadTask = (DocumentLoadTask) new DocumentLoadTask(this, source, destination, action).execute();
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

  public void uploadFile() {
    onLoad(_documentAdapter._documentActionDialog.myFile.path, null, ACTION_ADD_PHOTO);
  }

  private void init() {
    _listViewDocument = (ListView) findViewById(R.id.ListView_Files);
    _listViewDocument.setDivider(null);
    _textViewEmptyPage = (TextView) findViewById(R.id.TextView_EmptyPage);
    _textViewEmptyPage.setVisibility(View.INVISIBLE);

    changeLanguage();

  }

  public void setDocumentAdapter(ArrayList<ExoFile> documentList) {
    if ("".equals(_fileForCurrentActionBar.name)) {
      setTitle(getResources().getString(R.string.Documents));
    } else {
      setTitle(_fileForCurrentActionBar.name);
    }
    if (documentList.size() == 0) {
      setEmptyView(View.VISIBLE);
    } else
      setEmptyView(View.GONE);

    _documentAdapter = new DocumentAdapter(this, documentList);
    _listViewDocument.setAdapter(_documentAdapter);
    addOrRemoveFileActionButton();
  }

  /*
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
