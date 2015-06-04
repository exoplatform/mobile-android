/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.image.ExoPicasso;
import org.exoplatform.utils.image.RoundedCornersTranformer;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Represents sliding Item on Home screen. Created by The eXo Platform SAS
 * 
 * @author eXoPlatform exo@exoplatform.com May 22, 2012
 */
public class SocialHomeTickerItem extends LinearLayout {

  private static final String FONT_COLOR     = "#FFFFFF";

  /**
   * We are on the Home screen
   */
  private final boolean       IS_HOME_STREAM = true;

  private Context             mContext;

  private TextView            textViewName;

  private TextView            textViewMessage;

  private ImageView           activityAvatar;

  private String              userName;

  private Resources           resource;

  private SocialActivityInfo  activityInfo;

  private static final String TAG            = "eXo____HomeSocialItem____";

  public SocialHomeTickerItem(Context context) {
    super(context);
    mContext = context;
  }

  public SocialHomeTickerItem(Context context, SocialActivityInfo info) {
    super(context);
    mContext = context;
    userName = info.getUserName();
    resource = context.getResources();
    activityInfo = info;
    LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflate.inflate(R.layout.home_social_item_layout, this);
    activityAvatar = (ImageView) view.findViewById(R.id.home_activity_avatar);
    textViewName = (TextView) view.findViewById(R.id.home_activity_name_txt);
    textViewMessage = (TextView) view.findViewById(R.id.home_activity_message_txt);

    ExoPicasso.picasso(mContext)
              .load(Uri.parse(info.getImageUrl()))
              .transform(new RoundedCornersTranformer(mContext))
              .placeholder(R.drawable.default_avatar)
              .error(R.drawable.default_avatar)
              .fit()
              .into(activityAvatar);

    textViewName.setText(Html.fromHtml(userName));
    textViewMessage.setText(Html.fromHtml(activityInfo.getTitle()));
    int imageId = SocialActivityUtil.getActivityTypeId(info.getType());
    setViewByType(imageId);
  }

  private void setViewByType(int typeId) {
    String spaceInfo = null;
    switch (typeId) {
    case SocialActivityUtil.ACTIVITY_TYPE_FORUM:

      String forumBuffer = SocialActivityUtil.getActivityTypeForum(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);

      textViewName.setText(Html.fromHtml(forumBuffer));
      String forumBody = activityInfo.getBody();

      textViewMessage.setText(Html.fromHtml(forumBody));
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_WIKI:
      String wikiBuffer = SocialActivityUtil.getActivityTypeWiki(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);
      textViewName.setText(Html.fromHtml(wikiBuffer));
      String wikiBody = activityInfo.getBody();
      if (wikiBody == null || wikiBody.equalsIgnoreCase("body")) {
        textViewMessage.setVisibility(View.GONE);
      } else {
        textViewMessage.setText(Html.fromHtml(wikiBody));
      }
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_SPACE:
      /* add space information */
      spaceInfo = SocialActivityUtil.getHeaderWithSpaceInfo(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);
      if (spaceInfo != null)
        textViewName.setText(Html.fromHtml(spaceInfo), TextView.BufferType.SPANNABLE);
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_DOC:
      /* add space information */
      spaceInfo = SocialActivityUtil.getHeaderWithSpaceInfo(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);
      if (spaceInfo != null)
        textViewName.setText(Html.fromHtml(spaceInfo), TextView.BufferType.SPANNABLE);
      /* add document info */
      String tempMessage = activityInfo.templateParams.get("MESSAGE");
      if (tempMessage != null) {
        textViewMessage.setText(tempMessage.trim());
      }
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_NORMAL:
      /* add space information */
      spaceInfo = SocialActivityUtil.getHeaderWithSpaceInfo(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);
      if (spaceInfo != null)
        textViewName.setText(Html.fromHtml(spaceInfo), TextView.BufferType.SPANNABLE);
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_LINK:
      String templateComment = activityInfo.templateParams.get("comment");
      String description = activityInfo.templateParams.get("description");

      if (templateComment != null && !templateComment.equalsIgnoreCase("")) {
        textViewMessage.setText(Html.fromHtml(templateComment));
      }
      if (description != null) {
        textViewMessage.setText(Html.fromHtml(description.trim()));
      }

      break;
    case SocialActivityUtil.ACTIVITY_TYPE_RELATIONSHIP:
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_PEOPLE:
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_CONTENT:
      /* add space information */
      spaceInfo = SocialActivityUtil.getHeaderWithSpaceInfo(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);
      if (spaceInfo != null)
        textViewName.setText(Html.fromHtml(spaceInfo), TextView.BufferType.SPANNABLE);
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_ANSWER:
      String answerBuffer = SocialActivityUtil.getActivityTypeAnswer(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);

      textViewName.setText(Html.fromHtml(answerBuffer));

      String answerBody = activityInfo.getBody();
      if (answerBody != null) {
        textViewMessage.setText(Html.fromHtml(answerBody));
      }
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_CALENDAR:
      String calendarBuffer = SocialActivityUtil.getActivityTypeCalendar(userName,
                                                                         activityInfo,
                                                                         resource,
                                                                         FONT_COLOR,
                                                                         IS_HOME_STREAM);

      textViewName.setText(Html.fromHtml(calendarBuffer));
      SocialActivityUtil.setCalendarContent(textViewMessage, activityInfo, resource);
      break;
    default:
      break;
    }

  }
}
