package org.exoplatform.widget;

import java.net.URLConnection;
import java.util.Locale;

import android.graphics.Bitmap;
import android.util.Log;
import greendroid.widget.AsyncImageView;
import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.image.SocialImageLoader;

import android.content.Context;
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

/**
 * Represents the layout for activity details
 */
public class SocialActivityStreamItem extends LinearLayout {
  private static final String FONT_COLOR          = "#696969";

  private static final int    AVATAR_BORDER_COLOR = 0x22000000;

  public LinearLayout         contentLayoutWrap;

  private View                view;

  private ShaderImageView     imageViewAvatar;

  public TextView             textViewName;

  public TextView             textViewMessage;

  public TextView             textViewTempMessage;

  private TextView            textViewCommnet;

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

  private static final String TAG = "eXo____SocialActivityStreamItem____";

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
    imageViewAvatar = (ShaderImageView) view.findViewById(R.id.imageView_Avatar);
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

  }

  public void initCommonInfo() {
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
    setDetailView();
  }

  private void setDetailView() {
    if (isDetail) {
      imageViewAvatar.setBorderColor(AVATAR_BORDER_COLOR);
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
    case SocialActivityUtil.KS_FORUM_SPACE:

      setActivityTypeForum();
      break;
    case SocialActivityUtil.KS_WIKI_SPACE:
      // Map<String, String> templateMap = activityInfo.templateParams;
      // Set<String> set = templateMap.keySet();
      // for (String param : set) {
      // System.out.println("type: " + activityInfo.getType() +
      // "--template key: " + param + "-- "
      // + templateMap.get(param));
      // }
      setActivityTypeWiki();
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
        textViewName.setText(Html.fromHtml(docBuffer), TextView.BufferType.SPANNABLE);
      }

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
    case SocialActivityUtil.DEFAULT_ACTIVITY:
      break;
    case SocialActivityUtil.LINK_ACTIVITY:
      setActivityTypeLink();
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
        textViewName.setText(Html.fromHtml(spaceBuffer), TextView.BufferType.SPANNABLE);
      }

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
    case SocialActivityUtil.KS_ANSWER:
      setActivityTypeAnswer();
      break;

    case SocialActivityUtil.CS_CALENDAR_SPACES:
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

    String imageParams = activityInfo.templateParams.get(ExoDocumentUtils.IMAGE_TYPE);
    if ((imageParams != null) && (imageParams.toLowerCase(Locale.US).contains(ExoConstants.HTTP_PROTOCOL))) {
      displayAttachImage(imageParams, "", linkBuffer, ExoDocumentUtils.IMAGE_TYPE, true);
    } else {
      textViewTempMessage.setText(Html.fromHtml(linkBuffer), TextView.BufferType.SPANNABLE);
      textViewTempMessage.setVisibility(View.VISIBLE);
    }
  }

  private void displayAttachImage(String url,
                                  String name,
                                  String description,
                                  String fileType,
                                  boolean isLinkType) {
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
        SocialActivityUtil.setTextLinkfy(txtViewFileName);
        txtViewFileName.setMaxLines(100);
      }

    }
    /*
     * Use SocialImageLoader to get and display attached image.
     */

    if (fileType != null && fileType.startsWith(ExoDocumentUtils.IMAGE_TYPE)) {
      if (SocialDetailHelper.getInstance().socialImageLoader == null) {
        SocialDetailHelper.getInstance().socialImageLoader = new SocialImageLoader(mContext);
      }
      SocialDetailHelper.getInstance().socialImageLoader.displayImage(url, attachImage, isLinkType);
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
