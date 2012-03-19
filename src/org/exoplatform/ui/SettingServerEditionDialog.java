package org.exoplatform.ui;

import org.exoplatform.R;
import org.exoplatform.controller.setting.SettingServerEditionController;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.URLAnalyzer;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingServerEditionDialog extends Dialog implements android.view.View.OnClickListener {

  private Button        btnOK;

  private Button        btnDeleteCancel;

  private TextView      txtvTittle;

  private TextView      txtvServerName;

  private TextView      txtvServerUrl;

  private EditText      editTextServerName;

  private EditText      editTextServerUrl;

  private String        serverNameURLInvalid;

  private ServerObjInfo serverObj;

  private Context       mContext;

  private boolean       isNewServer;

  private ListView      listViewServer;

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

    isNewServer = ServerSettingHelper.getInstance().getIsNewServer();
    if (isNewServer) {
      serverObj = new ServerObjInfo();
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

    changeLanguage();

  }

  // @Override
  public void onClick(View view) {
    ServerObjInfo myServerObj = new ServerObjInfo();

    myServerObj._strServerName = editTextServerName.getText().toString();

    URLAnalyzer urlAnanyzer = new URLAnalyzer();
    myServerObj._strServerUrl = urlAnanyzer.parserURL(editTextServerUrl.getText().toString());
    if (myServerObj._strServerUrl != null) {
      myServerObj._strServerName = myServerObj._strServerName.trim();
      myServerObj._strServerUrl = myServerObj._strServerUrl.trim();

      SettingServerEditionController editController = new SettingServerEditionController(mContext);
      if (view.equals(btnOK)) {
        editController.onAccept(myServerObj, myServerObj);
      }

      if (view.equals(btnDeleteCancel)) {
        editController.onDelete(myServerObj, myServerObj);
      }

      dismiss();
      editController.onResetAdapter(listViewServer);
    } else {
      if (view.equals(btnDeleteCancel)) {
        dismiss();
      }
      Toast.makeText(mContext, serverNameURLInvalid, Toast.LENGTH_SHORT).show();
    }

  }

  public void changeLanguage() {

    Resources resource = mContext.getResources();
    String strTittle = "";
    String strServerName = "";
    String strServerUrl = "";

    String strOKButton = "";
    String strDeleteCancelButton = "";

    if (isNewServer) // New server
    {
      strTittle = resource.getString(R.string.NewServer);
      strDeleteCancelButton = resource.getString(R.string.Cancel);
    } else // Server detail
    {
      strTittle = resource.getString(R.string.ServerDetail);
      strDeleteCancelButton = resource.getString(R.string.Delete);

    }

    strServerName = resource.getString(R.string.NameOfTheServer);
    strServerUrl = resource.getString(R.string.URLOfTheSerVer);
    serverNameURLInvalid = resource.getString(R.string.SpecialCharacters);
    strOKButton = resource.getString(R.string.OK);

    txtvTittle.setText(strTittle);

    txtvServerName.setText(strServerName);
    txtvServerUrl.setText(strServerUrl);

    btnOK.setText(strOKButton);
    btnDeleteCancel.setText(strDeleteCancelButton);

  }

}
