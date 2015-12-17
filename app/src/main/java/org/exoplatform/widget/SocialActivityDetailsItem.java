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

import java.net.URLConnection;
import java.util.Locale;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.ExoUtils;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.image.EmptyImageGetter;
import org.exoplatform.utils.image.ExoPicasso;
import org.exoplatform.utils.image.RoundedCornersTranformer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Represents the layout for activity details
 */
public class SocialActivityDetailsItem extends LinearLayout {
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

  private TextView            textViewComment;

  public Button               buttonComment;

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

  public SocialActivityDetailsItem(Context ctx) {
    super(ctx);
  }

  public SocialActivityDetailsItem(Context ctx, AttributeSet attrs) {
    super(ctx, attrs);
  }

  public SocialActivityDetailsItem(Context ctx, AttributeSet attrs, int defStyle) {
    super(ctx, attrs, defStyle);
  }

  public SocialActivityDetailsItem(Context context, SocialActivityInfo info, boolean is) {
    super(context);

    mContext = context;
    resource = mContext.getResources();
    activityInfo = info;
    isDetail = is;
    LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    domain = SocialActivityUtil.getDomain();
    View view = inflate.inflate(R.layout.activitybrowserviewcell, this);
    imageViewAvatar = (ImageView) view.findViewById(R.id.imageView_Avatar);
    contentLayoutWrap = (LinearLayout) view.findViewById(R.id.relativeLayout_Content);
    textViewName = (TextView) view.findViewById(R.id.textView_Name);
    textViewName.setLinkTextColor(Color.rgb(21, 94, 173));
    textViewMessage = (TextView) view.findViewById(R.id.textView_Message);
    textViewTempMessage = (TextView) view.findViewById(R.id.textview_temp_message);
    textViewComment = (TextView) view.findViewById(R.id.activity_comment_view);
    buttonComment = (Button) view.findViewById(R.id.button_Comment);
    buttonLike = (Button) view.findViewById(R.id.button_Like);
    typeImageView = (ImageView) view.findViewById(R.id.activity_image_type);
    textViewTime = (TextView) view.findViewById(R.id.textView_Time);

    initCommonInfo();

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

    textViewTime.setText(SocialActivityUtil.getPostedTimeString(mContext,
                                                                activityInfo.getUpdatedTime() != 0 ? activityInfo.getUpdatedTime()
                                                                                                  : activityInfo.getPostedTime()));
    buttonComment.setText(String.valueOf(activityInfo.getCommentNumber()));
    buttonLike.setText(String.valueOf(activityInfo.getLikeNumber()));
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
      textViewComment.setMaxLines(100);
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
      if (tempMessage != null) {
        textViewMessage.setText(tempMessage.trim());
      }

      String docLink = activityInfo.templateParams.get("DOCLINK");
      if (docLink != null) {
        String docName = activityInfo.templateParams.get("DOCNAME");
        String url = domain + docLink;
        String mimeTypeExtension = URLConnection.guessContentTypeFromName(url);
        displayAttachImage(url, docName, null, mimeTypeExtension, false);
      }

      break;

    case SocialActivityUtil.ACTIVITY_TYPE_NORMAL:
      /* add space information */
      spaceInfo = SocialActivityUtil.getHeaderWithSpaceInfo(userName, activityInfo, resource, FONT_COLOR, IS_HOME_STREAM);
      if (spaceInfo != null)
        textViewName.setText(Html.fromHtml(spaceInfo), TextView.BufferType.SPANNABLE);
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
          displayAttachImage(buffer.toString(), contentName, null, contentType, false);
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
      // use the page excerpt if no body is available
      wikiBody = activityInfo.templateParams != null ? activityInfo.templateParams.get("page_exceprt") : null;
    }
    if (wikiBody == null) {
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
    // Display the activity's author name
    // If the activity is published in a space, display the space name too
    String info = SocialActivityUtil.getLinkActivityInfo(activityInfo, FONT_COLOR, resource);
    textViewName.setText(Html.fromHtml(info), TextView.BufferType.SPANNABLE);

    String templateComment = activityInfo.templateParams.get("comment");
    String description = activityInfo.templateParams.get("description").trim();

    if (templateComment != null && !templateComment.equalsIgnoreCase("")) {
      textViewMessage.setText(Html.fromHtml(templateComment, new EmptyImageGetter(mContext), null), TextView.BufferType.SPANNABLE);
    }
    if (description != null) {
      textViewComment.setText(Html.fromHtml(description), TextView.BufferType.SPANNABLE);
      textViewComment.setVisibility(View.VISIBLE);
    }

    String linkBuffer = SocialActivityUtil.getActivityTypeLink(description, activityInfo, FONT_COLOR, false);

    String imageParams = activityInfo.templateParams.get(ExoDocumentUtils.IMAGE_TYPE);
    if ((imageParams != null) && (imageParams.toLowerCase(Locale.US).contains(ExoConstants.HTTP_PROTOCOL))) {
      displayAttachImage(imageParams, "", linkBuffer, ExoDocumentUtils.IMAGE_TYPE, true);
    } else {
      textViewTempMessage.setText(Html.fromHtml(linkBuffer), TextView.BufferType.SPANNABLE);
      textViewTempMessage.setVisibility(View.VISIBLE);
    }
  }

  private void displayAttachImage(String url, String name, String description, String fileType, boolean isLinkType) {
    if (attachStubView == null) {
      initAttachStubView(url, name, description, fileType, isLinkType);
    }
    attachStubView.setVisibility(View.VISIBLE);
  }

  private void initAttachStubView(final String url,
                                  final String fileName,
                                  String description,
                                  final String fileType,
                                  boolean isLinkType) {
    attachStubView = ((ViewStub) findViewById(R.id.attached_image_stub_activity)).inflate();
    ImageView attachImage = (ImageView) attachStubView.findViewById(R.id.attached_image_view);
    TextView txtViewFileName = (TextView) attachStubView.findViewById(R.id.textView_file_name);
    if (description == null)
      txtViewFileName.setText(fileName);
    else {
      txtViewFileName.setText(Html.fromHtml(description), TextView.BufferType.SPANNABLE);

      if (isDetail) {
        SocialActivityUtil.setTextLinkify(txtViewFileName);
        txtViewFileName.setMaxLines(100);
      }
    }

    if (fileType != null && fileType.startsWith(ExoDocumentUtils.IMAGE_TYPE)) {
      int errorDrawable = isLinkType ? R.drawable.icon_for_unreadable_link : R.drawable.icon_for_placeholder_image;

      ExoPicasso.picasso(mContext)
                .load(Uri.parse(ExoUtils.encodeDocumentUrl(url)))
                .placeholder(R.drawable.loading_rect)
                .error(errorDrawable)
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

  public Button likeButton() {
    return buttonLike;
  }

  public Button commentButton() {
    return buttonComment;
  }
}
