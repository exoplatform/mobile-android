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
package org.exoplatform.singleton;

import java.util.ArrayList;

import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ServerConfigurationUtils;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used for storing the list of server url and the index of the selected
 * which is used for adding/repairing/deleting function in setting
 */
public class ServerSettingHelper implements Parcelable {

  // The application version number
  private String                     applicationVersion;

  // the server version number
  private String                     serverVersion;

  private String                     serverEdition;

  /** List of server url */
  private ArrayList<ServerObjInfo> serverInfoList;

  private static ServerSettingHelper helper = new ServerSettingHelper();

  private ServerSettingHelper() {

  }

  public static ServerSettingHelper getInstance() {
    return helper;
  }

  public void setInstance(ServerSettingHelper instance) {
    helper = instance;
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

  /**
   * Returns the list of server objects configured in the app.<br/>
   * If the property has not yet been set, the list is retrieved from storage lazily.
   * @param context
   * @return The list of server objects
   */
  public ArrayList<ServerObjInfo> getServerInfoList(Context context) {
    if (serverInfoList == null) {
      serverInfoList =
          ServerConfigurationUtils.getServerListFromFile(context, ExoConstants.EXO_SERVER_SETTING_FILE);
      ServerSettingHelper.getInstance().setServerInfoList(serverInfoList);
    }
    return serverInfoList;
  }
  
  /**
   * Checks whether two or more accounts are configured on the app
   * @param ctx
   * @return true if two or more accounts exist, false otherwise
   */
  public boolean twoOrMoreAccountsExist(Context ctx) {
	  return (getServerInfoList(ctx).size() > 1);
  }

  @Deprecated
  /**
   * Use getServerInfoList(Context) instead
   * @return the list of servers
   */
  public ArrayList<ServerObjInfo> getServerInfoList() {
    return serverInfoList;
  }

  private ServerSettingHelper(Parcel in) {
    readFromParcel(in);
  }

  public static final Parcelable.Creator<ServerSettingHelper> CREATOR = new Parcelable.Creator<ServerSettingHelper>() {
                                                                        public ServerSettingHelper createFromParcel(Parcel in) {
                                                                          return new ServerSettingHelper(in);
                                                                        }

                                                                        public ServerSettingHelper[] newArray(int size) {
                                                                          return new ServerSettingHelper[size];
                                                                        }
                                                                      };

  private void readFromParcel(Parcel in) {
    applicationVersion = in.readString();
    serverVersion = in.readString();
    serverEdition = in.readString();
    serverInfoList = new ArrayList<ServerObjInfo>();
    in.readList(serverInfoList, ServerObjInfo.class.getClassLoader());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(applicationVersion);
    dest.writeString(serverVersion);
    dest.writeString(serverEdition);
    dest.writeList(serverInfoList);
  }

}
