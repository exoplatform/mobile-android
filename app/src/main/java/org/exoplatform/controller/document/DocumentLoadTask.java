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
package org.exoplatform.controller.document;

import java.io.File;
import java.io.IOException;

import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.Log;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.ConnTimeOutDialog;
import org.exoplatform.widget.DocumentWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.animation.AnimationUtils;

public class DocumentLoadTask extends AsyncTask<Integer, Void, Integer> {

  /*
   * Result status
   */
  private static final int      RESULT_OK      = 1;

  private static final int      RESULT_ERROR   = 2;

  private static final int      RESULT_TIMEOUT = 3;

  private static final int      RESULT_FALSE   = 4;
  
  private static final int      RESULT_CANCELED   = 5;

  // LOG TAG
  private static final String   LOG_TAG        = "eXo____DocumentLoadTask____";

  private DocumentWaitingDialog _progressDialog;

  private String                loadingData;

  private String                okString;

  private String                titleString;

  /*
   * This @contentWarningString is for display the error/warning message when
   * retrieving document
   */
  private String                contentWarningString;

  private int                   actionID;

  private String                strSourceUrl;

  private String                strDestinationUrl;

  private DocumentActivity      documentActivity;

  private ExoFile               sourceFile;

  private ExoFile               loadedFolder;

  private Resources             resource;

  public DocumentLoadTask(DocumentActivity activity, ExoFile source, String destination, int action) {
    resource = activity.getResources();
    documentActivity = activity;
    sourceFile = source;
    strSourceUrl = source.path;
    strDestinationUrl = destination;
    actionID = action;
    changeLanguage();
  }

  private void changeLanguage() {
    loadingData = resource.getString(R.string.LoadingData);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    contentWarningString = resource.getString(R.string.LoadingDataError);
  }

  @Override
  public void onPreExecute() {
    _progressDialog = new DocumentWaitingDialog(documentActivity, null, loadingData);
    _progressDialog.show();
  }

  @Override
  public Integer doInBackground(Integer... params) {
    boolean result = true;
    try {
      // Check the session status each time we retrieve files/folders.
      // If timeout, try to re-login. If re-logging fails, pop-up an error
      String versionUrl = SocialActivityUtil.getDomain() + ExoConstants.DOMAIN_PLATFORM_VERSION;
      if (ExoConnectionUtils.checkTimeout(versionUrl) != ExoConnectionUtils.LOGIN_SUCCESS)
        return RESULT_TIMEOUT;

      switch (actionID) {
      case DocumentActivity.ACTION_DELETE:
        contentWarningString = resource.getString(R.string.DocumentCannotDelete);
        result = ExoDocumentUtils.deleteFile(strSourceUrl);
        sourceFile = documentActivity._fileForCurrentActionBar;
        break;
      case DocumentActivity.ACTION_COPY:
        contentWarningString = resource.getString(R.string.DocumentCopyPasteError);
        result = ExoDocumentUtils.copyFile(strSourceUrl, strDestinationUrl);
        sourceFile = documentActivity._fileForCurrentActionBar;
        break;
      case DocumentActivity.ACTION_MOVE:
        contentWarningString = resource.getString(R.string.DocumentCopyPasteError);
        result = ExoDocumentUtils.moveFile(strSourceUrl, strDestinationUrl);
        sourceFile = documentActivity._fileForCurrentActionBar;
        break;
      case DocumentActivity.ACTION_ADD_PHOTO:
        File file = new File(documentActivity._sdcard_temp_dir);
        contentWarningString = resource.getString(R.string.DocumentUploadError);
        File tempFile = PhotoUtils.reziseFileImage(file);
        if (tempFile != null) {
          result = ExoDocumentUtils.putFileToServerFromLocal(strSourceUrl + "/" + file.getName(),
                                                             tempFile,
                                                             ExoConstants.IMAGE_TYPE);
        }
        break;
      case DocumentActivity.ACTION_RENAME:
        contentWarningString = resource.getString(R.string.DocumentRenameError);
        result = ExoDocumentUtils.renameFolder(strSourceUrl, strDestinationUrl);
        if (result) {
          // prepare to reload the current folder if the action was successful
          if (sourceFile == documentActivity._fileForCurrentActionBar) {
            // folder renamed from within
            // update the key in folderToParentMap to link to the parent folder
            ExoFile parent = (ExoFile) DocumentHelper.getInstance().folderToParentMap.getParcelable(strSourceUrl);
            DocumentHelper.getInstance().folderToParentMap.remove(strSourceUrl);
            DocumentHelper.getInstance().folderToParentMap.putParcelable(strDestinationUrl, parent);
          } else {
            // file/folder renamed from parent
            // we will reload the current folder
            sourceFile = documentActivity._fileForCurrentActionBar;
          }
        }
        break;
      case DocumentActivity.ACTION_CREATE:
        contentWarningString = resource.getString(R.string.DocumentCreateFolderError);
        result = ExoDocumentUtils.createFolder(strDestinationUrl);
        if (result)
          // prepare to reload the current folder if the action was successful
          sourceFile = documentActivity._fileForCurrentActionBar;
        break;

      }

//    Stop execution if the task was canceled
      if (isCancelled())
        return RESULT_CANCELED;
      
//    Get folder content
      if (result == true) {
        loadedFolder = ExoDocumentUtils.getPersonalDriveContent(documentActivity, sourceFile);
        return RESULT_OK;
      } else {
        return RESULT_FALSE;
      }
    } catch (IOException e) {
      if (Log.LOGE)
        Log.e(LOG_TAG, e.getMessage(), e);
      return RESULT_ERROR;
    }
  }

  @Override
  public void onCancelled() {
    super.onCancelled();
    if (_progressDialog.isAttachedToWindow())
      _progressDialog.dismiss();
  }

  @Override
  public void onPostExecute(Integer result) {
    if (result == RESULT_OK) {
      // Map the new current folder path (sourceFile.path)
      // with its parent (former current folder)
      if (!DocumentHelper.getInstance().folderToParentMap.containsKey(sourceFile.path))
        DocumentHelper.getInstance().folderToParentMap.putParcelable(sourceFile.path, documentActivity._fileForCurrentActionBar);

      documentActivity.updateContent(loadedFolder);

      /*
       * Set animation for listview when access to folder
       */
      documentActivity._listViewDocument.setAnimation(AnimationUtils.loadAnimation(documentActivity, R.anim.anim_right_to_left));

    } else if (result == RESULT_ERROR) {
      new WarningDialog(documentActivity, titleString, contentWarningString, okString).show();
    } else if (result == RESULT_TIMEOUT) {
      new ConnTimeOutDialog(documentActivity, titleString, okString).show();
    } else if (result == RESULT_FALSE) {
      new WarningDialog(documentActivity, titleString, contentWarningString, okString).show();
    }
    if (_progressDialog.isAttachedToWindow())
      _progressDialog.dismiss();
  }

}
