package org.exoplatform.model;

public class SocialCommentInfo {

  private String commentImageUrl;

  private String commentId;

  private String commentName;

  private String commentTitle;

  private long   postedTime;

  public SocialCommentInfo() {

  }

  public void setImageUrl(String url) {
    commentImageUrl = url;
  }

  public String getImageUrl() {
    return commentImageUrl;
  }

  public void setCommentId(String id) {
    commentId = id;
  }

  public String getCommentId() {
    return commentId;
  }

  public void setCommentName(String name) {
    commentName = name;
  }

  public String getCommentName() {
    return commentName;
  }

  public void setCommentTitle(String title) {
    commentTitle = title;
  }

  public String getCommentTitle() {
    return commentTitle;
  }

  public void setPostedTime(long time) {
    postedTime = time;
  }

  public long getPostedTime() {
    return postedTime;
  }

}
