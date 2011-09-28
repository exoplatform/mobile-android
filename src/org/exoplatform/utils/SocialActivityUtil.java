package org.exoplatform.utils;

import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.ResourceBundle;

import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;

public class SocialActivityUtil {

  public static String getCommentString(LinkedList<SocialLikeInfo> socialLikeList) {
    LocalizationHelper location = LocalizationHelper.getInstance();
    String nolike = location.getString("NoLike");
    String likedThis = location.getString("LikedThis");
    String and = location.getString("And");
    String peoplesLiked = location.getString("PeoplesLikedThis");
    String peopleLiked = location.getString("PeopleLikedThis");
    StringBuffer buffer = new StringBuffer();
    int count = socialLikeList.size();
    SocialLikeInfo socialLike = null;

    if (count == 0) {
      buffer.append(nolike);
    } else if (count == 1) {
      socialLike = socialLikeList.get(0);
      buffer.append(socialLike.getLikeName());
      buffer.append(" ");
      buffer.append(likedThis);
    } else if (count < 4 || count == 4) {
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
      for (int i = 0; i < 4; i++) {
        socialLike = socialLikeList.get(i);
        buffer.append(socialLike.getLikeName());
        buffer.append(", ");
      }
      buffer.deleteCharAt(buffer.length() - 2);
      int remain = count - 4;
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

  public static String getPostedTimeString(long postedTime) {
    LocalizationHelper location = LocalizationHelper.getInstance();

    long time = (System.currentTimeMillis() - postedTime) / 1000;
    long value;
    String about = location.getString("About");
    StringBuffer buffer = new StringBuffer();
    if (time < 60) {
      buffer.append(location.getString("LessThanAMinute"));
    } else {
      if (time < 120) {
        buffer.append(location.getString("AboutAMinuteAgo"));
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          buffer.append(about);
          buffer.append(" ");
          buffer.append(String.valueOf(value));
          buffer.append(" ");
          buffer.append(location.getString("MinutesAgo"));
        } else {
          if (time < 7200) {
            buffer.append(location.getString("AboutAnHourAgo"));
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              buffer.append(about);
              buffer.append(" ");
              buffer.append(String.valueOf(value));
              buffer.append(" ");
              buffer.append(location.getString("HoursAgo"));
            } else {
              if (time < 172800) {
                buffer.append(location.getString("AboutADayAgo"));
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  buffer.append(about);
                  buffer.append(" ");
                  buffer.append(String.valueOf(value));
                  buffer.append(" ");
                  buffer.append(location.getString("DaysAgo"));
                } else {
                  if (time < 5184000) {
                    buffer.append(location.getString("AboutAMonthAgo"));
                  } else {
                    value = Math.round(time / 2592000);
                    buffer.append(about);
                    buffer.append(" ");
                    buffer.append(String.valueOf(value));
                    buffer.append(" ");
                    buffer.append(location.getString("MonthsAgo"));
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

  public static String getHeader(long postedTime) {
    LocalizationHelper location = LocalizationHelper.getInstance();
    long currentTime = System.currentTimeMillis();
    GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
    calendar.set(GregorianCalendar.HOUR, 0);
    calendar.set(GregorianCalendar.MINUTE, 0);
    calendar.set(GregorianCalendar.SECOND, 0);

    long miliTime = calendar.getTimeInMillis();
    long time = (currentTime - postedTime) / 1000;
    String about = location.getString("About");
    StringBuffer buffer = new StringBuffer();
    long value;
    if (postedTime > miliTime) {
      buffer.append(location.getString("Today"));
    } else {
      if (time < 172800) {
        buffer.append(location.getString("AboutADayAgo"));
      } else {
        if (time < 2592000) {
          value = Math.round(time / 86400);
          buffer.append(about);
          buffer.append(" ");
          buffer.append(String.valueOf(value));
          buffer.append(" ");
          buffer.append(location.getString("DaysAgo"));
        } else {
          if (time < 5184000) {
            buffer.append(location.getString("AboutAMonthAgo"));
          } else {
            value = Math.round(time / 2592000);
            buffer.append(about);
            buffer.append(" ");
            buffer.append(String.valueOf(value));
            buffer.append(" ");
            buffer.append(location.getString("MonthsAgo"));
          }
        }
      }
    }

    return buffer.toString();
  }

  public static String getDomain() {
    String domain = AccountSetting.getInstance().getDomainName();
    if (domain.endsWith("/")) {
      domain = domain.substring(0, domain.length() - 1);
    }
    return domain;
  }

  public static String getUrl() {
    StringBuffer urlBuffer = new StringBuffer();
    urlBuffer.append(ExoConstants.HTTP_PROTOCOL);
    urlBuffer.append("://");
    urlBuffer.append(ExoConstants.ACTIVITY_HOST);
    urlBuffer.append(":");
    urlBuffer.append(ExoConstants.ACTIVITY_PORT);
    return urlBuffer.toString();
  }
}
