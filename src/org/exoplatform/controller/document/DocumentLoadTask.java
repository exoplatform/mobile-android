package org.exoplatform.controller.document;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import org.exoplatform.model.ExoFile;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.utils.SocialActivityUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Loading content of document screen
 */
public class DocumentLoadTask extends AsyncTask<Integer, Void, Integer> {

  /**=== Result status ===*/
  public static final int      RESULT_OK      = 1;

  public static final int      RESULT_ERROR   = 2;

  public static final int      RESULT_TIMEOUT = 3;

  public static final int      RESULT_FALSE   = 4;


  private int                   mActionID;

  private String                mSourceUrl;

  private String                mDestUrl;

  private ExoFile               mActionBarFile;

  private ExoFile               mMenuFile;

  private String                mSDCardDir;

  private ArrayList<ExoFile>    mDocumentList;

  private AsyncTaskListener     mListener;

  private Context               mContext;

  public DocumentLoadTask(Context context, Bundle requestData) {
    mContext = context;

    mSourceUrl      = requestData.getString(DocumentActivity.DOC_SOURCE);
    mDestUrl        = requestData.getString(DocumentActivity.DOC_DESTINATION);
    mActionID       = requestData.getInt(DocumentActivity.ACTION_ID);
    mActionBarFile  = requestData.getParcelable(DocumentActivity.CURRENT_ACTION_BAR_FILE);

    if (mActionID == DocumentActivity.ACTION_RENAME)
      mMenuFile = requestData.getParcelable(DocumentActivity.CURRENT_MENU_FILE);
    else if (mActionID == DocumentActivity.ACTION_ADD_PHOTO)
      mSDCardDir = requestData.getString(DocumentActivity.SDCARD_DIR);
  }


  @Override
  public Integer doInBackground(Integer... params) {
    boolean result = true;
    mDocumentList = new ArrayList<ExoFile>();

    try {

      /**
       * Checking the session status each time we retrieve files/folders. If
       * time out, re logging in. If relogging in error, pop up a error dialog
       */
      String versionUrl = SocialActivityUtil.getDomain() + ExoConstants.DOMAIN_PLATFORM_VERSION;
      if (ExoConnectionUtils.checkTimeout(versionUrl) != ExoConnectionUtils.LOGIN_SUSCESS)
        return RESULT_TIMEOUT;

      switch (mActionID) {

        case DocumentActivity.ACTION_DELETE:
          //contentWarningString = resource.getString(R.string.DocumentCannotDelete);
          result = ExoDocumentUtils.deleteFile(mSourceUrl);
          mSourceUrl = ExoDocumentUtils.getParentUrl(mSourceUrl);
          break;

        case DocumentActivity.ACTION_COPY:
          //contentWarningString = resource.getString(R.string.DocumentCopyPasteError);
          result = ExoDocumentUtils.copyFile(mSourceUrl, mDestUrl);
          mSourceUrl = ExoDocumentUtils.getParentUrl(mDestUrl);
          break;

        case DocumentActivity.ACTION_MOVE:
          //contentWarningString = resource.getString(R.string.DocumentCopyPasteError);
          result = ExoDocumentUtils.moveFile(mSourceUrl, mDestUrl);
          mSourceUrl = ExoDocumentUtils.getParentUrl(mDestUrl);
          break;

        case DocumentActivity.ACTION_ADD_PHOTO:
          File file = new File(mSDCardDir);
          //contentWarningString = resource.getString(R.string.DocumentUploadError);

          File tempFile = PhotoUtils.reziseFileImage(file);
          if (tempFile != null) {
            result = ExoDocumentUtils.putFileToServerFromLocal(mSourceUrl + "/" + file.getName(),
                tempFile, ExoConstants.IMAGE_TYPE);
          }
          break;

        case DocumentActivity.ACTION_RENAME:
          //contentWarningString = resource.getString(R.string.DocumentRenameError);
          result = ExoDocumentUtils.renameFolder(mSourceUrl, mDestUrl);

          if (result) {
            mActionBarFile.isFolder = mMenuFile.isFolder;
            mActionBarFile.nodeType = mMenuFile.nodeType;
            mSourceUrl = mDestUrl;
            if (!mMenuFile.isFolder) mSourceUrl = ExoDocumentUtils.getParentUrl(mSourceUrl);

          } else {
            mActionBarFile.currentFolder = mSourceUrl;
            mActionBarFile.name = mSourceUrl.substring(mSourceUrl.lastIndexOf("/") + 1, mSourceUrl.length());
          }
          break;

        case DocumentActivity.ACTION_CREATE:
          //contentWarningString = resource.getString(R.string.DocumentCreateFolderError);
          result = ExoDocumentUtils.createFolder(mDestUrl);
          break;

      }

      /** Get folder content - default action */
      if (result) {
        mDocumentList = ExoDocumentUtils.getPersonalDriveContent(mContext, mActionBarFile);
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
  }

  @Override
  public void onPostExecute(Integer result) {
    if (mListener != null) mListener.onLoadingDocumentsFinished(result, mActionID, mDocumentList);
  }


  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }

  public interface AsyncTaskListener {

    void onLoadingDocumentsFinished(int result, int actionId, ArrayList<ExoFile> documentList);
  }
}
