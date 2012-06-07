package org.exoplatform.controller.social;

import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;
import java.util.LinkedList;

import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.model.SocialCommentInfo;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.ui.social.LikeListActivity;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.CommentItemLayout;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.RoundedImageView;
import org.exoplatform.widget.SocialActivityStreamItem;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SocialDetailController {

  private Context                   mContext;

  private LinearLayout              commentLayoutWrap;

  private Button                    likeButton;

  private LinearLayout              contentDetailLayout;

  private LinearLayout              likedLayoutWrap;

  private TextView                  textView_Like_Count;

  private int                       likeDrawable    = R.drawable.activity_like_button_background_shape;

  private int                       disLikeDrawable = R.drawable.activity_dislike_button_background_shape;

  private SocialDetailLoadTask      mLoadTask;

  private LikeLoadTask              mLikeLoadTask;

  private String                    activityId;

  private int                       likedAvatarSize;

  private ArrayList<SocialLikeInfo> likeList;

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
    onCancelLikeLoad();
    if (mLoadTask != null && mLoadTask.getStatus() == SocialDetailLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  public void onLikeLoad(LoaderActionBarItem loader, String id) {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mLikeLoadTask == null || mLikeLoadTask.getStatus() == LikeLoadTask.Status.FINISHED) {
        mLikeLoadTask = (LikeLoadTask) new LikeLoadTask(mContext, this, loader).execute(id);
      }
    } else {
      new ConnectionErrorDialog(mContext).show();
    }
  }

  public void onCancelLikeLoad() {
    if (mLikeLoadTask != null && mLikeLoadTask.getStatus() == LikeLoadTask.Status.RUNNING) {
      mLikeLoadTask.cancel(true);
      mLikeLoadTask = null;
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

  public void setLikedState() {
    boolean liked = SocialDetailHelper.getInstance().getLiked();
    if (liked) {
      likeButton.setBackgroundResource(disLikeDrawable);
    } else
      likeButton.setBackgroundResource(likeDrawable);
  }

  public void setComponentInfo(SocialActivityInfo streamInfo) {
    setLikedState();
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    contentDetailLayout.removeAllViews();
    SocialActivityStreamItem item = new SocialActivityStreamItem(mContext, streamInfo, true);
    SocialActivityUtil.setTextLinkfy(item.textViewMessage);
    SocialActivityUtil.setTextLinkfy(item.textViewName);
    SocialActivityUtil.setTextLinkfy(item.textViewTempMessage);
    contentDetailLayout.addView(item, params);
  }

  /*
   * The liker information text view
   */
  public void setLikeInfoText(LinkedList<SocialLikeInfo> likeLinkedList) {
    textView_Like_Count.setText(SocialActivityUtil.getComment(mContext, likeLinkedList));
  }

  /*
   * The liker information image view
   */

  public void setLikedInfo(LinkedList<SocialLikeInfo> likeLinkedList) {
    int size = likeLinkedList.size();
    likeList = new ArrayList<SocialLikeInfo>();
    for (SocialLikeInfo item : likeLinkedList) {
      likeList.add(item);
    }
    /*
     * We only display maximum 4 likers at the detail screen
     */
    int maxChild = 0;
    if (size > 4) {
      maxChild = 4;
    } else
      maxChild = size;
    /*
     * Set list of likers
     */
    likedLayoutWrap.removeAllViews();
    likedAvatarSize = mContext.getResources()
                              .getDimensionPixelSize(org.exoplatform.R.dimen.social_liked_avatar_size);
    LayoutParams params = new LayoutParams(likedAvatarSize, likedAvatarSize);
    params.setMargins(5, 0, 0, 0);
    LikedAvatarItem likedAvatar;
    for (int i = 0; i < maxChild; i++) {
      likedAvatar = new LikedAvatarItem(mContext);
      likedAvatar.avatarImage.setUrl(likeLinkedList.get(i).likedImageUrl);
      likedLayoutWrap.addView(likedAvatar, params);
    }
    /*
     * If have more than 4 likers, we put a "more_likers" image icon at the last
     */
    if (size > 4) {
      RoundedImageView image = new RoundedImageView(mContext);
      image.setDefaultImageResource(R.drawable.activity_detail_more_likers);
      likedLayoutWrap.addView(image, params);
    }

  }

  /*
   * Call this method when click on the liked frame
   */
  public void onClickLikedFrame() {
    Intent intent = new Intent(mContext, LikeListActivity.class);
    /*
     * put liked list intent extra to LikeListActivity
     */
    intent.putParcelableArrayListExtra(ExoConstants.SOCIAL_LIKED_LIST_EXTRA, likeList);
    mContext.startActivity(intent);
  }

  /*
   * When user click on like button, only update the liker part UI
   */
  public void onLikePress(LoaderActionBarItem loader) {
    onLikeLoad(loader, activityId);
  }

  /*
   * The Layout class for displaying the liker avatar and liker name
   */
  private class LikedAvatarItem extends RelativeLayout {
    public RoundedImageView avatarImage;

    public LikedAvatarItem(Context context) {
      super(context);
      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.detail_liked_item, this);
      avatarImage = (RoundedImageView) view.findViewById(R.id.detail_liked_image);
      avatarImage.setDefaultImageResource(R.drawable.default_avatar);
    }

  }
}
