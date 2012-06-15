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
