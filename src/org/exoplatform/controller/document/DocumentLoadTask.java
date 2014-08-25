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
import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
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

  private ArrayList<ExoFile>    _documentList;

  private Resources             resource;

  public DocumentLoadTask(DocumentActivity activity, String source, String destination, int action) {
    resource = activity.getResources();
    documentActivity = activity;
    strSourceUrl = source;
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
    _documentList = new ArrayList<ExoFile>();
    try {
      /*
       * Checking the session status each time we retrieve files/folders. If
       * time out, re logging in. If relogging in error, pop up a error dialog
       */
      String versionUrl = SocialActivityUtil.getDomain() + ExoConstants.DOMAIN_PLATFORM_VERSION;
      if (ExoConnectionUtils.checkTimeout(versionUrl) != ExoConnectionUtils.LOGIN_SUCCESS)
        return RESULT_TIMEOUT;

      switch (actionID) {
      case DocumentActivity.ACTION_DELETE:
        contentWarningString = resource.getString(R.string.DocumentCannotDelete);
        result = ExoDocumentUtils.deleteFile(strSourceUrl);
        strSourceUrl = ExoDocumentUtils.getParentUrl(strSourceUrl);

        break;
      case DocumentActivity.ACTION_COPY:
        contentWarningString = resource.getString(R.string.DocumentCopyPasteError);
        result = ExoDocumentUtils.copyFile(strSourceUrl, strDestinationUrl);
        strSourceUrl = ExoDocumentUtils.getParentUrl(strDestinationUrl);
        break;
      case DocumentActivity.ACTION_MOVE:
        contentWarningString = resource.getString(R.string.DocumentCopyPasteError);
        result = ExoDocumentUtils.moveFile(strSourceUrl, strDestinationUrl);
        strSourceUrl = ExoDocumentUtils.getParentUrl(strDestinationUrl);

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
          boolean isFolder = documentActivity._documentAdapter._documentActionDialog.myFile.isFolder;
          String type = documentActivity._documentAdapter._documentActionDialog.myFile.nodeType;
          documentActivity._fileForCurrentActionBar.isFolder = isFolder;
          documentActivity._fileForCurrentActionBar.nodeType = type;
          strSourceUrl = strDestinationUrl;
          if (!isFolder)
            strSourceUrl = ExoDocumentUtils.getParentUrl(strSourceUrl);
        } else {
          DocumentActivity._documentActivityInstance._fileForCurrentActionBar.currentFolder = strSourceUrl;
          int lastIndex = strSourceUrl.lastIndexOf("/");
          String folderName = strSourceUrl.substring(lastIndex + 1, strSourceUrl.length());
          DocumentActivity._documentActivityInstance._fileForCurrentActionBar.name = folderName;
        }
        break;
      case DocumentActivity.ACTION_CREATE:
        contentWarningString = resource.getString(R.string.DocumentCreateFolderError);
        result = ExoDocumentUtils.createFolder(strDestinationUrl);
        break;

      }
      /*
       * Get folder content
       */

      if (result == true) {
        _documentList = ExoDocumentUtils.getPersonalDriveContent(documentActivity,
                                                                 documentActivity._fileForCurrentActionBar);
        return RESULT_OK;
      } else
        return RESULT_FALSE;

    } catch (IOException e) {
      return RESULT_ERROR;
    }
  }

  @Override
  public void onCancelled() {
    super.onCancelled();
    _progressDialog.dismiss();
  }

  @Override
  public void onPostExecute(Integer result) {
    if (result == RESULT_OK) {

      documentActivity.setDocumentAdapter(_documentList);
      /*
       * Set animation for listview when access to folder
       */
      documentActivity._listViewDocument.setAnimation(AnimationUtils.loadAnimation(documentActivity,
                                                                                   R.anim.anim_right_to_left));

    } else if (result == RESULT_ERROR) {
      new WarningDialog(documentActivity, titleString, contentWarningString, okString).show();
    } else if (result == RESULT_TIMEOUT) {
      new ConnTimeOutDialog(documentActivity, titleString, okString).show();
    } else if (result == RESULT_FALSE) {
      new WarningDialog(documentActivity, titleString, contentWarningString, okString).show();
    }

    _progressDialog.dismiss();

  }

}
