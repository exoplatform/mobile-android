/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describe an Account configuration, containing the URL to connect to the server and credentials.
 * A server object information is identified by the combination: server name - server url - username
 */
public class ServerObjInfo implements Parcelable {

  /** Describe the server; if nothing specified, use authority of url */
  public String  serverName;

  /** url of server, contains the protocol (http:// ) */
  public String  serverUrl;

  /** username */
  public String  username;

  /** unencrypted password */
  public String  password;

  /**
   * Whether remember me is enabled on this credential
   * by default remember me is set to true
   */
  public boolean isRememberEnabled;

  /** Whether autologin is enabled */
  public boolean isAutoLoginEnabled;

  public ServerObjInfo() {
    serverName = "";
    serverUrl  = "";
    username   = "";
    password   = "";
    isRememberEnabled  = true;
    isAutoLoginEnabled = true;
  }

  private ServerObjInfo(Parcel in) {
    readFromParcel(in);
  }

  public static final Parcelable.Creator<ServerObjInfo> CREATOR = new Parcelable.Creator<ServerObjInfo>() {
                                                                  public ServerObjInfo createFromParcel(Parcel in) {
                                                                    return new ServerObjInfo(in);
                                                                  }

                                                                  public ServerObjInfo[] newArray(int size) {
                                                                    return new ServerObjInfo[size];
                                                                  }
                                                                };

  private void readFromParcel(Parcel in) {
    serverName = in.readString();
    serverUrl  = in.readString();
    username   = in.readString();
    password   = in.readString();
    isRememberEnabled  = in.readByte() == 1;
    isAutoLoginEnabled = in.readByte() == 1;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(serverName);
    dest.writeString(serverUrl);
    dest.writeString(username);
    dest.writeString(password);
    dest.writeByte((byte) (isRememberEnabled  ? 1 : 0));
    dest.writeByte((byte) (isAutoLoginEnabled ? 1 : 0));
  }

  /**
   * Compares this ServerObjInfo with the one given.<br/>
   * Returns true if server name and server URL and username are identical.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof  ServerObjInfo)) return false;
    ServerObjInfo _server = (ServerObjInfo) obj;
    if (_server.serverName.equals(serverName) && _server.serverUrl.equals(serverUrl) && _server.username.equals(username)) return true;
    return false;
  }

  /** clones this instance */
  public ServerObjInfo clone() {
    ServerObjInfo _server = new ServerObjInfo();
    _server.serverUrl  = serverUrl;
    _server.serverName = serverName;
    _server.username   = username;
    _server.password   = password;
    _server.isAutoLoginEnabled = isAutoLoginEnabled;
    _server.isRememberEnabled  = isRememberEnabled;
    return _server;
  }

  @Override
  public int hashCode() {
    return (serverUrl + username).hashCode();
  }
}
