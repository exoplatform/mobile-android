package org.exoplatform.ui;

import org.exoplatform.R;
import org.exoplatform.controller.document.DocumentActionAdapter;
import org.exoplatform.model.ExoFile;

import android.app.Dialog;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

//File action list
public class DocumentActionDialog extends Dialog {

  private ListView             _listViewFileAction;   // List of
                                                       // action

  private TextView             _txtvFileName;         // File's
                                                       // name

  public ExoFile               myFile;                // Current
                                                       // file

  public DocumentActionAdapter _documentActionAdapter;
  
  // Constructor
  public DocumentActionDialog(DocumentActivity context, ExoFile file, boolean isActBar) {

    super(context);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.exofileaction);

    setCanceledOnTouchOutside(true);

    myFile = file;

    _documentActionAdapter = new DocumentActionAdapter(context, this, myFile, isActBar);

    init();

    setTileForDialog(myFile.name);

  }

  public void setTileForDialog(String title) {
    _txtvFileName.setText(title);
  }

  private void init() {

    _listViewFileAction = (ListView) findViewById(R.id.ListView0_FileAction);

    _txtvFileName = (TextView) findViewById(R.id.TextView_Title);

    setDocumentActionAdapter();

  }

  public void setDocumentActionAdapter() {
    _listViewFileAction.setAdapter(_documentActionAdapter);
  }

}
