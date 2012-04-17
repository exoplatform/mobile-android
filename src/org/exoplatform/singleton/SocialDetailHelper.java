package org.exoplatform.singleton;

import org.exoplatform.utils.image.SocialImageLoader;

public class SocialDetailHelper {
  private String                    activityId;

  private boolean                   liked;

  private String                    attachImageUrl;

  public SocialImageLoader          socialImageloader;

  public boolean                    taskIsFinish = false;

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

}
