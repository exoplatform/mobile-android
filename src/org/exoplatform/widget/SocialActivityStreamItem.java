package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.social.client.api.model.RestActivityStream;
import org.exoplatform.ui.social.SocialAttachedImageActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.image.SocialImageLoader;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
  private static final String FONT_COLOR = "#696969";

  public LinearLayout         contentLayoutWrap;

  private View                view;

  private RoundedImageView    imageViewAvatar;

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

  public SocialActivityStreamItem(Context context, AttributeSet attrs) {
    super(context, attrs);

  }

  public SocialActivityStreamItem(Context context, SocialActivityInfo info, boolean is) {
    super(context);
    mContext = context;
    resource = mContext.getResources();
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

    textViewTime.setText(SocialActivityUtil.getPostedTimeString(mContext,
                                                                activityInfo.getPostedTime()));
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
      /*
       * add space information
       */

      String docBuffer = SocialActivityUtil.getActivityTypeDocument(userName,
                                                                    activityInfo,
                                                                    resource,
                                                                    FONT_COLOR,
                                                                    true);
      if (docBuffer != null) {
        textViewName.setText(Html.fromHtml(docBuffer), TextView.BufferType.SPANNABLE);
      }

      String tempMessage = activityInfo.templateParams.get("MESSAGE");
      if (tempMessage != null) {
        textViewTempMessage.setText(tempMessage.trim());
        textViewTempMessage.setVisibility(View.VISIBLE);
      }

      String docLink = activityInfo.templateParams.get("DOCLINK");
      if (docLink != null) {
        String docName = activityInfo.templateParams.get("DOCNAME");
        String url = domain + docLink;
        displayAttachImage(url, docName, null);

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
      /*
       * add space information
       */

      String spaceBuffer = SocialActivityUtil.getActivityTypeDocument(userName,
                                                                      activityInfo,
                                                                      resource,
                                                                      FONT_COLOR,
                                                                      true);
      if (spaceBuffer != null) {
        textViewName.setText(Html.fromHtml(spaceBuffer), TextView.BufferType.SPANNABLE);
      }

      String contentLink = activityInfo.templateParams.get("contenLink");
      if (contentLink != null) {

        String contentType = activityInfo.templateParams.get("mimeType");
        if (contentType != null && (contentType.contains("image"))) {
          String contentName = activityInfo.templateParams.get("contentName");
          StringBuffer buffer = new StringBuffer();
          buffer.append(domain);
          buffer.append("/portal/rest/jcr/");
          buffer.append(contentLink);
          displayAttachImage(buffer.toString(), contentName, null);
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

  private void setActivityTypeForum() {
    String forumBuffer = SocialActivityUtil.getActivityTypeForum(userName,
                                                                 activityInfo,
                                                                 resource,
                                                                 FONT_COLOR,
                                                                 false);

    textViewName.setText(Html.fromHtml(forumBuffer), TextView.BufferType.SPANNABLE);
    String forumBody = activityInfo.getBody();

    textViewMessage.setText(Html.fromHtml(forumBody), TextView.BufferType.SPANNABLE);

  }

  private void setActivityTypeWiki() {
    String wikiBuffer = SocialActivityUtil.getActivityTypeWiki(userName,
                                                               activityInfo,
                                                               resource,
                                                               FONT_COLOR,
                                                               false);
    textViewName.setText(Html.fromHtml(wikiBuffer), TextView.BufferType.SPANNABLE);
    String wikiBody = activityInfo.getBody();
    if (wikiBody == null || wikiBody.equalsIgnoreCase("body")) {
      textViewMessage.setVisibility(View.GONE);
    } else {
      textViewMessage.setText(Html.fromHtml(wikiBody), TextView.BufferType.SPANNABLE);
    }
  }

  private void setActivityTypeAnswer() {

    String answerBuffer = SocialActivityUtil.getActivityTypeAnswer(userName,
                                                                   activityInfo,
                                                                   resource,
                                                                   FONT_COLOR,
                                                                   false);

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
                                                                       false);

    textViewName.setText(Html.fromHtml(calendarBuffer), TextView.BufferType.SPANNABLE);
    SocialActivityUtil.setCaledarContent(textViewMessage, activityInfo, resource);

  }

  private void setActivityTypeLink() {
    String templateComment = activityInfo.templateParams.get("comment");
    String description = activityInfo.templateParams.get("description").trim();

    if (templateComment != null && !templateComment.equalsIgnoreCase("")) {
      textViewMessage.setText(Html.fromHtml(templateComment), TextView.BufferType.SPANNABLE);
    }
    if (description != null) {
      textViewCommnet.setText(Html.fromHtml(description), TextView.BufferType.SPANNABLE);
      textViewCommnet.setVisibility(View.VISIBLE);
    }

    String linkBuffer = SocialActivityUtil.getActivityTypeLink(description,
                                                               activityInfo,
                                                               FONT_COLOR,
                                                               false);

    String imageParams = activityInfo.templateParams.get("image");
    if ((imageParams != null) && (imageParams.contains("http"))) {
      displayAttachImage(imageParams, "", linkBuffer);
    } else {
      textViewTempMessage.setText(Html.fromHtml(linkBuffer), TextView.BufferType.SPANNABLE);
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
    /*
     * Use SocialImageLoader to get and display attached image.
     */
    if (SocialDetailHelper.getInstance().socialImageLoader == null) {
      SocialDetailHelper.getInstance().socialImageLoader = new SocialImageLoader(mContext);
    }
    SocialDetailHelper.getInstance().socialImageLoader.displayImage(url, attachImage);

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

  public Button likeButton() {
    return buttonLike;
  }

  public Button commentButton() {
    return buttonComment;
  }
}
