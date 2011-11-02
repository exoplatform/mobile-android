package org.exoplatform.widget;

import greendroid.widget.AsyncImageView;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;

import android.content.Context;
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

  private LinearLayout       contentLayoutWrap;

  private View               view;

  private AsyncImageView     imageViewAvatar;

  private TextView           textViewName;

  public TextView            textViewMessage;

  private TextView           textViewCommnet;

  private Button             buttonComment;

  private Button             buttonLike;

  private ImageView          typeImageView;

  private TextView           textViewTime;

  private View               attachStubView;

  private String             domain;

  private Context            mContext;

  private SocialActivityInfo activityInfo;

  public SocialActivityStreamItem(Context context, AttributeSet attrs) {
    super(context, attrs);

  }

  public SocialActivityStreamItem(Context context, SocialActivityInfo info, boolean isDetail) {
    super(context);
    mContext = context;
    activityInfo = info;
    LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    domain = SocialActivityUtil.getDomain();
    view = inflate.inflate(R.layout.activitybrowserviewcell, this);
    imageViewAvatar = (AsyncImageView) view.findViewById(R.id.imageView_Avatar);
    contentLayoutWrap = (LinearLayout) view.findViewById(R.id.relativeLayout_Content);
    textViewName = (TextView) view.findViewById(R.id.textView_Name);
    textViewMessage = (TextView) view.findViewById(R.id.textView_Message);
    textViewCommnet = (TextView) view.findViewById(R.id.activity_comment_view);
    buttonComment = (Button) view.findViewById(R.id.button_Comment);
    buttonLike = (Button) view.findViewById(R.id.button_Like);
    typeImageView = (ImageView) view.findViewById(R.id.activity_image_type);
    textViewTime = (TextView) view.findViewById(R.id.textView_Time);
    setDetailView(isDetail);
    initCommonInfo();
  }

  private void initCommonInfo() {
    String avatarUrl = activityInfo.getImageUrl();
    if (avatarUrl == null) {
      imageViewAvatar.setImageResource(ExoConstants.DEFAULT_AVATAR);
    } else
      imageViewAvatar.setUrl(avatarUrl);
    textViewName.setText(Html.fromHtml(activityInfo.getUserName()));
    try {
      String title = new String(activityInfo.getTitle().getBytes("ISO-8859-1"), "UTF-8");
      textViewMessage.setText(Html.fromHtml(title), TextView.BufferType.SPANNABLE);
    } catch (UnsupportedEncodingException e) {
    }
    textViewTime.setText(SocialActivityUtil.getPostedTimeString(activityInfo.getPostedTime()));
    buttonComment.setText("" + activityInfo.getCommentNumber());
    buttonLike.setText("" + activityInfo.getLikeNumber());
    int imageId = SocialActivityUtil.getActyvityTypeId(activityInfo.getType());
    SocialActivityUtil.setImageType(imageId, typeImageView);
    setViewByType(imageId);

  }

  private void setDetailView(boolean is) {
    if (is) {
      contentLayoutWrap.setBackgroundDrawable(null);
      buttonComment.setVisibility(View.GONE);
      buttonLike.setVisibility(View.GONE);
    }

  }

  private void setViewByType(int typeId) {
    switch (typeId) {
    case 1:
      setActivityTypeForum();
      Map<String, String> templateMap = activityInfo.templateParams;
      Set<String> set = templateMap.keySet();
      for (String param : set) {
        System.out.println("type: " + activityInfo.getType() + "--template key: " + param + "-- "
            + templateMap.get(param));
      }
      break;
    case 2:
      setActivityTypeWiki();
      /*
       * Map<String, String> templateMap = activityInfo.templateParams;
       * Set<String> set = templateMap.keySet(); for (String param : set) {
       * System.out.println("type: " + activityInfo.getType() +
       * "--template key: " + param + "-- " + templateMap.get(param)); }
       */
      break;
    case 3:

      break;
    case 4:
      String docLink = activityInfo.templateParams.get("DOCLINK");
      if (docLink != null) {
        displayAttachImage(domain + docLink);
      }
      break;
    case 5:

      break;
    case 6:

      String templateComment = activityInfo.templateParams.get("comment");
      textViewCommnet.setText(templateComment);
      textViewCommnet.setVisibility(View.VISIBLE);

      String imageParams = activityInfo.templateParams.get("image");
      if ((imageParams != null) && (imageParams.contains("http"))) {
        displayAttachImage(imageParams);
      }

      break;
    case 7:

      break;
    case 8:

      break;
    case 9:

      String contentLink = activityInfo.templateParams.get("contenLink");
      if (contentLink != null) {
        contentLink = domain + "/rest/private/jcr/" + contentLink;
        displayAttachImage(contentLink);
      }
      break;
    default:
      break;
    }
  }

  private void setActivityTypeForum() {
    StringBuffer forumBuffer = new StringBuffer();
    forumBuffer.append("<html><body>");
    forumBuffer.append(activityInfo.getUserName());
    forumBuffer.append(" ");
    String actType = activityInfo.templateParams.get("ActivityType");
    String actTypeDesc = null;
    String forumName = null;
    forumBuffer.append("<font color=\"#696969\">");
    if (actType.equalsIgnoreCase("AddPost")) {
      actTypeDesc = LocalizationHelper.getInstance().getString("HasAddANewPost");
      forumBuffer.append(actTypeDesc);
      forumName = activityInfo.templateParams.get("PostName");
    } else if (actType.equalsIgnoreCase("UpdatePost")) {
      actTypeDesc = LocalizationHelper.getInstance().getString("HasUpdateANewPost");
      forumBuffer.append(actTypeDesc);
      forumName = activityInfo.templateParams.get("PostName");
    } else if (actType.equalsIgnoreCase("AddTopic")) {
      actTypeDesc = LocalizationHelper.getInstance().getString("HasPostedAnewTopic");
      forumBuffer.append(actTypeDesc);
      forumName = activityInfo.templateParams.get("TopicName");
    } else if (actType.equalsIgnoreCase("UpdateTopic")) {
      actTypeDesc = LocalizationHelper.getInstance().getString("HasUpdateAnewTopic");
      forumBuffer.append(actTypeDesc);
      forumName = activityInfo.templateParams.get("TopicName");
    }
    forumBuffer.append("</font>");
    forumBuffer.append(" ");
    forumBuffer.append("<a>");
    forumBuffer.append(forumName);
    forumBuffer.append("</a>");
    forumBuffer.append("</body></html>");
    textViewName.setText(Html.fromHtml(forumBuffer.toString()));
  }

  private void setActivityTypeWiki() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<html><body>");
    buffer.append("<a>");
    buffer.append(activityInfo.getUserName());
    buffer.append(" ");
    buffer.append("</a>");
    buffer.append("<font color=\"#696969\">");
    String act_key = activityInfo.templateParams.get("act_key");
    String act_key_des = null;
    if (act_key.equalsIgnoreCase("update_page")) {
      act_key_des = LocalizationHelper.getInstance().getString("HasEditWikiPage");
      buffer.append(act_key_des);
    } else if (act_key.equalsIgnoreCase("add_page")) {
      act_key_des = LocalizationHelper.getInstance().getString("HasCreatWikiPage");
      buffer.append(act_key_des);
    }
    buffer.append("</font>");
    buffer.append(" ");
    String page_name = activityInfo.templateParams.get("page_name");
    buffer.append("<a>");
    buffer.append(page_name);
    buffer.append("</a>");
    buffer.append("</body></html>");
    textViewName.setText(Html.fromHtml(buffer.toString()));
  }

  private void displayAttachImage(String url) {
    if (attachStubView == null) {
      initAttachStubView(url);
    }
    attachStubView.setVisibility(View.VISIBLE);
  }

  private void initAttachStubView(final String url) {
    attachStubView = ((ViewStub) findViewById(R.id.attached_image_stub_activity)).inflate();
    ImageView attachImage = (ImageView) attachStubView.findViewById(R.id.attached_image_view);
    if (SocialDetailHelper.getInstance().taskIsFinish = true) {
      SocialDetailHelper.getInstance().imageDownloader.download(url,
                                                                attachImage,
                                                                ExoConnectionUtils._strCookie);
    }

  }

  public Button likeButton() {
    return buttonLike;
  }

  public Button commentButton() {
    return buttonComment;
  }
}
