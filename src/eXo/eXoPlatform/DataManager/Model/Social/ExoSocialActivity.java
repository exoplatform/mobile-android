package eXo.eXoPlatform.DataManager.Model.Social;

import java.util.Date;
import java.util.List;

import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.model.RestLike;

public class ExoSocialActivity {
  
  private String   activityId;

  private String            userID;

  private String            userName;

  private String            imageUrl;

  private String            title;

  private String            body;

  private Date              lastUpdateDate;

  private long              postedTime;

  private int               nbLikes;

  private int               nbComments;

  private String            postedTimeInWords;

  private List<RestComment> commentList;

  private List<RestLike>    likeList;

  private boolean           isShowMore;

  private boolean           isHeader;

  public ExoSocialActivity() {

  }
  
  public void setActivityId(String actId){
    activityId = actId;
  }

  public String getActivityId(){
    return activityId;
  }
  public void setUserId(String id) {
    userID = id;
  }

  public String getUserId() {
    return userID;
  }

  public void setUserName(String name) {
    userName = name;
  }

  public String getUserName() {
    return userName;
  }

  public void setImageUrl(String url) {
    imageUrl = url;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setTitle(String tit) {
    title = tit;
  }

  public String getTitle() {
    return title;
  }

  public void setPostedTime(long time) {
    postedTime = time;
  }

  public long getPostedTime() {
    return postedTime;
  }

  public void setCommentList(List<RestComment> list) {
    commentList = list;
  }

  public List<RestComment> getCommentList() {
    return commentList;
  }

  public void setLikelist(List<RestLike> list) {
    likeList = list;
  }

  public List<RestLike> getLikeList() {
    return likeList;
  }

  public void setCommentNumber(int number) {
    nbComments = number;
  }

  public int getCommentNumber() {
    return nbComments;
  }

  public void setLikeNumber(int n) {
    nbLikes = n;
  }

  public int getLikeNumber() {
    return nbLikes;
  }

  public void setIsShowMore(boolean showMore) {
    this.isShowMore = showMore;
  }

  public boolean getIsShowMore() {
    return isShowMore;
  }

  public void setIsHeader(boolean header) {
    this.isHeader = header;
  }

  public boolean getIsHeader() {
    return isHeader;
  }

}
