package org.exoplatform.controller.setting;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.ServerConfigurationUtils;
import org.exoplatform.utils.URLAnalyzer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.widget.ListView;
import android.widget.Toast;

public class SettingServerEditionController {
  private Context                  mContext;

  private ArrayList<ServerObjInfo> serverInfoList;      // List

  // of
  // server
  // url

  private boolean                  isNewServer;

  private int                      selectedServerIndex;

  private String                   serverIsEmpty;

  private String                   serverNameIsExisted;

  private String                   serverUrlIsExisted;

  private String                   serverNameURLInvalid;

  public SettingServerEditionController(Context context) {
    mContext = context;
    init();
  }

  private void init() {
    serverInfoList = ServerSettingHelper.getInstance().getServerInfoList();
    isNewServer = ServerSettingHelper.getInstance().getIsNewServer();
    selectedServerIndex = ServerSettingHelper.getInstance().getSelectedServerIndex();
    // version = ServerSettingHelper.getInstance().getVersion();
    changeLanguage();
  }

  public boolean onAccept(ServerObjInfo myServerObj) {

    if (myServerObj._strServerName.length() == 0 || myServerObj._strServerUrl.length() == 0) {

      Toast.makeText(mContext, serverIsEmpty, Toast.LENGTH_SHORT).show();
      return false;
    }
    URLAnalyzer urlAnanyzer = new URLAnalyzer();
    myServerObj._strServerUrl = urlAnanyzer.parserURL(myServerObj._strServerUrl);

    if (ExoDocumentUtils.isContainSpecialChar(myServerObj._strServerName,
                                              ExoConstants.SPECIAL_CHAR_NAME_SET)
        || ExoDocumentUtils.isContainSpecialChar(myServerObj._strServerUrl,
                                                 ExoConstants.SPECIAL_CHAR_URL_SET)) {

      Toast.makeText(mContext, serverNameURLInvalid, Toast.LENGTH_SHORT).show();
      return false;

    }

    if (isNewServer) {
      boolean isExisted = false;
      for (int i = 0; i < serverInfoList.size(); i++) {
        ServerObjInfo tmp = serverInfoList.get(i);
        if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)) {
          isExisted = true;
          Toast.makeText(mContext, serverNameIsExisted, Toast.LENGTH_SHORT).show();
          break;
        }

        if (myServerObj._strServerUrl.equalsIgnoreCase(tmp._strServerUrl)) {
          isExisted = true;
          Toast.makeText(mContext, serverUrlIsExisted, Toast.LENGTH_SHORT).show();
          break;
        }

      }
      if (!isExisted) {
        // Create new server
        myServerObj._bSystemServer = false;
        serverInfoList.add(myServerObj);
        ServerConfigurationUtils.createXmlDataWithServerList(serverInfoList,
                                                             "DefaultServerList.xml",
                                                             "");
      } else
        return false;
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
          Toast.makeText(mContext, serverNameIsExisted, Toast.LENGTH_SHORT).show();
          break;
        }

        if (myServerObj._strServerUrl.equalsIgnoreCase(tmp._strServerUrl)) {
          isExisted = false;
          Toast.makeText(mContext, serverUrlIsExisted, Toast.LENGTH_SHORT).show();
          break;
        }
      }
      if (isExisted) {
        // Remove the old server
        serverInfoList.remove(selectedServerIndex);
        myServerObj._strServerName = myServerObj._strServerName;
        myServerObj._strServerUrl = myServerObj._strServerUrl;
        serverInfoList.add(selectedServerIndex, myServerObj);
        ServerConfigurationUtils.createXmlDataWithServerList(serverInfoList,
                                                             "DefaultServerList.xml",
                                                             "");
      } else
        return false;
    }
    onSave();
    return true;
  }

  public void onDelete() {
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
      ServerConfigurationUtils.createXmlDataWithServerList(serverInfoList,
                                                           "DefaultServerList.xml",
                                                           "");

    }
    onSave();
  }

  private void onSave() {

    ServerSettingHelper.getInstance().setServerInfoList(serverInfoList);
    SharedPreferences.Editor editor = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0)
                                              .edit();
    editor.putString(ExoConstants.EXO_PRF_DOMAIN, AccountSetting.getInstance().getDomainName());
    editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, AccountSetting.getInstance()
                                                                      .getDomainIndex());
    editor.commit();
  }

  public void onResetAdapter(ListView listViewServer) {
    listViewServer.setAdapter(new ModifyServerAdapter(mContext));
  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    serverIsEmpty = resource.getString(R.string.WarningServerNameIsEmpty);
    serverNameIsExisted = resource.getString(R.string.WarningServerNameIsExist);
    serverUrlIsExisted = resource.getString(R.string.WarningServerUrlIsExist);
    serverNameURLInvalid = resource.getString(R.string.SpecialCharacters);

  }

}
