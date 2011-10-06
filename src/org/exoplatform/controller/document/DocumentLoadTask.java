package org.exoplatform.controller.document;

import java.io.File;
import java.util.List;

import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.util.Log;
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

  private List<ExoFile>         _documentList;

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

    try {
      if (actionID == 0) {
        _documentList = ExoDocumentUtils.getPersonalDriveContent(strSourceUrl);
      } else if (actionID == 1) {

        boolean deleteFile = documentActivity.deleteFile(strSourceUrl);
        if (deleteFile) {
          strSourceUrl = ExoDocumentUtils.getParentUrl(strSourceUrl);
          _documentList = ExoDocumentUtils.getPersonalDriveContent(strSourceUrl);
        }
      } else if (actionID == 2) {
        DocumentAdapter adapter = documentActivity._documentAdapter;
        _documentList = adapter._documentList;

        documentActivity.copyFile(strSourceUrl, strDestinationUrl);
        strSourceUrl = ExoDocumentUtils.getParentUrl(strDestinationUrl);
      } else if (actionID == 3) {
        DocumentAdapter adapter = documentActivity._documentAdapter;
        _documentList = adapter._documentList;

        documentActivity.moveFile(strSourceUrl, strDestinationUrl);
        strSourceUrl = ExoDocumentUtils.getParentUrl(strDestinationUrl);
      } else {
        File file = new File(documentActivity._sdcard_temp_dir);
        ExoDocumentUtils.putFileToServerFromLocal(strSourceUrl + "/" + file.getName(),
                                                  file,
                                                  "image/png");
        documentActivity._documentAdapter._urlStr = strSourceUrl;
        _documentList = ExoDocumentUtils.getPersonalDriveContent(strSourceUrl);
      }

      return true;
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
    Log.i("DocumentLoadTask", "" + _documentList.size());
    if (result) {
      if (_documentList.size() == 0) {
        documentActivity.setEmptyView(View.VISIBLE);
      } else
        documentActivity.setEmptyView(View.GONE);
      if (documentActivity._documentAdapter == null) {

        documentActivity._documentAdapter = new DocumentAdapter(documentActivity,
                                                                documentActivity._urlDocumentHome);
        documentActivity.setDocumentAdapter();
      }
      documentActivity._documentAdapter._documentList = _documentList;
      documentActivity._documentAdapter.notifyDataSetChanged();

    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
      documentActivity.setEmptyView(View.VISIBLE);
    }
    _progressDialog.dismiss();

    documentActivity.setTitle(ExoDocumentUtils.getLastPathComponent(strSourceUrl));
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
