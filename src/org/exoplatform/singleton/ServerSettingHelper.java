package org.exoplatform.singleton;

import java.util.ArrayList;

import org.exoplatform.proxy.ServerObj;

public class ServerSettingHelper {
  private int                        selectedServerIndex;

  private boolean                    isNewServer;

  private String                     version;

  private ArrayList<ServerObj>       serverInfoList;                    // List
                                                                         // of
                                                                         // server
                                                                         // url

  private ArrayList<ServerObj>       _arrUserServerList;

  private ArrayList<ServerObj>       _arrDefaulServerList;

  private ArrayList<ServerObj>       _arrDeletedServerList;

  private static ServerSettingHelper helper = new ServerSettingHelper();

  private ServerSettingHelper() {

  }

  public static ServerSettingHelper getInstance() {
    return helper;
  }

  public void setSelectedServerIndex(int index) {
    selectedServerIndex = index;
  }

  public int getSelectedServerIndex() {
    return selectedServerIndex;
  }

  public void setIsNewServer(boolean is) {
    isNewServer = is;
  }

  public boolean getIsNewServer() {
    return isNewServer;
  }

  public void setVersion(String ver) {
    version = ver;
  }

  public String getVersion() {
    return version;
  }

  public void setServerInfoList(ArrayList<ServerObj> list) {
    serverInfoList = list;
  }

  public ArrayList<ServerObj> getServerInfoList() {
    return serverInfoList;
  }

  public void setUserServerList(ArrayList<ServerObj> list) {
    _arrUserServerList = list;
  }

  public ArrayList<ServerObj> getUserServerList() {
    return _arrUserServerList;
  }

  public void setDefaultServerList(ArrayList<ServerObj> list) {
    _arrDefaulServerList = list;
  }

  public ArrayList<ServerObj> getDefaultServerList() {
    return _arrDefaulServerList;
  }

  public void setDeleteServerList(ArrayList<ServerObj> list) {
    _arrDeletedServerList = list;
  }

  public ArrayList<ServerObj> getDeleteServerList() {
    return _arrDeletedServerList;
  }
}
