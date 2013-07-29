/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describe a server configuration, containing url to connect to server and
 * credentials.
 *
 * A server object information is identified by a pair of server's url and username
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

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof  ServerObjInfo)) return false;
    ServerObjInfo _server = (ServerObjInfo) obj;
    if (_server.serverUrl.equals(serverUrl) && _server.username.equals(username)) return true;
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
