package org.exoplatform.singleton;

import java.util.ArrayList;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.exoplatform.proxy.ServerObj;
/*
 * This class is for manage all the account information
 * 
 */
public class AccountSetting {

  private static AccountSetting accountSetting = new AccountSetting();

  private String                userName; // username

  private String                password; // password

  private String                domainName; // checked server url

  private int                   domainIndex;// the index of checked server url

//  private ArrayList<ServerObj>  serverInfoList; // List of server url

  private boolean               isNewVersion; // is new version or not
  
  private AuthScope                   auth            ; // the authentication scope for HttpClient

  private UsernamePasswordCredentials credential      ; // username and password credentials for HttpClient

  private AccountSetting() {

  }

  public static AccountSetting getInstance() {
    return accountSetting;
  }

  public String getUsername() {
    return userName;
  }

  public void setUsername(String name) {
    userName = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String pwd) {
    password = pwd;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String url) {
    domainName = url;
  }

  public int getDomainIndex() {
    return domainIndex;
  }

  public void setDomainIndex(int index) {
    domainIndex = index;
  }

//  public void setServerInfoList(ArrayList<ServerObj> list) {
//    serverInfoList = list;
//  }
//
//  public ArrayList<ServerObj> getServerInfoList() {
//    return serverInfoList;
//  }

  public void setIsNewVersion(boolean is) {
    isNewVersion = is;
  }

  public boolean getIsNewVersion() {
    return isNewVersion;
  }
  
  public void setAuthScope(AuthScope au){
    auth = au;
  }
  
  public AuthScope getAuthScope(){
    return auth;
  }
  
  public void setCredentials(UsernamePasswordCredentials upc){
    credential = upc;
  }
  
  public UsernamePasswordCredentials getCredentials(){
    return credential;
  }
  
  
}
