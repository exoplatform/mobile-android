package org.exoplatform.controller.document;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
//import android.util.Log;
import android.view.View;

public class DocumentLoadTask extends UserTask<Integer, Void, Boolean> {
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

  private List<ExoFile>         _documentList = new ArrayList<ExoFile>();

  public DocumentLoadTask(Context context,
                          DocumentActivity activity,
                          String source,
                          String destination,
                          int action) {
    mContext = context;
    documentActivity = activity;
    strSourceUrl = source;
    strDestinationUrl = destination;
    actionID = action;

    changeLanguage();
  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    loadingData = location.getString("LoadingData");
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    contentString = location.getString("ConnectionError");
  }

  @Override
  public void onPreExecute() {
    _progressDialog = new DocumentWaitingDialog(mContext, null, loadingData);
    _progressDialog.show();

  }

  @Override
  public Boolean doInBackground(Integer... params) {

    boolean result = true;

    try {
      if (actionID == 1) {
        result = documentActivity.deleteFile(strSourceUrl);
        contentString = LocalizationHelper.getInstance().getString("DocumentCannotDelete");
        strSourceUrl = ExoDocumentUtils.getParentUrl(strSourceUrl);

      } else if (actionID == 2) {
        result = documentActivity.copyFile(strSourceUrl, strDestinationUrl);
        contentString = LocalizationHelper.getInstance().getString("DocumentCopyPasteError");
        strSourceUrl = ExoDocumentUtils.getParentUrl(strDestinationUrl);

      } else if (actionID == 3) {
        result = documentActivity.moveFile(strSourceUrl, strDestinationUrl);
        contentString = LocalizationHelper.getInstance().getString("DocumentCopyPasteError");
        strSourceUrl = ExoDocumentUtils.getParentUrl(strDestinationUrl);

      } else if (actionID == 4) {
        File file = new File(documentActivity._sdcard_temp_dir);
        contentString = LocalizationHelper.getInstance().getString("DocumentUploadError");
        result = ExoDocumentUtils.putFileToServerFromLocal(strSourceUrl + "/" + file.getName(),
                                                           file,
                                                           "image/png");

      } else if (actionID == 5) {
        result = documentActivity.moveFile(strSourceUrl, strDestinationUrl);
        contentString = LocalizationHelper.getInstance().getString("DocumentRenameError");
        boolean isFolder = documentActivity._documentAdapter._documentActionDialog.myFile.isFolder;
        strSourceUrl = strDestinationUrl;
        if (!isFolder)
          strSourceUrl = ExoDocumentUtils.getParentUrl(strSourceUrl);

      } else if (actionID == 6) {
        contentString = LocalizationHelper.getInstance().getString("DocumentCreateFolderError");
        result = documentActivity.createFolder(strDestinationUrl);

      }
      if (result == true) {
        _documentList = ExoDocumentUtils.getPersonalDriveContent(strSourceUrl);
      }

      return result;
    } catch (RuntimeException e) {
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

      documentActivity._fileForCurrnentActionBar = new ExoFile(strSourceUrl,
                                                               ExoDocumentUtils.getLastPathComponent(strSourceUrl),
                                                               true,
                                                               "text/html");

      if (actionID == 0 || actionID == 1 || actionID == 4 || actionID == 5 || actionID == 6) {

        if (actionID == 5) {
          boolean isFolder = documentActivity._documentAdapter._documentActionDialog.myFile.isFolder;
          String type = documentActivity._documentAdapter._documentActionDialog.myFile.contentType;
          documentActivity._fileForCurrnentActionBar.isFolder = isFolder;
          documentActivity._fileForCurrnentActionBar.contentType = type;
        }
      }

      if (documentActivity._documentAdapter == null) {

        documentActivity._documentAdapter = new DocumentAdapter(documentActivity,
                                                                documentActivity._urlDocumentHome);
        documentActivity.setDocumentAdapter();
      }
      documentActivity._documentAdapter._documentList = _documentList;
      documentActivity._documentAdapter.notifyDataSetChanged();
      documentActivity.addOrRemoveFileActionButton();

      try {
        String title = new String(ExoDocumentUtils.getLastPathComponent(strSourceUrl)
                                                  .getBytes("ISO-8859-1"), "UTF-8");
        documentActivity.setTitle(title);
      } catch (UnsupportedEncodingException e) {
      }

    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
    if (_documentList.size() == 0) {
      documentActivity.setEmptyView(View.VISIBLE);
    } else
      documentActivity.setEmptyView(View.GONE);
    _progressDialog.dismiss();

  }

  private class DocumentWaitingDialog extends WaitingDialog {

    public DocumentWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      DocumentActivity._documentActivityInstance.onCancelLoad();
    }

  }
}
