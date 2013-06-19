/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.utils.SocialActivityUtil;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com May
 * 22, 2012
 */
public class HomeSocialItem extends LinearLayout {

  private static final String FONT_COLOR = "#FFFFFF";

  private TextView            textViewName;

  private TextView            textViewMessage;

  private ShaderImageView     activtyAvatar;

  private String              userName;

  private Resources           resource;

  private SocialActivityInfo  activityInfo;

  public HomeSocialItem(Context context) {
    super(context);
  }
  
  public HomeSocialItem(Context context, SocialActivityInfo info) {
    super(context);
    userName = info.getUserName();
    resource = context.getResources();
    activityInfo = info;
    LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflate.inflate(R.layout.home_social_item_layout, this);
    activtyAvatar = (ShaderImageView) view.findViewById(R.id.home_activity_avatar);
    activtyAvatar.setDefaultImageResource(R.drawable.default_avatar);
    textViewName = (TextView) view.findViewById(R.id.home_activity_name_txt);
    textViewMessage = (TextView) view.findViewById(R.id.home_activity_message_txt);
    activtyAvatar.setUrl(info.getImageUrl());

    textViewName.setText(Html.fromHtml(userName));
    textViewMessage.setText(Html.fromHtml(activityInfo.getTitle()));
    int imageId = SocialActivityUtil.getActivityTypeId(info.getType());
    setViewByType(imageId);

  }

  private void setViewByType(int typeId) {
    switch (typeId) {
    case SocialActivityUtil.KS_FORUM_SPACE:

      String forumBuffer = SocialActivityUtil.getActivityTypeForum(userName,
                                                                   activityInfo,
                                                                   resource,
                                                                   FONT_COLOR,
                                                                   true);

      textViewName.setText(Html.fromHtml(forumBuffer));
      String forumBody = activityInfo.getBody();

      textViewMessage.setText(Html.fromHtml(forumBody));
      break;
    case SocialActivityUtil.KS_WIKI_SPACE:
      String wikiBuffer = SocialActivityUtil.getActivityTypeWiki(userName,
                                                                 activityInfo,
                                                                 resource,
                                                                 FONT_COLOR,
                                                                 true);
      textViewName.setText(Html.fromHtml(wikiBuffer));
      String wikiBody = activityInfo.getBody();
      if (wikiBody == null || wikiBody.equalsIgnoreCase("body")) {
        textViewMessage.setVisibility(View.GONE);
      } else {
        textViewMessage.setText(Html.fromHtml(wikiBody));
      }
      break;
    case SocialActivityUtil.EXO_SOCIAL_SPACE:
      break;
    case SocialActivityUtil.DOC_ACTIVITY:
      /*
       * add space information
       */

      String docBuffer = SocialActivityUtil.getActivityTypeDocument(userName,
                                                                    activityInfo,
                                                                    resource,
                                                                    FONT_COLOR,
                                                                    true);
      if (docBuffer != null) {
        textViewName.setText(Html.fromHtml(docBuffer));
      }

      String tempMessage = activityInfo.templateParams.get("MESSAGE");
      if (tempMessage != null) {
        textViewMessage.setText(tempMessage.trim());
      }

      break;
    case SocialActivityUtil.DEFAULT_ACTIVITY:
      break;
    case SocialActivityUtil.LINK_ACTIVITY:
      String templateComment = activityInfo.templateParams.get("comment");
      String description = activityInfo.templateParams.get("description").trim();

      if (templateComment != null && !templateComment.equalsIgnoreCase("")) {
        textViewMessage.setText(Html.fromHtml(templateComment));
      }
      if (description != null) {
        textViewMessage.setText(Html.fromHtml(description));
      }

      break;
    case SocialActivityUtil.EXO_SOCIAL_RELATIONSHIP:

      break;
    case SocialActivityUtil.EXO_SOCIAL_PEOPLE:
      break;
    case SocialActivityUtil.CONTENT_SPACE:
      /*
       * add space information
       */

      String spaceBuffer = SocialActivityUtil.getActivityTypeDocument(userName,
                                                                      activityInfo,
                                                                      resource,
                                                                      FONT_COLOR,
                                                                      true);
      if (spaceBuffer != null) {
        textViewName.setText(Html.fromHtml(spaceBuffer));
      }

      break;
    case SocialActivityUtil.KS_ANSWER:
      String answerBuffer = SocialActivityUtil.getActivityTypeAnswer(userName,
                                                                     activityInfo,
                                                                     resource,
                                                                     FONT_COLOR,
                                                                     true);

      textViewName.setText(Html.fromHtml(answerBuffer));

      String answerBody = activityInfo.getBody();
      if (answerBody != null) {
        textViewMessage.setText(Html.fromHtml(answerBody));
      }
      break;

    case SocialActivityUtil.CS_CALENDAR_SPACES:
      String calendarBuffer = SocialActivityUtil.getActivityTypeCalendar(userName,
                                                                         activityInfo,
                                                                         resource,
                                                                         FONT_COLOR,
                                                                         true);

      textViewName.setText(Html.fromHtml(calendarBuffer));
      SocialActivityUtil.setCaledarContent(textViewMessage, activityInfo, resource);
      break;
    default:
      break;
    }

  }
}
