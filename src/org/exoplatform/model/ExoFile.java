package org.exoplatform.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ExoFile implements Parcelable {

  public String  path;         // File's jcr url

  public boolean isFolder;     // Is folder

  public String  name;         // name of the file/folder

  public String  currentFolder; // the path of file

  public String  driveName;    // drive name of file

  public String  workspaceName; // work space of file

  public String  nodeType;     // file content type

  // Default constructors
  public ExoFile() {
    path = "";
    isFolder = true;
    name = "";
    currentFolder = "";
    driveName = "";
    workspaceName = "";
    nodeType = "";
  }

  private ExoFile(Parcel in) {
    readFromParcel(in);
  }

  private void readFromParcel(Parcel in) {
    path = in.readString();
    isFolder =(Boolean) in.readValue(null);
//    in.readBooleanArray(new boolean[] { isFolder });
    name = in.readString();
    currentFolder = in.readString();
    driveName = in.readString();
    workspaceName = in.readString();
    nodeType = in.readString();

  }

  public static final Parcelable.Creator<ExoFile> CREATOR = new Parcelable.Creator<ExoFile>() {
                                                            public ExoFile createFromParcel(Parcel in) {
                                                              return new ExoFile(in);
                                                            }

                                                            public ExoFile[] newArray(int size) {
                                                              return new ExoFile[size];
                                                            }
                                                          };

  /*
   * (non-Javadoc)
   * @see android.os.Parcelable#describeContents()
   */
  @Override
  public int describeContents() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
   */
  @Override
  public void writeToParcel(Parcel par, int flags) {
    par.writeString(path);
    par.writeValue(isFolder);
//    par.writeBooleanArray(new boolean[] { isFolder });
    par.writeString(name);
    par.writeString(currentFolder);
    par.writeString(driveName);
    par.writeString(workspaceName);
    par.writeString(nodeType);
  }

}
