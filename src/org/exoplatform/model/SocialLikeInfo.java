package org.exoplatform.model;

public class SocialLikeInfo {

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

}
