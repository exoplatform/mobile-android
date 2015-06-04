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
package org.exoplatform.ui.social;

import java.net.URLConnection;
import java.util.Locale;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.image.EmptyImageGetter;
import org.exoplatform.utils.image.ExoPicasso;
import org.exoplatform.utils.image.RoundedCornersTranformer;
import org.exoplatform.widget.StandardArrayAdapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Represents activity item on activity stream
 */
public class SocialActivityStreamItem {
  private static final String FONT_COLOR     = "#696969";

  /**
   * We are not on the Home screen
   */
  private final boolean       IS_HOME_STREAM = false;

  public LinearLayout         contentLayoutWrap;

  private ImageView           imageViewAvatar;

  public TextView             textViewName;

  public TextView             textViewMessage;

  public TextView             textViewTempMessage;

  private TextView            textViewCommnet;

  private Button              buttonComment;

  private Button              buttonLike;

  private ImageView           typeImageView;

  private TextView            textViewTime;

  private View                attachStubView;

  private String              domain;

  private String              userName;

  private Context             mContext;

  private SocialActivityInfo  activityInfo;

  private boolean             isDetail;

  private Resources           resource;

  private static final String TAG            = "eXo____SocialItem____";

  public SocialActivityStreamItem(Context context, StandardArrayAdapter.ViewHolder holder, SocialActivityInfo info, boolean is) {
    mContext = context;
    resource = mContext.getResources();
    activityInfo = info;
    isDetail = is;
    domain = SocialActivityUtil.getDomain();
    imageViewAvatar = holder.imageViewAvatar;
    contentLayoutWrap = holder.contentLayoutWrap;
    textViewName = holder.textViewName;
    textViewName.setLinkTextColor(Color.rgb(21, 94, 173));
    textViewMessage = holder.textViewMessage;
    textViewTempMessage = holder.textViewTempMessage;
    textViewCommnet = holder.textViewCommnet;
    buttonComment = holder.buttonComment;
    buttonLike = holder.buttonLike;
    typeImageView = holder.typeImageView;
    textViewTime = holder.textViewTime;
    attachStubView = holder.attachStubView;
  }

  public void initCommonInfo() {

    String avatarUrl = activityInfo.getImageUrl();
    if (avatarUrl != null) {
      ExoPicasso.picasso(mContext)
                .load(Uri.parse(avatarUrl))
                .transform(new RoundedCornersTranformer(mContext))
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .fit()
                .into(imageViewAvatar);
    }

    userName = activityInfo.getUserName();
    textViewName.setText(Html.fromHtml(userName));
    textViewMessage.setText(Html.fromHtml(activityInfo.getTitle(), new EmptyImageGetter(mContext), null),
                            TextView.BufferType.SPANNABLE);
    textViewMessage.setVisibility(View.VISIBLE);
    textViewTime.setText(SocialActivityUtil.getPostedTimeString(mContext,
                                                                activityInfo.getUpdatedTime() != 0 ? activityInfo.getUpdatedTime()
                                                                                                  : activityInfo.getPostedTime()));
    buttonComment.setText("" + activityInfo.getCommentNumber());
    buttonLike.setText("" + activityInfo.getLikeNumber());
    textViewTempMessage.setVisibility(View.GONE);
    textViewCommnet.setVisibility(View.GONE);
    attachStubView.setVisibility(View.GONE);

    int imageId = SocialActivityUtil.getActivityTypeId(activityInfo.getType());
    SocialActivityUtil.setImageType(imageId, typeImageView);
    setViewByType(imageId);
    setDetailView();
  }

  private void setDetailView() {
    if (isDetail) {
      contentLayoutWrap.setBackgroundDrawable(null);
      contentLayoutWrap.setPadding(5, -2, 5, 5);
      buttonComment.setVisibility(View.GONE);
      buttonLike.setVisibility(View.GONE);

      textViewName.setMaxLines(100);
      textViewMessage.setMaxLines(100);
      textViewTempMessage.setMaxLines(100);
      textViewCommnet.setMaxLines(100);
    }
  }

  private void setViewByType(int typeId) {
    String spaceInfo = null;
    switch (typeId) {

    case SocialActivityUtil.ACTIVITY_TYPE_FORUM:
      setActivityTypeForum();
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_WIKI:
      setActivityTypeWiki();
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
      if (tempMessage != null)
        textViewMessage.setText(tempMessage.trim());

      String docLink = activityInfo.templateParams.get("DOCLINK");
      if (docLink != null) {
        String docName = activityInfo.templateParams.get("DOCNAME");
        String url = domain + docLink;
        /** get mimetype from url */
        String mimeTypeExtension = URLConnection.guessContentTypeFromName(url);
        displayAttachImage(url, docName, null, mimeTypeExtension, false);
      }
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_NORMAL:
      /* add space information */
      spaceInfo = SocialActivityUtil.getHeaderWithSpaceInfo(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);
      if (spaceInfo != null) {
        textViewName.setText(Html.fromHtml(spaceInfo), TextView.BufferType.SPANNABLE);
      }
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_LINK:
      setActivityTypeLink();
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
      /* add content info */
      String contentLink = activityInfo.templateParams.get("contenLink");
      if (contentLink != null) {

        String contentType = activityInfo.templateParams.get("mimeType");
        if (contentType != null) {
          String contentName = activityInfo.templateParams.get("contentName");
          StringBuffer buffer = new StringBuffer();
          buffer.append(domain);
          buffer.append("/portal/rest/jcr/");
          buffer.append(contentLink);
          displayAttachImage(buffer.toString(), contentName, null, contentType, true);
        }

      }
      break;
    case SocialActivityUtil.ACTIVITY_TYPE_ANSWER:
      setActivityTypeAnswer();
      break;

    case SocialActivityUtil.ACTIVITY_TYPE_CALENDAR:
      setActivityTypeCalendar();
      break;

    default:
      break;
    }
  }

  private void setActivityTypeForum() {
    String forumBuffer = SocialActivityUtil.getActivityTypeForum(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);

    textViewName.setText(Html.fromHtml(forumBuffer), TextView.BufferType.SPANNABLE);
    String forumBody = activityInfo.getBody();

    textViewMessage.setText(Html.fromHtml(forumBody), TextView.BufferType.SPANNABLE);

  }

  private void setActivityTypeWiki() {
    String wikiBuffer = SocialActivityUtil.getActivityTypeWiki(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);
    textViewName.setText(Html.fromHtml(wikiBuffer), TextView.BufferType.SPANNABLE);
    String wikiBody = activityInfo.getBody();
    if (wikiBody == null || wikiBody.equalsIgnoreCase("body")) {
      textViewMessage.setVisibility(View.GONE);
    } else {
      textViewMessage.setText(Html.fromHtml(wikiBody), TextView.BufferType.SPANNABLE);
    }
  }

  private void setActivityTypeAnswer() {

    String answerBuffer = SocialActivityUtil.getActivityTypeAnswer(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);

    textViewName.setText(Html.fromHtml(answerBuffer), TextView.BufferType.SPANNABLE);

    String answerBody = activityInfo.getBody();
    if (answerBody != null) {
      textViewMessage.setText(Html.fromHtml(answerBody), TextView.BufferType.SPANNABLE);
    }
  }

  private void setActivityTypeCalendar() {
    String calendarBuffer = SocialActivityUtil.getActivityTypeCalendar(userName,
                                                                       activityInfo,
                                                                       resource,
                                                                       FONT_COLOR,
                                                                       IS_HOME_STREAM);

    textViewName.setText(Html.fromHtml(calendarBuffer), TextView.BufferType.SPANNABLE);
    SocialActivityUtil.setCalendarContent(textViewMessage, activityInfo, resource);

  }

  private void setActivityTypeLink() {

    String info = SocialActivityUtil.getLinkActivityInfo(activityInfo, FONT_COLOR, resource);
    textViewName.setText(Html.fromHtml(info), TextView.BufferType.SPANNABLE);

    String templateComment = activityInfo.templateParams.get("comment");
    String description = activityInfo.templateParams.get("description");

    if (templateComment != null && !templateComment.equalsIgnoreCase("")) {
      textViewMessage.setText(Html.fromHtml(templateComment, new EmptyImageGetter(mContext), null), TextView.BufferType.SPANNABLE);
    }
    if (description != null) {
      textViewCommnet.setText(Html.fromHtml(description.trim()), TextView.BufferType.SPANNABLE);
      textViewCommnet.setVisibility(View.VISIBLE);
    }

    String linkBuffer = SocialActivityUtil.getActivityTypeLink(description, activityInfo, FONT_COLOR, IS_HOME_STREAM);

    String imageParams = activityInfo.templateParams.get("image");
    if ((imageParams != null) && (imageParams.toLowerCase(Locale.US).contains(ExoConstants.HTTP_PROTOCOL))) {
      displayAttachImage(imageParams, "", linkBuffer, "image", true);
    } else {
      textViewTempMessage.setText(Html.fromHtml(linkBuffer), TextView.BufferType.SPANNABLE);
      textViewTempMessage.setVisibility(View.VISIBLE);
    }
  }

  private void displayAttachImage(String url, String name, String description, String fileType, boolean isLinkType) {
    initAttachStubView(url, name, description, fileType, isLinkType);
    attachStubView.setVisibility(View.VISIBLE);
  }

  private void initAttachStubView(final String url,
                                  final String fileName,
                                  String description,
                                  final String fileType,
                                  boolean isLinkType) {
    ImageView attachImage = (ImageView) attachStubView.findViewById(R.id.attached_image_view);
    TextView txtViewFileName = (TextView) attachStubView.findViewById(R.id.textView_file_name);
    if (description == null)
      txtViewFileName.setText(fileName);
    else {
      txtViewFileName.setText(Html.fromHtml(description), TextView.BufferType.SPANNABLE);

      if (isDetail) {
        SocialActivityUtil.setTextLinkfy(txtViewFileName);
        txtViewFileName.setMaxLines(100);
      }
    }

    if (fileType != null && fileType.startsWith(ExoDocumentUtils.IMAGE_TYPE)) {
      int errorDrawable = isLinkType ? R.drawable.icon_for_unreadable_link : R.drawable.icon_for_placeholder_image;
      int maxHeight = resource.getDimensionPixelSize(R.dimen.attachment_max_height);
      ExoPicasso.picasso(mContext)
                .load(Uri.parse(url))
                .placeholder(R.drawable.loading_rect)
                .error(errorDrawable)
                .resize(maxHeight, maxHeight)
                .centerCrop()
                .onlyScaleDown()
                .into(attachImage);
    } else {
      attachImage.setImageResource(ExoDocumentUtils.getIconFromType(fileType));
    }
    if (isDetail) {
      /*
       * Open file with compatible application
       */
      attachImage.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {

          ExoDocumentUtils.fileOpen(mContext, fileType, url, fileName);
        }
      });
    }
  }
}
