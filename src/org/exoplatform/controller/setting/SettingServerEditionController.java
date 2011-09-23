package org.exoplatform.controller.setting;

import java.util.ArrayList;

import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.proxy.ExoServerConfiguration;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ListView;
import android.widget.Toast;

public class SettingServerEditionController {
  private Context              mContext;

  private ArrayList<ServerObjInfo> serverInfoList;     // List

  // of
  // server
  // url

  private boolean              isNewServer;

  private int                  selectedServerIndex;

  private String               version;

  private String               serverIsEmpty;

  private String               serverisExisted;

  public SettingServerEditionController(Context context) {
    mContext = context;
    init();
  }

  private void init() {
    serverInfoList = ServerSettingHelper.getInstance().getServerInfoList();
    isNewServer = ServerSettingHelper.getInstance().getIsNewServer();
    selectedServerIndex = ServerSettingHelper.getInstance().getSelectedServerIndex();
    version = ServerSettingHelper.getInstance().getVersion();
    changeLanguage();
  }

  public void onAccept(ServerObjInfo myServerObj, ServerObjInfo serverObj) {
    if (myServerObj._strServerName.equalsIgnoreCase("")
        || myServerObj._strServerUrl.equalsIgnoreCase("")) {
      // Server name or server url is empty
      Toast.makeText(mContext, serverIsEmpty, Toast.LENGTH_LONG).show();
      return;
    }
    if (isNewServer) {
      boolean isExisted = false;
      for (int i = 0; i < serverInfoList.size(); i++) {
        ServerObjInfo tmp = serverInfoList.get(i);
        if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)) {
          isExisted = true;
          Toast.makeText(mContext, serverisExisted, Toast.LENGTH_LONG).show();
          break;
          // New server is the same with the old one
        }
      }
      if (!isExisted) {
        // Create new server
        myServerObj._bSystemServer = false;
        serverInfoList.add(myServerObj);
        ExoServerConfiguration.createXmlDataWithServerList(serverInfoList,
                                                           "DefaultServerList.xml",
                                                           "");
      }
    } else // Update server
    {
      boolean isExisted = true;
      for (int i = 0; i < serverInfoList.size(); i++) {
        ServerObjInfo tmp = serverInfoList.get(i);

        if (i == selectedServerIndex) {
          continue;
        }

        if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)) {
          isExisted = false;
          Toast.makeText(mContext, serverisExisted, Toast.LENGTH_LONG).show();
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
        ExoServerConfiguration.createXmlDataWithServerList(serverInfoList,
                                                           "DefaultServerList.xml",
                                                           "");
      }
    }
    onSave(myServerObj);
  }

  public void onDelete(ServerObjInfo myServerObj, ServerObjInfo serverObj) {
    if (!isNewServer) // Delete sever
    {
      int currentServerIndex = Integer.valueOf(AccountSetting.getInstance().getDomainIndex());
      if (currentServerIndex == selectedServerIndex) {
        AccountSetting.getInstance().setDomainIndex(String.valueOf(-1));
        AccountSetting.getInstance().setDomainName("");
      } else if (currentServerIndex > selectedServerIndex) {
        int index = currentServerIndex - 1;
        AccountSetting.getInstance().setDomainIndex(String.valueOf(index));
      }
      serverInfoList.remove(selectedServerIndex);
      ExoServerConfiguration.createXmlDataWithServerList(serverInfoList,
                                                         "DefaultServerList.xml",
                                                         "");

    }
    onSave(myServerObj);
  }

  private void onSave(ServerObjInfo myServerObj) {

    ServerSettingHelper.getInstance().setServerInfoList(serverInfoList);
    SharedPreferences.Editor editor = LocalizationHelper.getInstance().getSharePrefs().edit();
    editor.putString(ExoConstants.EXO_PRF_DOMAIN, AccountSetting.getInstance().getDomainName());
    editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, AccountSetting.getInstance()
                                                                      .getDomainIndex());
    editor.commit();
  }

  public void onResetAdapter(ListView listViewServer) {
    listViewServer.setAdapter(new ModifyServerAdapter(mContext));
  }

  private void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    serverIsEmpty = bundle.getString("WarningServerNameIsEmpty");
    serverisExisted = bundle.getString("WarningServerIsExist");

  }

}
