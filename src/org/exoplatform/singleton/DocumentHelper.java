/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.singleton;

import org.exoplatform.model.ExoFile;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jan
 * 31, 2012
 */
public class DocumentHelper implements Parcelable {

  private static DocumentHelper documentHelper = new DocumentHelper();

  private String                _urlrepositoryHome;

  public ExoFile                _fileCopied    = new ExoFile();                    ;

  public ExoFile                _fileMoved     = new ExoFile();

  public String                 repository     = null;

  /*
   * The dictionary for mapping between parent folder and its child files
   */
  public Bundle                 childFilesMap;

  /*
   * The dictionary for mapping between the current selected file and its parent
   */

  public Bundle                 currentFileMap;

  private DocumentHelper() {

  }

  public static DocumentHelper getInstance() {
    return documentHelper;
  }

  public void setInstance(DocumentHelper helper) {
    documentHelper = helper;
  }

  public void setRepositoryHomeUrl(String url) {
    _urlrepositoryHome = url;
  }

  public String getRepositoryHomeUrl() {
    return _urlrepositoryHome;
  }

  private DocumentHelper(Parcel in) {
    readFromParcel(in);
  }

  public static final Parcelable.Creator<DocumentHelper> CREATOR = new Parcelable.Creator<DocumentHelper>() {
                                                                   public DocumentHelper createFromParcel(Parcel in) {
                                                                     return new DocumentHelper(in);
                                                                   }

                                                                   public DocumentHelper[] newArray(int size) {
                                                                     return new DocumentHelper[size];
                                                                   }
                                                                 };

  private void readFromParcel(Parcel in) {
    _urlrepositoryHome = in.readString();
    _fileCopied = in.readParcelable(_fileCopied.getClass().getClassLoader());
    _fileMoved = in.readParcelable(_fileMoved.getClass().getClassLoader());
    repository = in.readString();
    currentFileMap = in.readBundle();
    childFilesMap = in.readBundle();
  }

  /*
   * (non-Javadoc)
   * @see android.os.Parcelable#describeContents()
   */
  @Override
  public int describeContents() {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
   */
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(_urlrepositoryHome);
    dest.writeParcelable(_fileCopied, flags);
    dest.writeParcelable(_fileMoved, flags);
    dest.writeString(repository);
    dest.writeBundle(currentFileMap);
    dest.writeBundle(childFilesMap);
  }

}
