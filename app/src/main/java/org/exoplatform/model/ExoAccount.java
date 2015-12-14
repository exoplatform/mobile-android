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
 * Describe an Account configuration, containing the URL to connect to the
 * server and credentials. A server object information is identified by the
 * combination: server name - server url - username
 */
public class ExoAccount implements Parcelable {

  /** Describe the server; if nothing specified, use authority of url */
  public String  accountName;

  /** url of server, contains the protocol (http:// ) */
  public String  serverUrl;

  /** username */
  public String  username;

  /** unencrypted password */
  public String  password;

  public String  avatarUrl;

  public String  userFullName;

  public long    lastLoginDate;

  /**
   * Whether remember me is enabled on this credential by default remember me is
   * set to true
   */
  public boolean isRememberEnabled;

  /** Whether autologin is enabled */
  public boolean isAutoLoginEnabled;

  public ExoAccount() {
    accountName = "";
    serverUrl = "";
    username = "";
    password = "";
    isRememberEnabled = false;
    isAutoLoginEnabled = false;
    userFullName = "";
    avatarUrl = "";
    lastLoginDate = -1;
  }

  private ExoAccount(Parcel in) {
    readFromParcel(in);
  }

  public static final Parcelable.Creator<ExoAccount> CREATOR = new Parcelable.Creator<ExoAccount>() {
                                                               public ExoAccount createFromParcel(Parcel in) {
                                                                 return new ExoAccount(in);
                                                               }

                                                               public ExoAccount[] newArray(int size) {
                                                                 return new ExoAccount[size];
                                                               }
                                                             };

  private void readFromParcel(Parcel in) {
    accountName = in.readString();
    serverUrl = in.readString();
    username = in.readString();
    password = in.readString();
    isRememberEnabled = in.readByte() == 1;
    isAutoLoginEnabled = in.readByte() == 1;
    userFullName = in.readString();
    avatarUrl = in.readString();
    lastLoginDate = in.readLong();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(accountName);
    dest.writeString(serverUrl);
    dest.writeString(username);
    dest.writeString(password);
    dest.writeByte((byte) (isRememberEnabled ? 1 : 0));
    dest.writeByte((byte) (isAutoLoginEnabled ? 1 : 0));
    dest.writeString(userFullName);
    dest.writeString(avatarUrl);
    dest.writeLong(lastLoginDate);
  }

  /**
   * Compares this ServerObjInfo with the one given.<br/>
   * Returns true if server name and server URL and username are identical.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ExoAccount))
      return false;
    ExoAccount _server = (ExoAccount) obj;
    return _server.accountName.equals(accountName) && _server.serverUrl.equals(serverUrl) && _server.username.equals(username);
  }

  /** clones this instance */
  public ExoAccount clone() {
    ExoAccount newAccount = new ExoAccount();
    newAccount.serverUrl = serverUrl;
    newAccount.accountName = accountName;
    newAccount.username = username;
    newAccount.password = password;
    newAccount.isAutoLoginEnabled = isAutoLoginEnabled;
    newAccount.isRememberEnabled = isRememberEnabled;
    newAccount.userFullName = userFullName;
    newAccount.avatarUrl = avatarUrl;
    newAccount.lastLoginDate = lastLoginDate;
    return newAccount;
  }

  @Override
  public int hashCode() {
    return (serverUrl + username).hashCode();
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder("Account Details:\n");
    b.append("* Name: ").append(accountName).append("\n");
    b.append("* URL: ").append(serverUrl).append("\n");
    b.append("* User: ").append(username).append("\n");
    b.append("* RM: [").append(isRememberEnabled).append("] / AL: [").append(isAutoLoginEnabled).append("]\n");
    b.append("* Full name: ").append(userFullName).append("\n");
    b.append("* Last login: ").append(lastLoginDate).append("\n");
    b.append("* Avatar: ").append(avatarUrl).append("\n");
    return b.toString();
  }
}
