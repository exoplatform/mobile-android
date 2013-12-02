package org.exoplatform.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ExoFile implements Parcelable {

  /**
   * JCR url of the file <br/>
   * Root folder or folders contained in root has this value empty
   */
  public String  path;

  /**
   * Indicate whether the item is file or folder
   */
  public boolean isFolder;

  public boolean canRemove;

  /**
   * Name of the file / folder <br/>
   * Only Root folder has this value empty
   */
  public String  name;

  /**
   * The folder that contains the item, if the item is a folder then
   * it's the same as name <br/>
   * Root folder or folders contained in root has this value empty
   */
  public String  currentFolder;

  public String  driveName;    // drive name of file

  public String  workspaceName; // work space of file

  public String  nodeType;     // file content type

  // Default constructors
  public ExoFile() {
    path = "";
    isFolder = true;
    canRemove = false;
    name = "";
    currentFolder = "";
    driveName = "";
    workspaceName = "";
    nodeType = "";
  }

  public ExoFile(String driverName) {
    path = "";
    isFolder = true;
    canRemove = false;
    name = "";
    currentFolder = "";
    this.driveName = driverName;
    workspaceName = "";
    nodeType = "";
  }

  private ExoFile(Parcel in) {
    readFromParcel(in);
  }

  private void readFromParcel(Parcel in) {
    path = in.readString();
    isFolder = (Boolean) in.readValue(null);
    canRemove = (Boolean) in.readValue(null);
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
    par.writeValue(canRemove);
    par.writeString(name);
    par.writeString(currentFolder);
    par.writeString(driveName);
    par.writeString(workspaceName);
    par.writeString(nodeType);
  }

}
