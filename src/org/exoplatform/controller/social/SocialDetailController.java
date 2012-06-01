package org.exoplatform.controller.social;

import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;
import java.util.LinkedList;

import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.model.SocialCommentInfo;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.CommentItemLayout;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.RoundedImageView;
import org.exoplatform.widget.SocialActivityStreamItem;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SocialDetailController {

  private Context              mContext;

  private LinearLayout         commentLayoutWrap;

  private Button               likeButton;

  private LinearLayout         contentDetailLayout;

  private LinearLayout         likedLayoutWrap;

  private TextView             textView_Like_Count;

  private int                  likeDrawable    = R.drawable.activity_like_button_background_shape;

  private int                  disLikeDrawable = R.drawable.activity_dislike_button_background_shape;

  private SocialDetailLoadTask mLoadTask;

  private String               activityId;

  private String               okString;

  private String               titleString;

  private String               likeErrorString;

  private int                  likedAvatarSize;

  public SocialDetailController(Context context,
                                LinearLayout layoutWrap,
                                LinearLayout likedWrap,
                                Button likeButton,
                                LinearLayout detailLayout,
                                TextView textView_Like_Count) {
    mContext = context;
    commentLayoutWrap = layoutWrap;
    likedLayoutWrap = likedWrap;
    this.likeButton = likeButton;
    this.textView_Like_Count = textView_Like_Count;
    contentDetailLayout = detailLayout;
    activityId = SocialDetailHelper.getInstance().getActivityId();
    changeLanguage();
  }

  public void onLoad(LoaderActionBarItem loader, boolean isLikeAction) {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mLoadTask == null || mLoadTask.getStatus() == SocialDetailLoadTask.Status.FINISHED) {
        mLoadTask = (SocialDetailLoadTask) new SocialDetailLoadTask(mContext, this, loader).execute(isLikeAction);
      }
    } else {
      new ConnectionErrorDialog(mContext).show();
    }
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == SocialDetailLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  public void createCommentList(ArrayList<SocialCommentInfo> commentList) {
    int commentListSize = commentList.size();
    if (commentListSize > 0) {
      SocialDetailActivity.socialDetailActivity.setEmptyView(View.GONE);
      LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
      commentLayoutWrap.removeAllViews();
      for (int i = 0; i < commentListSize; i++) {
        SocialCommentInfo comment = commentList.get(i);
        CommentItemLayout commentItem = new CommentItemLayout(mContext);
        String avatarUrl = comment.getImageUrl();
        if (avatarUrl == null) {
          commentItem.comAvatarImage.setImageResource(ExoConstants.DEFAULT_AVATAR);
        } else
          commentItem.comAvatarImage.setUrl(avatarUrl);
        String commentName = comment.getCommentName();
        commentItem.comTextViewName.setText(commentName);
        commentItem.comTextViewMessage.setText(Html.fromHtml(comment.getCommentTitle()),
                                               TextView.BufferType.SPANNABLE);
        SocialActivityUtil.setTextLinkfy(commentItem.comTextViewMessage);
        commentItem.comPostedTime.setText(SocialActivityUtil.getPostedTimeString(mContext,
                                                                                 comment.getPostedTime()));
        commentLayoutWrap.addView(commentItem, params);

      }
    } else {
      SocialDetailActivity.socialDetailActivity.setEmptyView(View.VISIBLE);
    }

  }

  public void setComponentInfo(SocialActivityInfo streamInfo) {
    boolean liked = SocialDetailHelper.getInstance().getLiked();
    if (liked) {
      likeButton.setBackgroundResource(disLikeDrawable);
    } else
      likeButton.setBackgroundResource(likeDrawable);
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    contentDetailLayout.removeAllViews();
    SocialActivityStreamItem item = new SocialActivityStreamItem(mContext, streamInfo, true);
    SocialActivityUtil.setTextLinkfy(item.textViewMessage);
    SocialActivityUtil.setTextLinkfy(item.textViewName);
    SocialActivityUtil.setTextLinkfy(item.textViewTempMessage);
    contentDetailLayout.addView(item, params);
  }

  public void setLikeInfo(LinkedList<SocialLikeInfo> likeLinkedList) {
    textView_Like_Count.setText(SocialActivityUtil.getComment(mContext, likeLinkedList));
  }

  public void setLikedInfo(LinkedList<SocialLikeInfo> likeLinkedList) {
    int size = likeLinkedList.size();
    int maxChild = 0;
    if (size > 4) {
      maxChild = 4;
    } else
      maxChild = size;
    likedLayoutWrap.removeAllViews();
    likedAvatarSize = mContext.getResources()
                              .getDimensionPixelSize(org.exoplatform.R.dimen.social_liked_avatar_size);
    LayoutParams params = new LayoutParams(likedAvatarSize, likedAvatarSize);
    params.setMargins(5, 0, 0, 0);
    RoundedImageView likedAvatar;
    for (int i = 0; i < maxChild; i++) {
      likedAvatar = new RoundedImageView(mContext);
      likedAvatar.setDefaultImageResource(R.drawable.default_avatar);
      likedAvatar.setScaleType(ScaleType.FIT_XY);
      likedAvatar.setUrl(likeLinkedList.get(i).likedImageUrl);

      likedLayoutWrap.addView(likedAvatar, params);
    }
//    if (size > 4) {
//      likedAvatar = new RoundedImageView(mContext);
//      likedAvatar.setScaleType(ScaleType.FIT_XY);
//      likedAvatar.setDefaultImageResource(R.drawable.next_icon);
//      likedLayoutWrap.addView(likedAvatar, params);
//    }

  }

  public void onLikePress(LoaderActionBarItem loader) {
    boolean liked = SocialDetailHelper.getInstance().getLiked();
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      try {
        RestActivity activity = SocialServiceHelper.getInstance().activityService.get(activityId);

        if (liked == true) {
          SocialServiceHelper.getInstance().activityService.unlike(activity);
          SocialDetailHelper.getInstance().setLiked(false);
        } else {
          SocialServiceHelper.getInstance().activityService.like(activity);
        }
        onLoad(loader, true);
      } catch (SocialClientLibException e) {

        WarningDialog dialog = new WarningDialog(mContext, titleString, likeErrorString, okString);
        dialog.show();
      }
    } else {
      new ConnectionErrorDialog(mContext).show();
    }
  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    likeErrorString = resource.getString(R.string.ErrorOnLike);
  }
}
