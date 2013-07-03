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
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 12, 2011
 */
public class ServerObjInfo implements Parcelable {

  public String  _strServerName; // Name of server

  public String  _strServerUrl; // URL of server

  /* username */
  public String  username;

  /* encrypted password */
  public String  password;

  //public boolean _bSystemServer; // Is default server

  public ServerObjInfo() {
    _strServerName = "";
    _strServerUrl  = "";
    //_bSystemServer = false;
    username       = "";
    password       = "";
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
    _strServerName = in.readString();
    _strServerUrl  = in.readString();
    //_bSystemServer = (Boolean) in.readValue(null);
    username       = in.readString();
    password       = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(_strServerName);
    dest.writeString(_strServerUrl);
    //dest.writeValue(_bSystemServer);
    dest.writeString(username);
    dest.writeString(password);
  }

}
