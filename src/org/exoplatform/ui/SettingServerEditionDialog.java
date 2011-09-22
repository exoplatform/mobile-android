package org.exoplatform.ui;

import org.exoplatform.R;
import org.exoplatform.controller.setting.SettingServerEditionController;
import org.exoplatform.proxy.ServerObj;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.URLAnalyzer;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SettingServerEditionDialog extends Dialog implements android.view.View.OnClickListener {

  private Button    btnOK;

  private Button    btnDeleteCancel;

  private TextView  txtvTittle;

  private TextView  txtvServerName;

  private TextView  txtvServerUrl;

  private EditText  editTextServerName;

  private EditText  editTextServerUrl;

  private ServerObj serverObj;

  private Context   mContext;

  private boolean   isNewServer;

  private ListView  listViewServer;

  public SettingServerEditionDialog(Context context, ListView listView) {
    super(context);
    /** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    /** Design the dialog in main.xml file */
    setContentView(R.layout.exolanguagesetting);
    mContext = context;
    listViewServer = listView;
    init();
  }

  private void init() {
    btnOK = (Button) findViewById(R.id.Button_OK);
    btnOK.setOnClickListener(this);

    btnDeleteCancel = (Button) findViewById(R.id.Button_Delete_Cancel);
    btnDeleteCancel.setOnClickListener(this);

    txtvTittle = (TextView) findViewById(R.id.TextView_Title);

    txtvServerName = (TextView) findViewById(R.id.TextView_Server_Name);
    txtvServerUrl = (TextView) findViewById(R.id.TextView_Server_URL);

    editTextServerName = (EditText) findViewById(R.id.EditText_Server_Name);
    editTextServerUrl = (EditText) findViewById(R.id.EditText_Server_URL);
    changeLanguage();
    isNewServer = ServerSettingHelper.getInstance().getIsNewServer();
    if (isNewServer) {
      serverObj = new ServerObj();
      serverObj._bSystemServer = false;
      serverObj._strServerName = "";
      serverObj._strServerUrl = "";
    } else {
      serverObj = ServerSettingHelper.getInstance()
                                     .getServerInfoList()
                                     .get(ServerSettingHelper.getInstance()
                                                             .getSelectedServerIndex());
    }

    editTextServerName.setText(serverObj._strServerName);
    editTextServerUrl.setText(serverObj._strServerUrl);

  }

//  @Override
  public void onClick(View view) {
    ServerObj myServerObj = new ServerObj();

    myServerObj._strServerName = editTextServerName.getText().toString();
    // myServerObj._strServerUrl = editTextServerUrl.getText().toString();

    URLAnalyzer urlAnanyzer = new URLAnalyzer();
    myServerObj._strServerUrl = urlAnanyzer.parserURL(editTextServerUrl.getText().toString());
    SettingServerEditionController editController = new SettingServerEditionController(mContext);
    if (view == btnOK) {
      editController.onAccept(myServerObj, myServerObj);
    }

    if (view == btnDeleteCancel) {
      editController.onDelete(myServerObj, myServerObj);
    }
    
    dismiss();
    editController.onResetAdapter(listViewServer);
  }

  public void changeLanguage() {
    LocalizationHelper local = LocalizationHelper.getInstance();
    String strTittle = "";
    String strServerName = "";
    String strServerUrl = "";

    String strOKButton = "";
    String strDeleteCancelButton = "";

    if (isNewServer) // New server
    {
      strTittle = local.getString("NewServer");
      strDeleteCancelButton = local.getString("Cancel");
    } else // Server detail
    {
      strTittle = local.getString("ServerDetail");
      strDeleteCancelButton = local.getString("Delete");

    }

    strServerName = local.getString("NameOfTheServer");
    strServerUrl = local.getString("URLOfTheSerVer");

    strOKButton = local.getString("OK");

    txtvTittle.setText(strTittle);

    txtvServerName.setText(strServerName);
    txtvServerUrl.setText(strServerUrl);

    btnOK.setText(strOKButton);
    btnDeleteCancel.setText(strDeleteCancelButton);

  }

}
