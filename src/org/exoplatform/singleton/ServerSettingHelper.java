package org.exoplatform.singleton;

import java.util.ArrayList;

import org.exoplatform.model.ServerObjInfo;

/*
 * This class for storing the list of server url and the index of the selected 
 *  which is used for adding/repairing/deleting function in setting   
 */

public class ServerSettingHelper {

  // The index of server url was selected in setting
  private int                        selectedServerIndex;

  // if this server url is new url
  private boolean                    isNewServer;

  // The application version number
  private String                     applicationVersion;

  // the server version number
  private String                     serverVersion;

  private String                     serverEdition;

  // public boolean isMobileCompliant = false;

  // List of server url
  private ArrayList<ServerObjInfo>   serverInfoList;

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

  public void setApplicationVersion(String version) {
    applicationVersion = version;
  }

  public String getApplicationVersion() {
    return applicationVersion;
  }

  public void setServerVersion(String ver) {
    serverVersion = ver;
  }

  public String getServerVersion() {
    return serverVersion;
  }

  public void setServerEdition(String ver) {
    serverEdition = ver;
  }

  public String getServerEdition() {
    return serverEdition;
  }

  public void setServerInfoList(ArrayList<ServerObjInfo> list) {
    serverInfoList = list;
  }

  public ArrayList<ServerObjInfo> getServerInfoList() {
    return serverInfoList;
  }

}
