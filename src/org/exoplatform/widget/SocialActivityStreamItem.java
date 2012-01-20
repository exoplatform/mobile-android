package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.social.client.api.model.RestActivityStream;
import org.exoplatform.ui.social.SocialAttachedImageActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.utils.SocialActivityUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SocialActivityStreamItem extends LinearLayout {

  public LinearLayout        contentLayoutWrap;

  private View               view;

  private RoundedImageView   imageViewAvatar;

  public TextView            textViewName;

  public TextView            textViewMessage;

  public TextView            textViewTempMessage;

  private TextView           textViewCommnet;

  private Button             buttonComment;

  private Button             buttonLike;

  private ImageView          typeImageView;

  private TextView           textViewTime;

  private View               attachStubView;

  private String             domain;

  private String             userName;

  private Context            mContext;

  private SocialActivityInfo activityInfo;

  private boolean            isDetail;

  public SocialActivityStreamItem(Context context, AttributeSet attrs) {
    super(context, attrs);

  }

  public SocialActivityStreamItem(Context context, SocialActivityInfo info, boolean is) {
    super(context);
    mContext = context;
    activityInfo = info;
    isDetail = is;
    LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    domain = SocialActivityUtil.getDomain();
    view = inflate.inflate(R.layout.activitybrowserviewcell, this);
    imageViewAvatar = (RoundedImageView) view.findViewById(R.id.imageView_Avatar);
    imageViewAvatar.setDefaultImageResource(R.drawable.default_avatar);
    contentLayoutWrap = (LinearLayout) view.findViewById(R.id.relativeLayout_Content);
    textViewName = (TextView) view.findViewById(R.id.textView_Name);
    textViewName.setLinkTextColor(Color.rgb(21, 94, 173));
    textViewMessage = (TextView) view.findViewById(R.id.textView_Message);
    textViewTempMessage = (TextView) view.findViewById(R.id.textview_temp_message);
    textViewCommnet = (TextView) view.findViewById(R.id.activity_comment_view);
    buttonComment = (Button) view.findViewById(R.id.button_Comment);
    buttonLike = (Button) view.findViewById(R.id.button_Like);
    typeImageView = (ImageView) view.findViewById(R.id.activity_image_type);
    textViewTime = (TextView) view.findViewById(R.id.textView_Time);

    initCommonInfo();
    setDetailView();

  }

  private void initCommonInfo() {

    String avatarUrl = activityInfo.getImageUrl();
    if (avatarUrl != null) {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = 2;
      imageViewAvatar.setOptions(options);
      imageViewAvatar.setUrl(avatarUrl);
    }

    userName = activityInfo.getUserName();
    textViewName.setText(Html.fromHtml(userName));
    textViewMessage.setText(Html.fromHtml(activityInfo.getTitle()), TextView.BufferType.SPANNABLE);

    textViewTime.setText(SocialActivityUtil.getPostedTimeString(activityInfo.getPostedTime()));
    buttonComment.setText("" + activityInfo.getCommentNumber());
    buttonLike.setText("" + activityInfo.getLikeNumber());
    int imageId = SocialActivityUtil.getActivityTypeId(activityInfo.getType());
    SocialActivityUtil.setImageType(imageId, typeImageView);
    setViewByType(imageId);

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
    
    switch (typeId) {
    case 1:
      // ks-forum:spaces

      setActivityTypeForum();
      break;
    case 2:
      // ks-wiki:spaces
      // Map<String, String> templateMap = activityInfo.templateParams;
      // Set<String> set = templateMap.keySet();
      // for (String param : set) {
      // System.out.println("type: " + activityInfo.getType() +
      // "--template key: " + param + "-- "
      // + templateMap.get(param));
      // }
      setActivityTypeWiki();
      break;
    case 3:
      // exosocial:spaces
      break;
    case 4:
      // DOC_ACTIVITY

      String tempMessage = activityInfo.templateParams.get("MESSAGE");
      if (tempMessage != null) {
        textViewTempMessage.setText(tempMessage.trim());
        textViewTempMessage.setVisibility(View.VISIBLE);
      }

      String docLink = activityInfo.templateParams.get("DOCLINK");
      if (docLink != null) {
        String docName = activityInfo.templateParams.get("DOCNAME");
        if (PhotoUtils.isImages(docName)) {
          String url = domain + docLink;
          displayAttachImage(url, docName, null);
        }
      }

      break;
    case 5:
      // DEFAULT_ACTIVITY
      break;
    case 6:
      // LINK_ACTIVITY
      setActivityTypeLink();
      break;
    case 7:
      // exosocial:relationship

      break;
    case 8:
      // exosocial:people
      break;
    case 9:
      // contents:spaces
      String contentLink = activityInfo.templateParams.get("contenLink");
      if (contentLink != null) {
        String contentType = activityInfo.templateParams.get("mimeType");
        if (contentType != null && (contentType.equalsIgnoreCase("image/jpeg"))) {
          StringBuffer buffer = new StringBuffer();
          buffer.append(domain);
          buffer.append("/portal/rest/jcr/");
          buffer.append(contentLink);
          displayAttachImage(buffer.toString(), "", null);
        }

      }
      break;
    case 10:
      // ks-answer
      setActivityTypeAnswer();
      break;

    case 11:
      // calendar
      setActivityTypeCalendar();
      break;
    default:
      break;
    }
  }

  private String addSpaceInfo() {
    StringBuffer spaceBuffer = new StringBuffer();
    spaceBuffer.append("<font style=\"font-style:normal\" color=\"#696969\">");
    spaceBuffer.append(LocalizationHelper.getInstance().getString("In"));
    spaceBuffer.append("</font>");
    spaceBuffer.append(" ");
    RestActivityStream actStream = activityInfo.restActivityStream;
    String nameSpace = actStream.getFullName();
    String spaceLink = actStream.getPermaLink();
    spaceBuffer.append("<a href=");
    spaceBuffer.append(spaceLink);
    spaceBuffer.append(">");
    spaceBuffer.append(nameSpace);
    spaceBuffer.append("</a>");
    spaceBuffer.append(" ");
    spaceBuffer.append("<font style=\"font-style:normal\" color=\"#696969\">");
    spaceBuffer.append(LocalizationHelper.getInstance().getString("Space"));
    spaceBuffer.append("</font>");
    return spaceBuffer.toString();
  }

  private void setActivityTypeForum() {
    String forumLink = null;
    StringBuffer forumBuffer = new StringBuffer();
    forumBuffer.append("<html><body>");
    forumBuffer.append(userName);
    forumBuffer.append(" ");
    forumBuffer.append(addSpaceInfo());
    forumBuffer.append(" ");
    String actType = activityInfo.templateParams.get("ActivityType");
    String actTypeDesc = null;
    String forumName = null;
    forumBuffer.append("<font style=\"font-style:normal\" color=\"#696969\">");
    if (actType.equalsIgnoreCase("AddPost")) {
      forumLink = activityInfo.templateParams.get("PostLink");
      actTypeDesc = LocalizationHelper.getInstance().getString("HasAddANewPost");
      forumName = activityInfo.templateParams.get("PostName");
    } else if (actType.equalsIgnoreCase("UpdatePost")) {
      forumLink = activityInfo.templateParams.get("PostLink");
      actTypeDesc = LocalizationHelper.getInstance().getString("HasUpdateANewPost");
      forumName = activityInfo.templateParams.get("PostName");
    } else if (actType.equalsIgnoreCase("AddTopic")) {
      forumLink = activityInfo.templateParams.get("TopicLink");
      actTypeDesc = LocalizationHelper.getInstance().getString("HasPostedAnewTopic");
      forumName = activityInfo.templateParams.get("TopicName");
    } else if (actType.equalsIgnoreCase("UpdateTopic")) {
      forumLink = activityInfo.templateParams.get("TopicLink");
      actTypeDesc = LocalizationHelper.getInstance().getString("HasUpdateAnewTopic");
      forumName = activityInfo.templateParams.get("TopicName");
    }
    forumBuffer.append(actTypeDesc);
    forumBuffer.append("</font>");
    forumBuffer.append("<br>");
    forumBuffer.append("<a href=");
    forumBuffer.append(forumLink);
    forumBuffer.append(">");
    forumBuffer.append(forumName);
    forumBuffer.append("</a>");
    forumBuffer.append("</body></html>");

    textViewName.setText(Html.fromHtml(forumBuffer.toString()), TextView.BufferType.SPANNABLE);
    String forumBody = activityInfo.getBody();

    textViewMessage.setText(Html.fromHtml(forumBody), TextView.BufferType.SPANNABLE);

  }

  private void setActivityTypeWiki() {
    String wiki_url = null;
    StringBuffer buffer = new StringBuffer();
    buffer.append("<html><body>");
    buffer.append("<a>");
    buffer.append(userName);
    buffer.append("</a> ");
    buffer.append(addSpaceInfo());
    buffer.append(" ");
    buffer.append("<font color=\"#696969\">");
    String act_key = activityInfo.templateParams.get("act_key");
    String act_key_des = null;
    if (act_key != null) {
      if (act_key.equalsIgnoreCase("update_page")) {
        wiki_url = activityInfo.templateParams.get("view_change_url");
        act_key_des = LocalizationHelper.getInstance().getString("HasEditWikiPage");
      } else if (act_key.equalsIgnoreCase("add_page")) {
        wiki_url = activityInfo.templateParams.get("page_url");
        act_key_des = LocalizationHelper.getInstance().getString("HasCreatWikiPage");
      }
    }
    buffer.append(act_key_des);
    buffer.append("</font>");
    buffer.append("<br>");
    String page_name = activityInfo.templateParams.get("page_name");
    buffer.append("<a href=");
    buffer.append(wiki_url);
    buffer.append(">");
    buffer.append(page_name);
    buffer.append("</a>");
    buffer.append("</body></html>");
    textViewName.setText(Html.fromHtml(buffer.toString()), TextView.BufferType.SPANNABLE);
    String wikiBody = activityInfo.getBody();
    if (wikiBody == null || wikiBody.equalsIgnoreCase("body")) {
      textViewMessage.setVisibility(View.GONE);
    } else {
      textViewMessage.setText(Html.fromHtml(wikiBody), TextView.BufferType.SPANNABLE);
    }
  }

  private void setActivityTypeAnswer() {
    String answer_link = null;
    StringBuffer answerBuffer = new StringBuffer();
    answerBuffer.append("<html><body>");
    answerBuffer.append("<a>");
    answerBuffer.append(userName);
    answerBuffer.append("</a> ");
    answerBuffer.append(addSpaceInfo());
    answerBuffer.append(" ");
    answerBuffer.append("<font color=\"#696969\">");
    String act_key = activityInfo.templateParams.get("ActivityType");
    String act_key_des = null;
    if (act_key.equalsIgnoreCase("QuestionUpdate")) {
      act_key_des = LocalizationHelper.getInstance().getString("HasUpdatedQuestion");
    } else if (act_key.equalsIgnoreCase("QuestionAdd")) {
      act_key_des = LocalizationHelper.getInstance().getString("HasAskAnswer");
    } else if (act_key.equalsIgnoreCase("AnswerAdd")) {
      act_key_des = LocalizationHelper.getInstance().getString("HasAnswerQuestion");
    }
    answerBuffer.append(act_key_des);
    answerBuffer.append("</font>");
    answerBuffer.append("<br>");
    answer_link = activityInfo.templateParams.get("Link");
    String page_name = activityInfo.templateParams.get("Name");
    answerBuffer.append("<a href=");
    answerBuffer.append(answer_link);
    answerBuffer.append(">");
    answerBuffer.append(page_name);
    answerBuffer.append("</a>");
    answerBuffer.append("</body></html>");

    textViewName.setText(Html.fromHtml(answerBuffer.toString()), TextView.BufferType.SPANNABLE);

    String answerBody = activityInfo.getBody();
    if (answerBody != null) {
      textViewMessage.setText(Html.fromHtml(answerBody), TextView.BufferType.SPANNABLE);
    }
  }

  private void setActivityTypeCalendar() {
    StringBuffer forumBuffer = new StringBuffer();
    forumBuffer.append("<html><body>");
    forumBuffer.append("<a>");
    forumBuffer.append(userName);
    forumBuffer.append("</a> ");
    forumBuffer.append(addSpaceInfo());
    forumBuffer.append(" ");
    String actType = activityInfo.templateParams.get("EventType");
    String actTypeDesc = null;
    String forumName = null;
    forumBuffer.append("<font color=\"#696969\">");
    if (actType.equalsIgnoreCase("EventAdded")) {
      actTypeDesc = LocalizationHelper.getInstance().getString("AddedAnEvent");
    } else if (actType.equalsIgnoreCase("EventUpdated")) {
      actTypeDesc = LocalizationHelper.getInstance().getString("UpdatedAnEvent");
    } else if (actType.equalsIgnoreCase("TaskAdded")) {
      actTypeDesc = LocalizationHelper.getInstance().getString("AddedATask");
    } else if (actType.equalsIgnoreCase("TaskUpdated")) {
      actTypeDesc = LocalizationHelper.getInstance().getString("UpdatedATask");
    }
    forumBuffer.append(actTypeDesc);
    forumBuffer.append("</font>");
    forumBuffer.append("<br>");
    forumBuffer.append("<a>");
    forumName = activityInfo.templateParams.get("EventSummary");
    forumBuffer.append("<font color=\"#000000\">");
    forumBuffer.append(forumName);
    forumBuffer.append("</font>");
    forumBuffer.append("</a>");
    forumBuffer.append("</body></html>");

    textViewName.setText(Html.fromHtml(forumBuffer.toString()), TextView.BufferType.SPANNABLE);
    setCaledarContent(textViewMessage);

  }

  private void setCaledarContent(TextView textView) {
    StringBuffer caledarBuffer = new StringBuffer();
    caledarBuffer.append("<html><body>");
    caledarBuffer.append(LocalizationHelper.getInstance().getString("CalendarDescription"));
    caledarBuffer.append("\n");
    String description = activityInfo.templateParams.get("EventDescription");
    if (description != null) {
      caledarBuffer.append(description);
    }
    caledarBuffer.append("<br>");
    caledarBuffer.append(LocalizationHelper.getInstance().getString("CalendarLocation"));
    caledarBuffer.append(" ");
    String location = activityInfo.templateParams.get("EventLocale");
    if (location != null) {
      caledarBuffer.append(location);
    }
    caledarBuffer.append("<br>");
    caledarBuffer.append(LocalizationHelper.getInstance().getString("CalendarStart"));
    caledarBuffer.append(" ");
    String startTime = activityInfo.templateParams.get("EventStartTime");
    startTime = PhotoUtils.getDateFromString(startTime);
    caledarBuffer.append(startTime);
    caledarBuffer.append("<br>");
    caledarBuffer.append(LocalizationHelper.getInstance().getString("CalendarEnd"));
    caledarBuffer.append(" ");
    String endTime = activityInfo.templateParams.get("EventEndTime");
    endTime = PhotoUtils.getDateFromString(endTime);
    caledarBuffer.append(endTime);
    caledarBuffer.append("</body></html>");
    textView.setText(Html.fromHtml(caledarBuffer.toString()));
  }

  private void setActivityTypeLink() {
    StringBuffer linkBuffer = new StringBuffer();
    String linkTitle = activityInfo.templateParams.get("title").trim();
    String linkUrl = activityInfo.templateParams.get("link");
    String templateComment = activityInfo.templateParams.get("comment");
    String description = activityInfo.templateParams.get("description").trim();
    linkBuffer.append("<html><body>");

    if (templateComment != null && !templateComment.equalsIgnoreCase("")) {
      textViewMessage.setText(Html.fromHtml(templateComment), TextView.BufferType.SPANNABLE);
    }
    if (description != null) {
      textViewCommnet.setText(Html.fromHtml(description), TextView.BufferType.SPANNABLE);
      textViewCommnet.setVisibility(View.VISIBLE);
    }
    linkBuffer.append("<a href=");
    linkBuffer.append(linkUrl);
    linkBuffer.append(">");
    linkBuffer.append(linkTitle);
    linkBuffer.append("</a>");
    linkBuffer.append("</body></html>");

    String imageParams = activityInfo.templateParams.get("image");
    if ((imageParams != null) && (imageParams.contains("http"))) {
      displayAttachImage(imageParams, "", linkBuffer.toString());
    } else {
      textViewTempMessage.setText(Html.fromHtml(linkBuffer.toString()),
                                  TextView.BufferType.SPANNABLE);
      textViewTempMessage.setVisibility(View.VISIBLE);
    }
  }

  private void displayAttachImage(String url, String name, String description) {
    if (attachStubView == null) {
      initAttachStubView(url, name, description);
    }
    attachStubView.setVisibility(View.VISIBLE);
  }

  private void initAttachStubView(final String url, String fileName, String description) {
    attachStubView = ((ViewStub) findViewById(R.id.attached_image_stub_activity)).inflate();
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

    if (SocialDetailHelper.getInstance().taskIsFinish = true) {
      SocialDetailHelper.getInstance().imageDownloader.download(url,
                                                                attachImage,
                                                                ExoConnectionUtils._strCookie);

      if (isDetail) {
        attachImage.setOnClickListener(new OnClickListener() {

          // @Override
          public void onClick(View v) {
            SocialDetailHelper.getInstance().setAttachedImageUrl(url);
            Intent intent = new Intent(mContext, SocialAttachedImageActivity.class);
            mContext.startActivity(intent);
          }
        });
      }
    }

  }

  public Button likeButton() {
    return buttonLike;
  }

  public Button commentButton() {
    return buttonComment;
  }
}
