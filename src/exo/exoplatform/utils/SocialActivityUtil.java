package exo.exoplatform.utils;

import java.util.ArrayList;

import exo.exoplatform.social.entity.ExoSocialLike;

public class SocialActivityUtil {

  public static String getCommentString(ArrayList<ExoSocialLike> socialLikeList) {
    StringBuffer buffer = new StringBuffer();
    int count = socialLikeList.size();
    ExoSocialLike socialLike = null;

    if (count == 0) {
      buffer.append("No like for the moment");
    } else if (count == 1) {
      socialLike = socialLikeList.get(0);
      buffer.append(socialLike.getLikeName());

      buffer.append(" liked this");
    } else if (count < 4) {
      for (int i = 0; i < count - 1; i++) {
        socialLike = socialLikeList.get(i);
        buffer.append(socialLike.getLikeName());
        buffer.append(", ");
      }
      buffer.deleteCharAt(buffer.length() - 2);
      socialLike = socialLikeList.get(count - 1);
      buffer.append("and ");
      buffer.append(socialLike.getLikeName());
      buffer.append(" liked this");
    } else {
      for (int i = 0; i < 3; i++) {
        socialLike = socialLikeList.get(i);
        buffer.append(socialLike.getLikeName());
        buffer.append(", ");
      }
      buffer.deleteCharAt(buffer.length() - 2);
      int remain = count - 3;
      buffer.append("and ");
      buffer.append(remain);
      if (remain > 1) {
        buffer.append(" peoples liked this");
      } else
        buffer.append(" people liked this");

    }

    return buffer.toString();
  }

  public static String getPostedTimeString(long postedTime) {
    long time = (System.currentTimeMillis() - postedTime) / 1000;
    long value;
    if (time < 60) {
      return "less than a minute";
    } else {
      if (time < 120) {
        return "about a minute ago";
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          return "about " + String.valueOf(value) + " minutes";
        } else {
          if (time < 7200) {
            return "about an hour ago";
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              return "about " + String.valueOf(value) + " hours ago";
            } else {
              if (time < 172800) {
                return "about a day ago";
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  return "about " + String.valueOf(value) + " days ago";
                } else {
                  if (time < 5184000) {
                    return "about a month ago";
                  } else {
                    value = Math.round(time / 2592000);
                    return "about " + String.valueOf(value) + " months ago";
                  }
                }
              }
            }
          }
        }
      }
    }
  }

}
