package org.exoplatform.controller.document;

import java.util.List;

import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.LocalizationHelper;

import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.SocialWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;

public class DocumentTask extends UserTask<Integer, Void, Boolean> {
  private SocialWaitingDialog _progressDialog;

  private Context             mContext;

  private String              loadingData;

  private String              okString;

  private String              titleString;

  private String              contentString;

//  actionID: 0-retrieve data, 1: delete file, 2: copy file, 3: move file
  private int                 actionID;
  
  private String              strSourceUrl;
  private String              strDestinationUrl;
  
  private DocumentActivity     documentActivity;
  
  private List<ExoFile>        _documentList;
  
  public DocumentTask(Context context, DocumentActivity activity, String source, String destination, int action) {
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
    _progressDialog = new SocialWaitingDialog(mContext, null, loadingData);
    _progressDialog.show();

  }

  @Override
  public Boolean doInBackground(Integer... params) {

    try {
      if(actionID == 0) {
        _documentList = ExoDocumentUtils.getPersonalDriveContent(strSourceUrl);
        
      }
      else if(actionID == 1) {
        
        boolean deleteFile = documentActivity.deleteFile(strSourceUrl);
        if(deleteFile)
        {
          String url = ExoDocumentUtils.getParentUrl(strSourceUrl);
          _documentList = ExoDocumentUtils.getPersonalDriveContent(url);
        }
        
      }
      else if(actionID == 2) {
        DocumentAdapter adapter = documentActivity._documentAdapter;
        _documentList = adapter._documentList;
        
        documentActivity.copyFile(strSourceUrl, strDestinationUrl);
      }
      else {
        DocumentAdapter adapter = documentActivity._documentAdapter;
        _documentList = adapter._documentList;
        
        documentActivity.copyFile(strSourceUrl, strDestinationUrl);
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

    if (result) {
      
      if(documentActivity._documentAdapter == null) {
        
        documentActivity._documentAdapter = new DocumentAdapter(documentActivity, documentActivity._urlDocumentHome);
        documentActivity.setDocumentAdapter();
      }
      documentActivity._documentAdapter._documentList = _documentList;
      documentActivity._documentAdapter.notifyDataSetChanged();
    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
    _progressDialog.dismiss();

  }

}
