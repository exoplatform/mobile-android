package org.exoplatform.model;

public class ServerInfo {
  private String  _strServerName; // Name of server

  private String  _strServerUrl; // URL of server

  private boolean _bSystemServer; // Is default server

  public ServerInfo() {

  }

  public String getServerName() {
    return _strServerName;
  }

  public void setServerName(String name) {
    _strServerName = name;
  }

  public String getServerUrl() {
    return _strServerUrl;
  }

  public void setServerUrl(String url) {
    _strServerUrl = url;
  }

  public boolean isDefaultServer() {
    return _bSystemServer;
  }
  
  public void setDefaultServer(boolean sys){
    _bSystemServer = sys;
  }

}
