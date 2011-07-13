package org.exoplatform.setting;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.exoplatform.R;
import org.exoplatform.controller.AppController;
import org.exoplatform.proxy.ExoServerConfiguration;
import org.exoplatform.proxy.ServerObj;

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
public class ExoLanguageSettingDialog extends Dialog implements OnClickListener {

  Button         btnOK;

  Button         btnDeleteCancel;

  TextView       txtvTittle;

  TextView       txtvServerName;

  TextView       txtvServerUrl;

  EditText       editTextServerName;

  EditText       editTextServerUrl;

  static int     selectedServerIndex;

  static boolean isNewServer;

  ServerObj      serverObj;

  // Constructor
  public ExoLanguageSettingDialog(Context context) {

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
    if (isNewServer) {
      serverObj = new ServerObj();
      serverObj._bSystemServer = false;
      serverObj._strServerName = "";
      serverObj._strServerUrl = "";
    } else {
      serverObj = AppController.configurationInstance._arrServerList.get(selectedServerIndex);
    }

    editTextServerName.setText(serverObj._strServerName);
    editTextServerUrl.setText(serverObj._strServerUrl);

    changeLanguage(AppController.bundle);
  }

  // Show user guide or change language
  public void onClick(View v) {
    /** When OK Button is clicked, dismiss the dialog */

    ExoServerConfiguration conf = AppController.configurationInstance;

    ServerObj myServerObj = new ServerObj();

    myServerObj._strServerName = editTextServerName.getText().toString();
    myServerObj._strServerUrl = editTextServerUrl.getText().toString();

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
        for (int i = 0; i < conf._arrServerList.size(); i++) {
          ServerObj tmp = conf._arrServerList.get(i);
          if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)
              && myServerObj._strServerUrl.equalsIgnoreCase(tmp._strServerUrl)) {
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
          AppController.configurationInstance._arrUserServerList.add(myServerObj);
          AppController.configurationInstance.createXmlDataWithServerList(AppController.configurationInstance._arrUserServerList,
                                                                          "UserServerList.xml",
                                                                          "");

        }
      } else // Update server
      {
        boolean isExisted = true;
        for (int i = 0; i < conf._arrServerList.size(); i++) {
          ServerObj tmp = conf._arrServerList.get(i);

          if (i == selectedServerIndex) {
            continue;
          }

          if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)
              && myServerObj._strServerUrl.equalsIgnoreCase(tmp._strServerUrl)) {
            isExisted = false;
            Toast.makeText(ExoSetting.eXoSettingInstance, "The server's existed", Toast.LENGTH_LONG)
                 .show();
            break;
            // updated server is the same with the old one
          }
        }
        if (isExisted) {
          // Remove the old server
          AppController.configurationInstance._arrServerList.remove(selectedServerIndex);
          serverObj._strServerName = myServerObj._strServerName;
          serverObj._strServerUrl = myServerObj._strServerUrl;
          AppController.configurationInstance._arrServerList.add(selectedServerIndex, serverObj);
          if (serverObj._bSystemServer)// Update default server
          {
            AppController.configurationInstance._arrDefaulServerList.remove(selectedServerIndex);
            AppController.configurationInstance._arrDefaulServerList.add(selectedServerIndex,
                                                                         serverObj);
            AppController.configurationInstance.createXmlDataWithServerList(AppController.configurationInstance._arrDefaulServerList,
                                                                            "DefaultServerList.xml",
                                                                            AppController.configurationInstance.version);
          } else// update user server
          {
            int index = selectedServerIndex
                - AppController.configurationInstance._arrDefaulServerList.size();
            AppController.configurationInstance._arrUserServerList.remove(index);
            AppController.configurationInstance._arrUserServerList.add(index, serverObj);
            AppController.configurationInstance.createXmlDataWithServerList(AppController.configurationInstance._arrUserServerList,
                                                                            "UserServerList.xml",
                                                                            "");
          }
        }
      }
    } else if (v == btnDeleteCancel) {
      if (!isNewServer) // Delete sever
      {
        int currentServerIndex = AppController.appControllerInstance._intDomainIndex;
        if (currentServerIndex == selectedServerIndex) {
          AppController.appControllerInstance._intDomainIndex = -1;
          AppController.appControllerInstance._strDomain = "";
        } else if (currentServerIndex > selectedServerIndex) {
          AppController.appControllerInstance._intDomainIndex = currentServerIndex - 1;
        }

        if (serverObj._bSystemServer) {
          AppController.configurationInstance._arrDefaulServerList.remove(selectedServerIndex);
          AppController.configurationInstance._arrDeletedServerList.add(serverObj);
          AppController.configurationInstance.createXmlDataWithServerList(AppController.configurationInstance._arrDeletedServerList,
                                                                          "DeletedDefaultServerList.xml",
                                                                          "");
          AppController.configurationInstance.createXmlDataWithServerList(AppController.configurationInstance._arrDefaulServerList,
                                                                          "DefaultServerList.xml",
                                                                          AppController.configurationInstance.version);
        } else {
          int index = selectedServerIndex
              - AppController.configurationInstance._arrDefaulServerList.size();
          AppController.configurationInstance._arrUserServerList.remove(index);
          AppController.configurationInstance.createXmlDataWithServerList(AppController.configurationInstance._arrUserServerList,
                                                                          "UserServerList.xml",
                                                                          "");
        }
      }
    }

    AppController.configurationInstance._arrServerList = new ArrayList<ServerObj>();
    if (AppController.configurationInstance._arrDefaulServerList.size() > 0)
      AppController.configurationInstance._arrServerList.addAll(AppController.configurationInstance._arrDefaulServerList);
    if (AppController.configurationInstance._arrUserServerList.size() > 0)
      AppController.configurationInstance._arrServerList.addAll(AppController.configurationInstance._arrUserServerList);

    ExoModifyServerList.eXoModifyServerListInstance.createServersAdapter(AppController.configurationInstance._arrServerList);
    ExoSetting.eXoSettingInstance.createServersAdapter(AppController.configurationInstance._arrServerList);
    AppController.appControllerInstance.createServersAdapter(AppController.configurationInstance._arrServerList);

    dismiss();
  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {

    String strTittle = "";
    String strServerName = "";
    String strServerUrl = "";

    String strOKButton = "";
    String strDeleteCancelButton = "";

    try {
      if (isNewServer) // New server
      {
        strTittle = new String(resourceBundle.getString("NewServer").getBytes("ISO-8859-1"),
                               "UTF-8");
        strDeleteCancelButton = new String(resourceBundle.getString("Cancel")
                                                         .getBytes("ISO-8859-1"), "UTF-8");
      } else // Server detail
      {
        strTittle = new String(resourceBundle.getString("ServerDetail").getBytes("ISO-8859-1"),
                               "UTF-8");
        strDeleteCancelButton = new String(resourceBundle.getString("Delete")
                                                         .getBytes("ISO-8859-1"), "UTF-8");
      }
      
      strServerName = new String(resourceBundle.getString("NameOfTheServer").getBytes("ISO-8859-1"),
                                 "UTF-8");
      strServerUrl = new String(resourceBundle.getString("URLOfTheSerVer").getBytes("ISO-8859-1"),
                                "UTF-8");

      strOKButton = new String(resourceBundle.getString("OK").getBytes("ISO-8859-1"), "UTF-8");

    } catch (Exception e) {

    }

    txtvTittle.setText(strTittle);
    
    
    txtvServerName.setText(strServerName);
    txtvServerUrl.setText(strServerUrl);

    btnOK.setText(strOKButton);
    btnDeleteCancel.setText(strDeleteCancelButton);

  }
}
