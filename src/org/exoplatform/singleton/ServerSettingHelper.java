package org.exoplatform.singleton;

import java.util.ArrayList;
import java.util.HashSet;

import org.exoplatform.model.ServerObjInfo;

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
