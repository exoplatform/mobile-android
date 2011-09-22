package org.exoplatform.ui;

import org.exoplatform.R;
import org.exoplatform.controller.document.DocumentActionAdapter;
import org.exoplatform.model.DocumentActionDescription;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.LocalizationHelper;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

//File action list
public class DocumentActionDialog extends Dialog implements OnClickListener {

  Thread                  thread;

  Button                  _btnClose;                  // Close dialog

  ListView                _listViewFileAction;        // List of action

  TextView                _txtvFileName;              // File's name

  Context                 mContext;                   // context

  ExoFile                 myFile;                     // Current file
  
  // Localization string
  String                  strClose;

  String                  strCannotBackToPreviousPage;

  DocumentActionDescription[] fileActionList   = null;
  
  public DocumentActionAdapter  _documentActionAdapter;

  // Constructor
  public DocumentActionDialog(Context context, ExoFile file) {
    
    super(context);
    
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.exofileaction);
    mContext = context;
    
    myFile = file;
    
    _documentActionAdapter = new DocumentActionAdapter(getContext(), this, myFile);
    
    init();
    
  }

  private void init() {
    
    _listViewFileAction = (ListView) findViewById(R.id.ListView0_FileAction);
    
    _btnClose = (Button) findViewById(R.id.Button_Close);
    _btnClose.setOnClickListener(new View.OnClickListener() {
      
      public void onClick(View v) {
        dismiss();
      }
    });
    
    _txtvFileName = (TextView) findViewById(R.id.TextView_FileName);
    _txtvFileName.setText(myFile.fileName.replace("%20", " "));
    
    changeLanguage();
    
    setDocumentActionAdapter();

  }
  
  public void setMyFile(ExoFile file) {
    
    myFile = file;
  }
  
  public void setDocumentActionAdapter() {
    _listViewFileAction.setAdapter(_documentActionAdapter);
  }
  
  // Set language
  public void changeLanguage() {

    LocalizationHelper local = LocalizationHelper.getInstance();
    
    String strClose = local.getString("CloseButton");
    strCannotBackToPreviousPage = local.getString("CannotBackToPreviousPage");

    _btnClose.setText(strClose);

  }

  public void onClick(View v) {
    
  }

}
