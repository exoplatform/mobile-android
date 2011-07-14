package org.exoplatform.utils;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.exoplatform.social.entity.ExoSocialLike;

public class SocialActivityUtil {

  public static String getCommentString(ArrayList<ExoSocialLike> socialLikeList,
                                        ResourceBundle resourceBundle) {
    String nolike = resourceBundle.getString("NoLike");
    String likedThis = resourceBundle.getString("LikedThis");
    String and = resourceBundle.getString("And");
    String peoplesLiked = resourceBundle.getString("PeoplesLikedThis");
    String peopleLiked = resourceBundle.getString("PeopleLikedThis");
    StringBuffer buffer = new StringBuffer();
    int count = socialLikeList.size();
    ExoSocialLike socialLike = null;

    if (count == 0) {
      buffer.append(nolike);
    } else if (count == 1) {
      socialLike = socialLikeList.get(0);
      buffer.append(socialLike.getLikeName());
      buffer.append(" ");
      buffer.append(likedThis);
    } else if (count < 4) {
      for (int i = 0; i < count - 1; i++) {
        socialLike = socialLikeList.get(i);
        buffer.append(socialLike.getLikeName());
        buffer.append(", ");
      }
      buffer.deleteCharAt(buffer.length() - 2);
      socialLike = socialLikeList.get(count - 1);
      buffer.append(and);
      buffer.append(" ");
      buffer.append(socialLike.getLikeName());
      buffer.append(" ");
      buffer.append(likedThis);
    } else {
      for (int i = 0; i < 3; i++) {
        socialLike = socialLikeList.get(i);
        buffer.append(socialLike.getLikeName());
        buffer.append(", ");
      }
      buffer.deleteCharAt(buffer.length() - 2);
      int remain = count - 3;
      buffer.append(and);
      buffer.append(" ");
      buffer.append(remain);
      buffer.append(" ");
      if (remain > 1) {
        buffer.append(peoplesLiked);
      } else
        buffer.append(peopleLiked);
    }

    return buffer.toString();
  }

  public static String getPostedTimeString(long postedTime, ResourceBundle resourceBundle) {

    long time = (System.currentTimeMillis() - postedTime) / 1000;
    long value;
    String about = resourceBundle.getString("About");
    StringBuffer buffer = new StringBuffer();
    if (time < 60) {
      buffer.append(resourceBundle.getString("LessThanAMinute"));
    } else {
      if (time < 120) {
        buffer.append(resourceBundle.getString("AboutAMinuteAgo"));
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          buffer.append(about);
          buffer.append(" ");
          buffer.append(String.valueOf(value));
          buffer.append(" ");
          buffer.append(resourceBundle.getString("MinutesAgo"));
          // return "about " + String.valueOf(value) + " minutes ago";
        } else {
          if (time < 7200) {
            buffer.append(resourceBundle.getString("AboutAnHourAgo"));
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              buffer.append(about);
              buffer.append(" ");
              buffer.append(String.valueOf(value));
              buffer.append(" ");
              buffer.append(resourceBundle.getString("HoursAgo"));
            } else {
              if (time < 172800) {
                buffer.append(resourceBundle.getString("AboutADayAgo"));
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  buffer.append(about);
                  buffer.append(" ");
                  buffer.append(String.valueOf(value));
                  buffer.append(" ");
                  buffer.append(resourceBundle.getString("DaysAgo"));
                } else {
                  if (time < 5184000) {
                    buffer.append(resourceBundle.getString("AboutAMonthAgo"));
                  } else {
                    value = Math.round(time / 2592000);
                    buffer.append(about);
                    buffer.append(" ");
                    buffer.append(String.valueOf(value));
                    buffer.append(" ");
                    buffer.append(resourceBundle.getString("MonthsAgo"));
                  }
                }
              }
            }
          }
        }
      }
    }
    return buffer.toString();
  }

}
