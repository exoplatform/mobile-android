package org.exoplatform.controller.social;

import greendroid.widget.AsyncImageView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.exoplatform.model.SocialCommentInfo;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.CommentItemLayout;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.text.Html;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SocialDetailController {
  private Context              mContext;

  private LinearLayout         commentLayoutWrap;

  private Button               likeButton;

  private AsyncImageView       imageView_Avatar;

  private TextView             textView_Name;

  private TextView             textView_Message;

  private TextView             textView_Time;

  private TextView             textView_Like_Count;

  private int                  likeDrawable    = R.drawable.activity_like_button_background_shape;

  private int                  disLikeDrawable = R.drawable.activity_dislike_button_background_shape;

  private SocialDetailLoadTask mLoadTask;

  private String               activityId;

  private String               okString;

  private String               titleString;


  public SocialDetailController(Context context,
                                LinearLayout layoutWrap,
                                Button likeButton,
                                AsyncImageView imageView_Avatar,
                                TextView textView_Name,
                                TextView textView_Message,
                                TextView textView_Time,
                                TextView textView_Like_Count) {
    mContext = context;
    commentLayoutWrap = layoutWrap;
    this.likeButton = likeButton;
    this.imageView_Avatar = imageView_Avatar;
    this.textView_Name = textView_Name;
    this.textView_Message = textView_Message;
    this.textView_Time = textView_Time;
    this.textView_Like_Count = textView_Like_Count;
    activityId = SocialDetailHelper.getInstance().getActivityId();
    changeLanguage();
  }

  public void onLoad() {
    if (mLoadTask == null || mLoadTask.getStatus() == SocialDetailLoadTask.Status.FINISHED) {
      mLoadTask = (SocialDetailLoadTask) new SocialDetailLoadTask(mContext, this).execute();
    }
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == SocialDetailLoadTask.Status.RUNNING) {
      mLoadTask.onCancelled();
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  public void createCommentList(ArrayList<SocialCommentInfo> commentList) {
    if (commentList != null) {

      LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
      commentLayoutWrap.removeAllViews();
      for (int i = 0; i < commentList.size(); i++) {
        SocialCommentInfo comment = commentList.get(i);
        CommentItemLayout commentItem = new CommentItemLayout(mContext);
        String avatarUrl = comment.getImageUrl();
        if (avatarUrl == null) {
          commentItem.comAvatarImage.setImageResource(ExoConstants.DEFAULT_AVATAR);
        } else
          commentItem.comAvatarImage.setUrl(avatarUrl);
        try {
          String commentName = comment.getCommentName();
          if (commentName != null) {
            String userName = new String(commentName.getBytes("ISO-8859-1"), "UTF-8");
            commentItem.comTextViewName.setText(userName);
          }

        } catch (UnsupportedEncodingException e) {
        }
        commentItem.comTextViewMessage.setText(Html.fromHtml(comment.getCommentTitle()),
                                               TextView.BufferType.SPANNABLE);
        SocialActivityUtil.setTextLinkfy(mContext, commentItem.comTextViewMessage);
        commentItem.comPostedTime.setText(SocialActivityUtil.getPostedTimeString(comment.getPostedTime()));
        commentLayoutWrap.addView(commentItem, params);

      }
    }

  }

  public void setComponentInfo(RestProfile profile, String title, long postedTime) {
    boolean liked = SocialDetailHelper.getInstance().getLiked();
    if (liked) {
      likeButton.setBackgroundResource(disLikeDrawable);
    } else
      likeButton.setBackgroundResource(likeDrawable);
    String avatarUrl = profile.getAvatarUrl();
    if (avatarUrl == null) {
      imageView_Avatar.setImageResource(ExoConstants.DEFAULT_AVATAR);
    } else
      imageView_Avatar.setUrl(avatarUrl);

    try {
      String userName = new String(profile.getFullName().getBytes("ISO-8859-1"), "UTF-8");
      textView_Name.setText(userName);
    } catch (UnsupportedEncodingException e) {

    }

    textView_Message.setText(Html.fromHtml(title), TextView.BufferType.SPANNABLE);
    SocialActivityUtil.setTextLinkfy(mContext, textView_Message);
    textView_Time.setText(SocialActivityUtil.getPostedTimeString(postedTime));
    String attachedUrl = SocialDetailHelper.getInstance().getAttachedImageUrl();
    if (attachedUrl != null) {
      SocialDetailActivity.socialDetailActivity.displayAttachImage(attachedUrl);
    }
  }

  public void setLikeInfo(LinkedList<SocialLikeInfo> likeLinkedList) {
    textView_Like_Count.setText(SocialActivityUtil.getCommentString(likeLinkedList));
  }

  public void onLikePress() {
    boolean liked = SocialDetailHelper.getInstance().getLiked();
    try {
      RestActivity activity = SocialServiceHelper.getInstance()
                                                 .getActivityService()
                                                 .get(activityId);

      if (liked == true) {
        SocialServiceHelper.getInstance().getActivityService().unlike(activity);
        SocialDetailHelper.getInstance().setLiked(false);
      } else {
        SocialServiceHelper.getInstance().getActivityService().like(activity);
      }
      onLoad();
    } catch (RuntimeException e) {
      WarningDialog dialog = new WarningDialog(mContext, titleString, e.toString(), okString);
      dialog.show();
    }
  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    okString = location.getString("OK");
    titleString = location.getString("Warning");

  }
}
