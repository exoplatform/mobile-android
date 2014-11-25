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

public class SocialLikeInfo implements Parcelable {
  /*
   * Class for liker information, implements Parcelable object to make it can
   * transport between activities by put/get intent extra
   */

  private String likeId;

  private String likeName;

  public String  likedImageUrl = null;

  public SocialLikeInfo() {

  }

  public void setLikeID(String id) {
    likeId = id;
  }

  public String getLikeId() {
    return likeId;
  }

  public void setLikeName(String name) {
    likeName = name;
  }

  public String getLikeName() {
    return likeName;
  }

  private SocialLikeInfo(Parcel in) {
    readFromParcel(in);
  }

  public static final Parcelable.Creator<SocialLikeInfo> CREATOR = new Parcelable.Creator<SocialLikeInfo>() {
                                                                   public SocialLikeInfo createFromParcel(Parcel in) {
                                                                     return new SocialLikeInfo(in);
                                                                   }

                                                                   public SocialLikeInfo[] newArray(int size) {
                                                                     return new SocialLikeInfo[size];
                                                                   }
                                                                 };

  private void readFromParcel(Parcel in) {
    likeId = in.readString();
    likeName = in.readString();
    likedImageUrl = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(likeId);
    dest.writeString(likeName);
    dest.writeString(likedImageUrl);
  }

}
