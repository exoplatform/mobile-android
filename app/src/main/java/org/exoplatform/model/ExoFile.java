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

import java.util.List;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

public class ExoFile implements Parcelable {

  /**
   * File's JCR URL
   */
  public String        path;

  /**
   * Whether this file is a folder
   */
  public boolean       isFolder;

  /**
   * Whether this file can be deleted, renamed
   */
  public boolean       canRemove;

  /**
   * Name of the file/folder
   */
  public String        name;

  /**
   * The path of file
   */
  public String        currentFolder;

  /**
   * The drive name in which this file is
   */
  public String        driveName;

  /**
   * The workspace in which this file is
   */
  public String        workspaceName;

  /**
   * File content type
   */
  public String        nodeType;

  /**
   * Natural name used instead of the technical name when the file is a group
   * folder<br/>
   * e.g. .space.mobile => Mobile
   */
  private String       naturalName;

  /**
   * List of sub-files and sub-folders
   */
  public List<ExoFile> children;

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
    naturalName = "";
    children = null;
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
    naturalName = "";
    children = null;
  }

  private ExoFile(Parcel in) {
    readFromParcel(in);
  }

  /**
   * The <i>name</i> of the file / folder.<br/>
   * If the <i>naturalName</i> exists, <i>naturalName</i> is returned instead.
   * 
   * @return a name for the file / folder
   */
  public String getName() {
    if (naturalName == null || "".equals(naturalName))
      return name;
    else
      return naturalName;
  }

  /**
   * Create the natural name of the folder, based on the <i>name</i> property,
   * as follows
   * <ul>
   * <li>1st dot is removed</li>
   * <li>other '.' and '_' are replaced by spaces</li>
   * <li>all remaining words must start by a capital letter</li>
   * <li>exo is replaced by eXo</li>
   * <li>ending 's' in 'spaces' is removed</li>
   * </ul>
   * The <i>naturalName</i> property is created.
   */
  public void createNaturalName() {
    StringBuilder sb = new StringBuilder();
    // replace all _ and - by . to be treated by the split
    String tmpName = name.replace('_', '.');
    tmpName = tmpName.replace('-', '.');
    // initial 'spaces' is removed
    if (tmpName.startsWith(".spaces."))
      tmpName = tmpName.substring(".spaces.".length());
    // split by . will remove them from the resulting string
    String[] words = tmpName.split("\\.");
    for (String word : words) {
      if ("exo".equals(word)) { // exo is replaced by eXo
        sb.append("eXo ");
        continue;
      }
      // all remaining words must start by a capital letter
      sb.append(toCapitalCase(word)).append(" ");
    }
    naturalName = sb.toString().trim();
  }

  /**
   * Returns a string with the initial letter in upper case and the rest in
   * lower case
   * 
   * @param word the word to capitalize
   * @return the capitalized word
   */
  private String toCapitalCase(String word) {
    if (word == null || word.length() == 0)
      return "";
    String initial = String.valueOf(word.charAt(0)).toUpperCase(Locale.US);
    String remainder = word.substring(1).toLowerCase(Locale.US);
    return initial + remainder;
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
    naturalName = in.readString();
    in.readList(children, this.getClass().getClassLoader());
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
    par.writeString(naturalName);
    par.writeList(children);
  }
}
