package org.exoplatform.mocks;

import java.util.Date;

public class SocialActivityMock {

  public String userID;

  public String title;

  public String body;

  public Date   lastUpdateDate;

  public long   postedTime;

  public int    nbLikes;

  public int    nbComments; 

  public SocialActivityMock(String userID,
                        String title,
                        String body,
                        long postedTime,
                        int nbLikes,
                        int nbComments) {
    super();
    this.userID = userID;
    this.title = title;
    this.body = body;
    this.postedTime = postedTime;
    this.nbLikes = nbLikes;
    this.nbComments = nbComments;
  }
}
