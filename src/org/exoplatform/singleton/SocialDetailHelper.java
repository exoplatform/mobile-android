package org.exoplatform.singleton;

import org.exoplatform.utils.image.SocialImageLoader;

import android.os.Parcel;
import android.os.Parcelable;

public class SocialDetailHelper implements Parcelable {
  private String                    activityId;

  private boolean                   liked;

  private String                    attachImageUrl;

  public SocialImageLoader          socialImageLoader;

  private static SocialDetailHelper detailHelper = new SocialDetailHelper();

  private SocialDetailHelper() {

  }

  public static SocialDetailHelper getInstance() {
    return detailHelper;
  }

  public void setActivityId(String id) {
    activityId = id;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setLiked(boolean is) {
    liked = is;
  }

  public boolean getLiked() {
    return liked;
  }

  public void setAttachedImageUrl(String url) {
    attachImageUrl = url;
  }

  public String getAttachedImageUrl() {
    return attachImageUrl;
  }

  private SocialDetailHelper(Parcel in) {
    readFromParcel(in);
  }

  public void readFromParcel(Parcel in) {
    activityId = in.readString();
    in.readBooleanArray(new boolean[] { liked });
    attachImageUrl = in.readString();
  }

  public static final Parcelable.Creator<SocialDetailHelper> CREATOR = new Parcelable.Creator<SocialDetailHelper>() {
                                                                       public SocialDetailHelper createFromParcel(Parcel in) {
                                                                         return new SocialDetailHelper(in);
                                                                       }

                                                                       public SocialDetailHelper[] newArray(int size) {
                                                                         return new SocialDetailHelper[size];
                                                                       }
                                                                     };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(activityId);
    dest.writeBooleanArray(new boolean[] { liked });
    dest.writeString(attachImageUrl);
  }

}
