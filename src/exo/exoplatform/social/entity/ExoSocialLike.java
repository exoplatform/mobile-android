package org.exoplatform.social.entity;

public class ExoSocialLike {

  private String likeId;

  private String likeName;

  public ExoSocialLike() {

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
