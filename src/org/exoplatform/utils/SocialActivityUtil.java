package org.exoplatform.utils;

import greendroid.util.Config;

import java.util.GregorianCalendar;
import java.util.LinkedList;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.social.client.api.model.RestActivityStream;
import org.exoplatform.widget.TextUrlSpan;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class SocialActivityUtil {

  public static String getCommentString(Context context, LinkedList<SocialLikeInfo> socialLikeList) {
    Resources resource = context.getResources();
    String nolike = resource.getString(R.string.NoLike);
    String likedThis = resource.getString(R.string.LikedThis);
    String and = resource.getString(R.string.And);
    String peoplesLiked = resource.getString(R.string.PeoplesLikedThis);
    String peopleLiked = resource.getString(R.string.PeopleLikedThis);
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

  public static String getPostedTimeString(Context context, long postedTime) {
    Resources resource = context.getResources();
    long time = (System.currentTimeMillis() - postedTime) / 1000;
    long value;
    String about = resource.getString(R.string.About);
    StringBuffer buffer = new StringBuffer();
    if (time < 60) {
      buffer.append(resource.getString(R.string.LessThanAMinute));
    } else {
      if (time < 120) {
        buffer.append(resource.getString(R.string.AboutAMinuteAgo));
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          buffer.append(about);
          buffer.append(" ");
          buffer.append(String.valueOf(value));
          buffer.append(" ");
          buffer.append(resource.getString(R.string.MinutesAgo));
        } else {
          if (time < 7200) {
            buffer.append(resource.getString(R.string.AboutAnHourAgo));
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              buffer.append(about);
              buffer.append(" ");
              buffer.append(String.valueOf(value));
              buffer.append(" ");
              buffer.append(resource.getString(R.string.HoursAgo));
            } else {
              if (time < 172800) {
                buffer.append(resource.getString(R.string.AboutADayAgo));
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  buffer.append(about);
                  buffer.append(" ");
                  buffer.append(String.valueOf(value));
                  buffer.append(" ");
                  buffer.append(resource.getString(R.string.DaysAgo));
                } else {
                  if (time < 5184000) {
                    buffer.append(resource.getString(R.string.AboutAMonthAgo));
                  } else {
                    value = Math.round(time / 2592000);
                    buffer.append(about);
                    buffer.append(" ");
                    buffer.append(String.valueOf(value));
                    buffer.append(" ");
                    buffer.append(resource.getString(R.string.MonthsAgo));
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

  public static String getHeader(Context context, long postedTime) {
    Resources resource = context.getResources();
    long currentTime = System.currentTimeMillis();
    GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
    calendar.set(GregorianCalendar.HOUR, 0);
    calendar.set(GregorianCalendar.MINUTE, 0);
    calendar.set(GregorianCalendar.SECOND, 0);

    long miliTime = calendar.getTimeInMillis();
    long time = (currentTime - postedTime) / 1000;
    String about = resource.getString(R.string.About);
    StringBuffer buffer = new StringBuffer();
    long value;
    if (postedTime > miliTime) {
      buffer.append(resource.getString(R.string.Today));
    } else {
      if (time < 172800) {
        buffer.append(resource.getString(R.string.AboutADayAgo));
      } else {
        if (time < 2592000) {
          value = Math.round(time / 86400);
          buffer.append(about);
          buffer.append(" ");
          buffer.append(String.valueOf(value));
          buffer.append(" ");
          buffer.append(resource.getString(R.string.DaysAgo));
        } else {
          if (time < 5184000) {
            buffer.append(resource.getString(R.string.AboutAMonthAgo));
          } else {
            value = Math.round(time / 2592000);
            buffer.append(about);
            buffer.append(" ");
            buffer.append(String.valueOf(value));
            buffer.append(" ");
            buffer.append(resource.getString(R.string.MonthsAgo));
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
      try {
        Spannable spannable = (Spannable) textView.getText();
        for (URLSpan span : list) {

          int start = spannable.getSpanStart(span);
          int stop = spannable.getSpanEnd(span);
          int flags = spannable.getSpanEnd(span);
          String spanUrl = span.getURL();
          spannable.removeSpan(span);
          TextUrlSpan myUrlSpan = null;
          if (spanUrl.startsWith(ExoConstants.HTTP_PROTOCOL)) {
            myUrlSpan = new TextUrlSpan(spanUrl);
          } else {
            String link = AccountSetting.getInstance().getDomainName() + spanUrl;
            myUrlSpan = new TextUrlSpan(link);
          }
          spannable.setSpan(myUrlSpan, start, stop, flags);
          textView.setText(spannable);
          textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
      } catch (Exception e) {
        if (Config.GD_ERROR_LOGS_ENABLED)
          Log.e("Exception", "LinkMovementMethod error!");
      }
    }
  }

  public static String getShortActivityContent(SocialActivityInfo info) {
    return null;
  }

  private static String appendFontColor(String fontColor) {
    StringBuffer buffer = new StringBuffer("<font style=\"font-style:normal\" color=\"");
    buffer.append(fontColor);
    buffer.append("\">");
    return buffer.toString();
  }

  private static String appendLinkStyleColor() {
    return "style=\"color: #FFFFFF";
  }

  private static String addSpaceInfo(SocialActivityInfo info,
                                     Resources resource,
                                     String fontColor,
                                     boolean isHomeStyle) {
    RestActivityStream actStream = info.restActivityStream;
    String spaceType = actStream.getType();
    StringBuffer spaceBuffer = new StringBuffer();
    if (spaceType.equalsIgnoreCase(ExoConstants.SOCIAL_SPACE)) {
      // spaceBuffer.append("<font style=\"font-style:normal\" color=\"#696969\">");
      spaceBuffer.append(appendFontColor(fontColor));
      spaceBuffer.append(resource.getString(R.string.In));
      spaceBuffer.append("</font>");
      spaceBuffer.append(" ");
      String nameSpace = actStream.getFullName();
      String spaceLink = actStream.getPermaLink();
      spaceBuffer.append("<a href=");
      spaceBuffer.append(spaceLink);
      if (isHomeStyle) {
        spaceBuffer.append(appendLinkStyleColor());
      }
      spaceBuffer.append(">");
      spaceBuffer.append(nameSpace);
      spaceBuffer.append("</a>");
      spaceBuffer.append(" ");
      // spaceBuffer.append("<font style=\"font-style:normal\" color=\"#696969\">");
      spaceBuffer.append(appendFontColor(fontColor));
      spaceBuffer.append(resource.getString(R.string.Space));
      spaceBuffer.append("</font>");
      return spaceBuffer.toString();
    } else
      return null;

  }

  public static String getActivityTypeForum(String userName,
                                            SocialActivityInfo activityInfo,
                                            Resources resource,
                                            String fontColor,
                                            boolean isHomeStyle) {
    String forumLink = null;
    StringBuffer forumBuffer = new StringBuffer();
    forumBuffer.append("<html><body>");
    forumBuffer.append(userName);
    forumBuffer.append(" ");
    String spaceInfo = addSpaceInfo(activityInfo, resource, fontColor, isHomeStyle);
    if (spaceInfo != null) {
      forumBuffer.append(spaceInfo);
    }
    forumBuffer.append(" ");
    String actType = activityInfo.templateParams.get("ActivityType");
    String actTypeDesc = null;
    String forumName = null;
    // forumBuffer.append("<font style=\"font-style:normal\" color=\"#696969\">");
    forumBuffer.append(appendFontColor(fontColor));
    if (actType.equalsIgnoreCase("AddPost")) {
      forumLink = activityInfo.templateParams.get("PostLink");
      actTypeDesc = resource.getString(R.string.HasAddANewPost);
      forumName = activityInfo.templateParams.get("PostName");
    } else if (actType.equalsIgnoreCase("UpdatePost")) {
      forumLink = activityInfo.templateParams.get("PostLink");
      actTypeDesc = resource.getString(R.string.HasUpdateANewPost);
      forumName = activityInfo.templateParams.get("PostName");
    } else if (actType.equalsIgnoreCase("AddTopic")) {
      forumLink = activityInfo.templateParams.get("TopicLink");
      actTypeDesc = resource.getString(R.string.HasPostedAnewTopic);
      forumName = activityInfo.templateParams.get("TopicName");
    } else if (actType.equalsIgnoreCase("UpdateTopic")) {
      forumLink = activityInfo.templateParams.get("TopicLink");
      actTypeDesc = resource.getString(R.string.HasUpdateAnewTopic);
      forumName = activityInfo.templateParams.get("TopicName");
    }
    forumBuffer.append(actTypeDesc);
    forumBuffer.append("</font>");
    forumBuffer.append("<br>");
    forumBuffer.append("<a href=");
    forumBuffer.append(forumLink);
    if (isHomeStyle) {
      forumBuffer.append(appendLinkStyleColor());
    }
    forumBuffer.append(">");
    forumBuffer.append(forumName);
    forumBuffer.append("</a>");
    forumBuffer.append("</body></html>");
    return forumBuffer.toString();
  }

  public static String getActivityTypeWiki(String userName,
                                           SocialActivityInfo activityInfo,
                                           Resources resource,
                                           String fontColor,
                                           boolean isHomeStyle) {
    String wiki_url = null;
    StringBuffer buffer = new StringBuffer();
    buffer.append("<html><body>");
    buffer.append("<a>");
    buffer.append(userName);
    buffer.append("</a> ");
    String spaceInfo = addSpaceInfo(activityInfo, resource, fontColor, isHomeStyle);
    if (spaceInfo != null) {
      buffer.append(spaceInfo);
    }
    buffer.append(" ");
    // buffer.append("<font color=\"#696969\">");
    buffer.append(appendFontColor(fontColor));
    String act_key = activityInfo.templateParams.get("act_key");
    String act_key_des = null;
    if (act_key != null) {
      if (act_key.equalsIgnoreCase("update_page")) {
        wiki_url = activityInfo.templateParams.get("view_change_url");
        act_key_des = resource.getString(R.string.HasEditWikiPage);
      } else if (act_key.equalsIgnoreCase("add_page")) {
        wiki_url = activityInfo.templateParams.get("page_url");
        act_key_des = resource.getString(R.string.HasCreatWikiPage);
      }
    }
    buffer.append(act_key_des);
    buffer.append("</font>");
    buffer.append("<br>");
    String page_name = activityInfo.templateParams.get("page_name");
    buffer.append("<a href=");
    buffer.append(wiki_url);
    if (isHomeStyle) {
      buffer.append(appendLinkStyleColor());
    }
    buffer.append(">");
    buffer.append(page_name);
    buffer.append("</a>");
    buffer.append("</body></html>");
    return buffer.toString();
  }

  public static String getActivityTypeAnswer(String userName,
                                             SocialActivityInfo activityInfo,
                                             Resources resource,
                                             String fontColor,
                                             boolean isHomeStyle) {
    String answer_link = null;
    StringBuffer answerBuffer = new StringBuffer();
    answerBuffer.append("<html><body>");
    answerBuffer.append("<a>");
    answerBuffer.append(userName);
    answerBuffer.append("</a> ");
    String spaceInfo = addSpaceInfo(activityInfo, resource, fontColor, isHomeStyle);
    if (spaceInfo != null) {
      answerBuffer.append(spaceInfo);
    }
    answerBuffer.append(" ");
    // answerBuffer.append("<font color=\"#696969\">");
    answerBuffer.append(appendFontColor(fontColor));
    String act_key = activityInfo.templateParams.get("ActivityType");
    String act_key_des = null;
    if (act_key.equalsIgnoreCase("QuestionUpdate")) {
      act_key_des = resource.getString(R.string.HasUpdatedQuestion);
    } else if (act_key.equalsIgnoreCase("QuestionAdd")) {
      act_key_des = resource.getString(R.string.HasAskAnswer);
    } else if (act_key.equalsIgnoreCase("AnswerAdd")) {
      act_key_des = resource.getString(R.string.HasAnswerQuestion);
    }
    answerBuffer.append(act_key_des);
    answerBuffer.append("</font>");
    answerBuffer.append("<br>");
    answer_link = activityInfo.templateParams.get("Link");
    String page_name = activityInfo.templateParams.get("Name");
    answerBuffer.append("<a href=");
    answerBuffer.append(answer_link);
    if (isHomeStyle) {
      answerBuffer.append(appendLinkStyleColor());
    }
    answerBuffer.append(">");
    answerBuffer.append(page_name);
    answerBuffer.append("</a>");
    answerBuffer.append("</body></html>");

    return answerBuffer.toString();
  }

  public static String getActivityTypeCalendar(String userName,
                                               SocialActivityInfo activityInfo,
                                               Resources resource,
                                               String fontColor,
                                               boolean isHomeStyle) {
    StringBuffer forumBuffer = new StringBuffer();
    forumBuffer.append("<html><body>");
    forumBuffer.append("<a>");
    forumBuffer.append(userName);
    forumBuffer.append("</a> ");
    String spaceInfo = addSpaceInfo(activityInfo, resource, fontColor, isHomeStyle);
    if (spaceInfo != null) {
      forumBuffer.append(spaceInfo);
    }
    forumBuffer.append(" ");
    String actType = activityInfo.templateParams.get("EventType");
    String actTypeDesc = null;
    String forumName = null;
    // forumBuffer.append("<font color=\"#696969\">");
    forumBuffer.append(appendFontColor(fontColor));
    if (actType.equalsIgnoreCase("EventAdded")) {
      actTypeDesc = resource.getString(R.string.AddedAnEvent);
    } else if (actType.equalsIgnoreCase("EventUpdated")) {
      actTypeDesc = resource.getString(R.string.UpdatedAnEvent);
    } else if (actType.equalsIgnoreCase("TaskAdded")) {
      actTypeDesc = resource.getString(R.string.AddedATask);
    } else if (actType.equalsIgnoreCase("TaskUpdated")) {
      actTypeDesc = resource.getString(R.string.UpdatedATask);
    }
    forumBuffer.append(actTypeDesc);
    forumBuffer.append("</font>");
    forumBuffer.append("<br>");
    forumBuffer.append("<a>");
    forumName = activityInfo.templateParams.get("EventSummary");
    if (isHomeStyle) {
      forumBuffer.append("<font color=\"#FFFFFF\">");
    } else
      forumBuffer.append("<font color=\"#000000\">");
    forumBuffer.append(forumName);
    forumBuffer.append("</font>");
    forumBuffer.append("</a>");
    forumBuffer.append("</body></html>");
    return forumBuffer.toString();
  }

  public static String getActivityTypeLink(String userName,
                                           SocialActivityInfo activityInfo,
                                           String fontColor,
                                           boolean isHomeStyle) {
    StringBuffer linkBuffer = new StringBuffer();
    String linkTitle = activityInfo.templateParams.get("title").trim();
    String linkUrl = activityInfo.templateParams.get("link");
    linkBuffer.append("<html><body>");

    linkBuffer.append("<a href=");
    linkBuffer.append(linkUrl);
    if (isHomeStyle) {
      linkBuffer.append(appendLinkStyleColor());
    }
    linkBuffer.append(">");
    linkBuffer.append(linkTitle);
    linkBuffer.append("</a>");
    linkBuffer.append("</body></html>");
    return linkBuffer.toString();
  }

  public static String getActivityTypeDocument(String userName,
                                               SocialActivityInfo activityInfo,
                                               Resources resource,
                                               String fontColor,boolean isHomeStyle) {
    String space = addSpaceInfo(activityInfo, resource, fontColor, isHomeStyle);

    if (space != null) {
      StringBuffer docBuffer = new StringBuffer();
      docBuffer.append("<html><body>");
      docBuffer.append(userName);
      docBuffer.append(" ");
      docBuffer.append(space);
      return docBuffer.toString();
    } else
      return null;

  }

  public static void setCaledarContent(TextView textView,
                                       SocialActivityInfo activityInfo,
                                       Resources resource) {
    StringBuffer caledarBuffer = new StringBuffer();
    caledarBuffer.append("<html><body>");
    caledarBuffer.append(resource.getString(R.string.CalendarDescription));
    caledarBuffer.append("\n");
    String description = activityInfo.templateParams.get("EventDescription");
    if (description != null) {
      caledarBuffer.append(description);
    }
    caledarBuffer.append("<br>");
    caledarBuffer.append(resource.getString(R.string.CalendarLocation));
    caledarBuffer.append(" ");
    String location = activityInfo.templateParams.get("EventLocale");
    if (location != null) {
      caledarBuffer.append(location);
    }
    caledarBuffer.append("<br>");
    caledarBuffer.append(resource.getString(R.string.CalendarStart));
    caledarBuffer.append(" ");
    String startTime = activityInfo.templateParams.get("EventStartTime");
    startTime = PhotoUtils.getDateFromString(startTime);
    caledarBuffer.append(startTime);
    caledarBuffer.append("<br>");
    caledarBuffer.append(resource.getString(R.string.CalendarEnd));
    caledarBuffer.append(" ");
    String endTime = activityInfo.templateParams.get("EventEndTime");
    endTime = PhotoUtils.getDateFromString(endTime);
    caledarBuffer.append(endTime);
    caledarBuffer.append("</body></html>");
    textView.setText(Html.fromHtml(caledarBuffer.toString()));
  }

}
