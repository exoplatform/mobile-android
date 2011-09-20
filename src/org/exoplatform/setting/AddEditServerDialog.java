package org.exoplatform.setting;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.exoplatform.R;
import org.exoplatform.controller.AppController;
import org.exoplatform.proxy.ExoServerConfiguration;
import org.exoplatform.proxy.ServerObj;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.URLAnalyzer;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/** Class Must extends with Dialog */
/** Implement onClickListener to dismiss dialog when OK Button is pressed */
// Display language setting & user guide page
public class AddEditServerDialog extends Dialog implements OnClickListener {

  Button                       btnOK;

  Button                       btnDeleteCancel;

  TextView                     txtvTittle;

  TextView                     txtvServerName;

  TextView                     txtvServerUrl;

  EditText                     editTextServerName;

  EditText                     editTextServerUrl;

  public static int            selectedServerIndex;

  public static boolean        isNewServer;

  ServerObj                    serverObj;

  private ArrayList<ServerObj> serverInfoList;

  public ArrayList<ServerObj>  _arrUserServerList;

  public ArrayList<ServerObj>  _arrDefaulServerList;

  public ArrayList<ServerObj>  _arrDeletedServerList;

//  public ArrayList<ServerObj>  _arrServerList;

  // Constructor
  public AddEditServerDialog(Context context) {

    super(context);

    /** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    /** Design the dialog in main.xml file */
    setContentView(R.layout.exolanguagesetting);

    btnOK = (Button) findViewById(R.id.Button_OK);
    btnOK.setOnClickListener(this);

    btnDeleteCancel = (Button) findViewById(R.id.Button_Delete_Cancel);
    btnDeleteCancel.setOnClickListener(this);

    txtvTittle = (TextView) findViewById(R.id.TextView_Title);

    txtvServerName = (TextView) findViewById(R.id.TextView_Server_Name);
    txtvServerUrl = (TextView) findViewById(R.id.TextView_Server_URL);

    editTextServerName = (EditText) findViewById(R.id.EditText_Server_Name);
    editTextServerUrl = (EditText) findViewById(R.id.EditText_Server_URL);
    serverInfoList = ServerSettingHelper.getInstance().getServerInfoList();

    if (isNewServer) {
      serverObj = new ServerObj();
      serverObj._bSystemServer = false;
      serverObj._strServerName = "";
      serverObj._strServerUrl = "";
    } else {
      serverObj = serverInfoList.get(selectedServerIndex);
    }

    editTextServerName.setText(serverObj._strServerName);
    editTextServerUrl.setText(serverObj._strServerUrl);

    changeLanguage();
  }

  // Show user guide or change language
  public void onClick(View v) {
    /** When OK Button is clicked, dismiss the dialog */

    ExoServerConfiguration conf = AppController.configurationInstance;

    ServerObj myServerObj = new ServerObj();

    myServerObj._strServerName = editTextServerName.getText().toString();
    // myServerObj._strServerUrl = editTextServerUrl.getText().toString();

    URLAnalyzer urlAnanyzer = new URLAnalyzer();
    myServerObj._strServerUrl = urlAnanyzer.parserURL(editTextServerUrl.getText().toString());

    if (v == btnOK) {
      if (myServerObj._strServerName.equalsIgnoreCase("")
          || myServerObj._strServerUrl.equalsIgnoreCase("")) {
        // Server name or server url is empty
        Toast.makeText(ExoSetting.eXoSettingInstance,
                       "Server name or url is empty. Please enter it again",
                       Toast.LENGTH_LONG).show();
        return;
      }
      if (isNewServer) {
        boolean isExisted = false;
        for (int i = 0; i < serverInfoList.size(); i++) {
          ServerObj tmp = serverInfoList.get(i);
          if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)) {
            isExisted = true;
            Toast.makeText(ExoSetting.eXoSettingInstance, "The server's existed", Toast.LENGTH_LONG)
                 .show();
            break;
            // New server is the same with the old one
          }
        }
        if (!isExisted) {
          // Create new server
          myServerObj._bSystemServer = false;
          serverInfoList.add(myServerObj);
          ExoServerConfiguration.createXmlDataWithServerList(serverInfoList,
                                                             "UserServerList.xml",
                                                             "");
        }
      } else // Update server
      {
        boolean isExisted = true;
        for (int i = 0; i < serverInfoList.size(); i++) {
          ServerObj tmp = serverInfoList.get(i);

          if (i == selectedServerIndex) {
            continue;
          }

          if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)) {
            isExisted = false;
            Toast.makeText(ExoSetting.eXoSettingInstance, "The server's existed", Toast.LENGTH_LONG)
                 .show();
            break;
            // updated server is the same with the old one
          }
        }
        if (isExisted) {
          // Remove the old server
          serverInfoList.remove(selectedServerIndex);
          serverObj._strServerName = myServerObj._strServerName;
          serverObj._strServerUrl = myServerObj._strServerUrl;
          serverInfoList.add(selectedServerIndex, serverObj);
          if (serverObj._bSystemServer)// Update default server
          {
            serverInfoList.remove(selectedServerIndex);
            serverInfoList.add(selectedServerIndex, serverObj);
            ExoServerConfiguration.createXmlDataWithServerList(serverInfoList,
                                                               "DefaultServerList.xml",
                                                               ExoServerConfiguration.version);
          } else// update user server
          {
            int index = selectedServerIndex - _arrDefaulServerList.size();
            _arrUserServerList.remove(index);
            _arrUserServerList.add(index, serverObj);
            ExoServerConfiguration.createXmlDataWithServerList(_arrUserServerList,
                                                               "UserServerList.xml",
                                                               "");
          }
        }
      }
    } else if (v == btnDeleteCancel) {
      if (!isNewServer) // Delete sever
      {
        int currentServerIndex = AppController._intDomainIndex;
        if (currentServerIndex == selectedServerIndex) {
          AppController._intDomainIndex = -1;
          AppController._strDomain = "";
        } else if (currentServerIndex > selectedServerIndex) {
          AppController._intDomainIndex = currentServerIndex - 1;
        }

        if (serverObj._bSystemServer) {
          _arrDefaulServerList.remove(selectedServerIndex);
          _arrDeletedServerList.add(serverObj);
          ExoServerConfiguration.createXmlDataWithServerList(_arrDeletedServerList,
                                                             "DeletedDefaultServerList.xml",
                                                             "");
          ExoServerConfiguration.createXmlDataWithServerList(_arrDefaulServerList,
                                                             "DefaultServerList.xml",
                                                             AppController.configurationInstance.version);
        } else {
          int index = selectedServerIndex - _arrDefaulServerList.size();
          _arrUserServerList.remove(index);
          ExoServerConfiguration.createXmlDataWithServerList(_arrUserServerList,
                                                             "UserServerList.xml",
                                                             "");
        }
      }
    }

    serverInfoList = new ArrayList<ServerObj>();
    if (_arrDefaulServerList.size() > 0)
      serverInfoList.addAll(_arrDefaulServerList);
    if (_arrUserServerList.size() > 0)
      serverInfoList.addAll(_arrUserServerList);

    ExoModifyServerList.eXoModifyServerListInstance.createServersAdapter(serverInfoList);
    ExoSetting.eXoSettingInstance.setServerList(serverInfoList);
    AppController.createServersAdapter(serverInfoList);

    ServerSettingHelper.getInstance().setServerInfoList(serverInfoList);
    dismiss();
  }

  // Set language
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
