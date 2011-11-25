package org.exoplatform.utils;

import java.util.GregorianCalendar;
import java.util.LinkedList;

import org.exoplatform.R;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.widget.TextUrlSpan;

import android.content.Context;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.widget.ImageView;
import android.widget.TextView;

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

  public static int getActivityTypeId(String type) {
    if (type != null) {
      if (type.contains("ks-forum:spaces")) {
        return 1;
      } else if (type.contains("ks-wiki:spaces")) {
        return 2;
      } else if (type.contains("exosocial:spaces")) {
        return 3;
      } else if (type.contains("DOC_ACTIVITY")) {
        return 4;
      } else if (type.contains("DEFAULT_ACTIVITY")) {
        return 5;
      } else if (type.contains("LINK_ACTIVITY")) {
        return 6;
      } else if (type.contains("exosocial:relationship")) {
        return 7;
      } else if (type.contains("exosocial:people")) {
        return 8;
      } else if (type.contains("contents:spaces")) {
        return 9;
      } else if (type.contains("ks-answer")) {
        return 10;
      } else if (type.contains("cs-calendar:spaces")) {
        return 11;
      } else
        return 0;
    } else
      return 0;
  }

  public static void setImageType(int type, ImageView imageView) {
    int returnType = 0;
    switch (type) {
    case 0:
      returnType = R.drawable.activity_type_normal;
      break;
    case 1:
      returnType = R.drawable.activity_type_forum;
      break;
    case 2:
      returnType = R.drawable.activity_type_wiki;
      break;
    case 3:
      returnType = R.drawable.activity_type_normal;
      break;
    case 4:
      returnType = R.drawable.activity_type_document;
      break;
    case 5:
      returnType = R.drawable.activity_type_normal;
      break;
    case 6:
      returnType = R.drawable.activity_type_link;
      break;
    case 7:
      returnType = R.drawable.activity_type_connection;
      break;
    case 8:
      returnType = R.drawable.activity_type_normal;
      break;
    case 9:
      returnType = R.drawable.activity_type_normal;
      break;
    case 10:
      returnType = R.drawable.activity_type_answer;
      break;
    case 11:
      returnType = R.drawable.activity_type_calendar;

    }
    imageView.setImageResource(returnType);
  }

  public static void setTextLinkfy(TextView textView) {
    URLSpan[] list = textView.getUrls();
    if (list != null) {
      Spannable spannable = (Spannable) textView.getText();
      for (URLSpan span : list) {
        try {
          int start = spannable.getSpanStart(span);
          int stop = spannable.getSpanEnd(span);
          int flags = spannable.getSpanEnd(span);
          String spanUrl = span.getURL();
          spannable.removeSpan(span);
          URLSpan myUrlSpan = null;
          if (spanUrl.startsWith(ExoConstants.HTTP_PROTOCOL)) {
            myUrlSpan = new URLSpan(spanUrl);
          } else {
            String link = AccountSetting.getInstance().getDomainName() + spanUrl;
            myUrlSpan = new URLSpan(link);
          }
          spannable.setSpan(myUrlSpan, start, stop, flags);
          textView.setText(spannable);
          textView.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (Exception e) {
        }
      }
    }
  }

}
