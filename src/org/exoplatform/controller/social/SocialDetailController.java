package org.exoplatform.controller.social;

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
import org.exoplatform.widget.SocialActivityStreamItem;
import org.exoplatform.widget.SocialDetailWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SocialDetailController {
  private SocialDetailWaitingDialog _progressDialog;

  private Context                   mContext;

  private LinearLayout              commentLayoutWrap;

  private Button                    likeButton;

  private LinearLayout              contentDetailLayout;

  private TextView                  textView_Like_Count;

  private int                       likeDrawable    = R.drawable.activity_like_button_background_shape;

  private int                       disLikeDrawable = R.drawable.activity_dislike_button_background_shape;

  private SocialDetailLoadTask      mLoadTask;

  private String                    activityId;

  private String                    okString;

  private String                    titleString;

  private String                    likeErrorString;

  public SocialDetailController(Context context,
                                LinearLayout layoutWrap,
                                Button likeButton,
                                LinearLayout detailLayout,
                                TextView textView_Like_Count,
                                SocialDetailWaitingDialog dialog) {
    mContext = context;
    commentLayoutWrap = layoutWrap;
    this.likeButton = likeButton;
    this.textView_Like_Count = textView_Like_Count;
    contentDetailLayout = detailLayout;
    activityId = SocialDetailHelper.getInstance().getActivityId();
    _progressDialog = dialog;
    changeLanguage();
  }

  public void onLoad(boolean isLikeAction) {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mLoadTask == null || mLoadTask.getStatus() == SocialDetailLoadTask.Status.FINISHED) {
        mLoadTask = (SocialDetailLoadTask) new SocialDetailLoadTask(mContext, this, _progressDialog).execute(isLikeAction);
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
    textView_Like_Count.setText(SocialActivityUtil.getCommentString(mContext, likeLinkedList));
  }

  public void onLikePress() {
    boolean liked = SocialDetailHelper.getInstance().getLiked();
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
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
        onLoad(true);
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
