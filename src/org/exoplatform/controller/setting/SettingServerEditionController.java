package org.exoplatform.controller.setting;

import java.util.ArrayList;

import org.exoplatform.proxy.ExoServerConfiguration;
import org.exoplatform.proxy.ServerObj;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;

import android.content.Context;
import android.widget.ListView;
import android.widget.Toast;

public class SettingServerEditionController {
  private Context              mContext;

  private ArrayList<ServerObj> serverInfoList;       // List

  // of
  // server
  // url

  private ArrayList<ServerObj> _arrUserServerList;

  private ArrayList<ServerObj> _arrDefaulServerList;

  private ArrayList<ServerObj> _arrDeletedServerList;

  private boolean              isNewServer;

  private int                  selectedServerIndex;

  private String               version;

  public SettingServerEditionController(Context context) {
    mContext = context;
    init();
  }

  private void init() {
    serverInfoList = ServerSettingHelper.getInstance().getServerInfoList();
    _arrUserServerList = ServerSettingHelper.getInstance().getUserServerList();
    _arrDefaulServerList = ServerSettingHelper.getInstance().getDefaultServerList();
    _arrDeletedServerList = ServerSettingHelper.getInstance().getDeleteServerList();
    isNewServer = ServerSettingHelper.getInstance().getIsNewServer();
    selectedServerIndex = ServerSettingHelper.getInstance().getSelectedServerIndex();
    version = ServerSettingHelper.getInstance().getVersion();
  }

  public void onAccept(ServerObj myServerObj, ServerObj serverObj) {
    if (myServerObj._strServerName.equalsIgnoreCase("")
        || myServerObj._strServerUrl.equalsIgnoreCase("")) {
      // Server name or server url is empty
      Toast.makeText(mContext,
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
          Toast.makeText(mContext, "The server's existed", Toast.LENGTH_LONG).show();
          break;
          // New server is the same with the old one
        }
      }
      if (!isExisted) {
        // Create new server
        myServerObj._bSystemServer = false;
        _arrUserServerList.add(myServerObj);
        ExoServerConfiguration.createXmlDataWithServerList(_arrUserServerList, "UserServerList.xml", "");
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
          Toast.makeText(mContext, "The server's existed", Toast.LENGTH_LONG).show();
          break;
          // updated server is the same with the old one
        }
      }
      if (isExisted) {
        // Remove the old server
        _arrDefaulServerList.remove(selectedServerIndex);
        serverObj._strServerName = myServerObj._strServerName;
        serverObj._strServerUrl = myServerObj._strServerUrl;
        _arrDefaulServerList.add(selectedServerIndex, serverObj);
        if (serverObj._bSystemServer)// Update default server
        {
          _arrDefaulServerList.remove(selectedServerIndex);
          _arrDefaulServerList.add(selectedServerIndex, serverObj);
          ExoServerConfiguration.createXmlDataWithServerList(_arrDefaulServerList,
                                                             "DefaultServerList.xml",
                                                             version);
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
    onSave(myServerObj);
  }

  public void onDelete(ServerObj myServerObj, ServerObj serverObj) {
    if (!isNewServer) // Delete sever
    {
      int currentServerIndex = AccountSetting.getInstance().getDomainIndex();
      if (currentServerIndex == selectedServerIndex) {
        AccountSetting.getInstance().setDomainIndex(-1);
        AccountSetting.getInstance().setDomainName("");
      } else if (currentServerIndex > selectedServerIndex) {
        int index = currentServerIndex - 1;
        AccountSetting.getInstance().setDomainIndex(index);
      }

      if (serverObj._bSystemServer) {
        _arrDefaulServerList.remove(selectedServerIndex);
        _arrDeletedServerList.add(serverObj);
        ExoServerConfiguration.createXmlDataWithServerList(_arrDeletedServerList,
                                                           "DeletedDefaultServerList.xml",
                                                           "");
        ExoServerConfiguration.createXmlDataWithServerList(_arrDefaulServerList,
                                                           "DefaultServerList.xml",
                                                           version);
      } else {
        int index = selectedServerIndex - _arrDefaulServerList.size();
        _arrUserServerList.remove(index);
        ExoServerConfiguration.createXmlDataWithServerList(_arrUserServerList,
                                                           "UserServerList.xml",
                                                           "");
      }
    }
    onSave(myServerObj);
  }

  private void onSave(ServerObj myServerObj) {
    serverInfoList = new ArrayList<ServerObj>();
    if (_arrDefaulServerList.size() > 0)
      serverInfoList.addAll(_arrDefaulServerList);
    if (_arrUserServerList.size() > 0)
      serverInfoList.addAll(_arrUserServerList);

    // ExoModifyServerList.eXoModifyServerListInstance.createServersAdapter(AppController.configurationInstance._arrServerList);
    // ExoSetting.eXoSettingInstance.setServerList(AppController.configurationInstance._arrServerList);
    // AppController.createServersAdapter(AppController.configurationInstance._arrServerList);

    ServerSettingHelper.getInstance().setServerInfoList(serverInfoList);
    ServerSettingHelper.getInstance().setUserServerList(_arrUserServerList);
    ServerSettingHelper.getInstance().setDefaultServerList(_arrDefaulServerList);
    ServerSettingHelper.getInstance().setDeleteServerList(_arrDeletedServerList);
  }

  public void onResetAdapter(ListView listViewServer) {
    listViewServer.setAdapter(new ModifyServerAdapter(mContext));
  }

}
