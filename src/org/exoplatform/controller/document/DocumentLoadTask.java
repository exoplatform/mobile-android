package org.exoplatform.controller.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.exoplatform.model.ExoFile;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.widget.DocumentWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.View;

import com.cyrilmottier.android.greendroid.R;

public class DocumentLoadTask extends AsyncTask<Integer, Void, Boolean> {
  private DocumentWaitingDialog _progressDialog;

  private Context               mContext;

  private String                loadingData;

  private String                okString;

  private String                titleString;

  private String                contentString;

  // actionID: 0-retrieve data, 1: delete file, 2: copy file, 3: move file, 4:
  // upload file
  private int                   actionID;

  private String                strSourceUrl;

  private String                strDestinationUrl;

  private DocumentActivity      documentActivity;

  private ArrayList<ExoFile>    _documentList;

  private Resources             resource;

  public DocumentLoadTask(Context context,
                          DocumentActivity activity,
                          String source,
                          String destination,
                          int action,
                          DocumentWaitingDialog progressDialog) {
    mContext = context;
    resource = mContext.getResources();
    documentActivity = activity;
    strSourceUrl = source;
    strDestinationUrl = destination;
    actionID = action;
    _progressDialog = progressDialog;

    changeLanguage();
  }

  private void changeLanguage() {
    loadingData = resource.getString(R.string.LoadingData);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    contentString = resource.getString(R.string.ConnectionError);
  }

  @Override
  public void onPreExecute() {
    _progressDialog = new DocumentWaitingDialog(mContext, null, loadingData);
    _progressDialog.show();

  }

  @Override
  public Boolean doInBackground(Integer... params) {
    boolean result = true;
    _documentList = new ArrayList<ExoFile>();
    try {
      /*
       * Checking the session status each time we retrieve files/folders. If
       * time out, re logging in
       */
      if (ExoConnectionUtils.getResponseCode(strSourceUrl) == 0) {
        ExoConnectionUtils.onReLogin();
      }
      if (actionID == 1) {
        result = ExoDocumentUtils.deleteFile(strSourceUrl);
        contentString = resource.getString(R.string.DocumentCannotDelete);
        strSourceUrl = ExoDocumentUtils.getParentUrl(strSourceUrl);

      } else if (actionID == 2) {
        result = ExoDocumentUtils.copyFile(strSourceUrl, strDestinationUrl);
        contentString = resource.getString(R.string.DocumentCopyPasteError);
        strSourceUrl = ExoDocumentUtils.getParentUrl(strDestinationUrl);

      } else if (actionID == 3) {
        result = ExoDocumentUtils.moveFile(strSourceUrl, strDestinationUrl);
        contentString = resource.getString(R.string.DocumentCopyPasteError);
        strSourceUrl = ExoDocumentUtils.getParentUrl(strDestinationUrl);

      } else if (actionID == 4) {
        File file = new File(documentActivity._sdcard_temp_dir);
        contentString = resource.getString(R.string.DocumentUploadError);
        File tempFile = PhotoUtils.reziseFileImage(file);
        if (tempFile != null) {
          result = ExoDocumentUtils.putFileToServerFromLocal(strSourceUrl + "/" + file.getName(),
                                                             tempFile,
                                                             ExoConstants.IMAGE_TYPE);
          tempFile.delete();
        }

      } else if (actionID == 5) {
        result = ExoDocumentUtils.renameFolder(strSourceUrl, strDestinationUrl);
        contentString = resource.getString(R.string.DocumentRenameError);
        boolean isFolder = documentActivity._documentAdapter._documentActionDialog.myFile.isFolder;
        strSourceUrl = strDestinationUrl;
        if (!isFolder)
          strSourceUrl = ExoDocumentUtils.getParentUrl(strSourceUrl);

      } else if (actionID == 6) {
        contentString = resource.getString(R.string.DocumentCreateFolderError);
        result = ExoDocumentUtils.createFolder(strDestinationUrl);

      }
      if (result == true) {
        _documentList = ExoDocumentUtils.getPersonalDriveContent(documentActivity._fileForCurrentActionBar);
      }

      return result;
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public void onCancelled() {
    super.onCancelled();
    _progressDialog.dismiss();
  }

  @Override
  public void onPostExecute(Boolean result) {
    if (result) {

      if (actionID == 0 || actionID == 1 || actionID == 4 || actionID == 5 || actionID == 6) {

        if (actionID == 5) {
          boolean isFolder = documentActivity._documentAdapter._documentActionDialog.myFile.isFolder;
          String type = documentActivity._documentAdapter._documentActionDialog.myFile.nodeType;
          documentActivity._fileForCurrentActionBar.isFolder = isFolder;
          documentActivity._fileForCurrentActionBar.nodeType = type;
        }
      }
      if (DocumentActivity._documentActivityInstance._fileForCurrentActionBar == null)
        DocumentActivity._documentActivityInstance.setListViewPadding(5, 0, 5, 0);
      else
        DocumentActivity._documentActivityInstance.setListViewPadding(-2, 0, -2, 0);

      if (documentActivity._fileForCurrentActionBar == null)
        documentActivity.setTitle(resource.getString(R.string.Documents));
      else
        documentActivity.setTitle(documentActivity._fileForCurrentActionBar.name);
      documentActivity._documentAdapter = new DocumentAdapter(documentActivity, _documentList);
      documentActivity.setDocumentAdapter();
      documentActivity.addOrRemoveFileActionButton();

      if (_documentList.size() == 0) {
        documentActivity.setEmptyView(View.VISIBLE);
      } else
        documentActivity.setEmptyView(View.GONE);

    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }

    _progressDialog.dismiss();

  }

}
