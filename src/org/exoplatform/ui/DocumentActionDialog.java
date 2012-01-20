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
import android.widget.ListView;
import android.widget.TextView;

//File action list
public class DocumentActionDialog extends Dialog implements OnClickListener {

  Thread                       thread;

  ListView                     _listViewFileAction;        // List of action

  TextView                     _txtvFileName;              // File's name

  Context                      mContext;                   // context

  public ExoFile                      myFile;                     // Current file

  // Localization string
  String                       strClose;

  String                       strCannotBackToPreviousPage;

  DocumentActionDescription[]  fileActionList = null;

  public DocumentActionAdapter _documentActionAdapter;

  // Constructor
  public DocumentActionDialog(Context context, ExoFile file) {

    super(context);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.exofileaction);

    setCanceledOnTouchOutside(true);

    mContext = context;

    myFile = file;

    _documentActionAdapter = new DocumentActionAdapter(getContext(), this, myFile);

    init();

    setTileForDialog(myFile.name);
    
  }

  public void setTileForDialog(String title) {
    _txtvFileName.setText(title);
  }
  
  private void init() {

    _listViewFileAction = (ListView) findViewById(R.id.ListView0_FileAction);

    _txtvFileName = (TextView) findViewById(R.id.TextView_Title);
    
    changeLanguage();

    setDocumentActionAdapter();

  }

  public void setDocumentActionAdapter() {
    _listViewFileAction.setAdapter(_documentActionAdapter);
  }

  // Set language
  public void changeLanguage() {

    LocalizationHelper local = LocalizationHelper.getInstance();

    strCannotBackToPreviousPage = local.getString("CannotBackToPreviousPage");

  }

  public void onClick(View v) {

  }

}
