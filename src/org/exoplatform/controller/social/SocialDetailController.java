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
package org.exoplatform.controller.social;

import java.util.ArrayList;
import java.util.LinkedList;

import com.squareup.picasso.Picasso;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.model.SocialCommentInfo;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.ui.social.LikeListActivity;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoUtils;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.image.ExoPicasso;
import org.exoplatform.utils.image.PicassoImageGetter;
import org.exoplatform.utils.image.RoundedCornersTranformer;
import org.exoplatform.widget.CommentItemLayout;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.SocialActivityDetailsItem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

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

  private MenuItem                  loaderItem;

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

  public void setLoaderItem(MenuItem item) {
    this.loaderItem = item;
  }

  public void setLoading(boolean loading) {
    ExoUtils.setLoadingItem(loaderItem, loading);
  }

  public void onLoad(boolean isLikeAction, int position) {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mLoadTask == null || mLoadTask.getStatus() == SocialDetailLoadTask.Status.FINISHED) {
        mLoadTask = (SocialDetailLoadTask) new SocialDetailLoadTask(mContext, this, position).execute(isLikeAction);
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

  public void onLikeLoad(String id, int position) {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mLikeLoadTask == null || mLikeLoadTask.getStatus() == LikeLoadTask.Status.FINISHED) {
        mLikeLoadTask = (LikeLoadTask) new LikeLoadTask(mContext, this, position).execute(id);
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
      LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
      commentLayoutWrap.removeAllViews();
      for (int i = 0; i < commentListSize; i++) {
        SocialCommentInfo comment = commentList.get(i);
        CommentItemLayout commentItem = new CommentItemLayout(mContext);
        String avatarUrl = comment.getImageUrl();
        if (avatarUrl == null) {
          commentItem.comAvatarImage.setImageResource(ExoConstants.DEFAULT_AVATAR);
        } else {
          ExoPicasso.picasso(mContext)
                    .load(Uri.parse(avatarUrl))
                    .transform(new RoundedCornersTranformer(mContext))
                    .into(commentItem.comAvatarImage);
        }
        String commentName = comment.getCommentName();
        commentItem.comTextViewName.setText(commentName);

        commentItem.comTextViewMessage.setText(Html.fromHtml(comment.getCommentTitle(),
                                                             new PicassoImageGetter(commentItem.comTextViewMessage),
                                                             null),
                                               TextView.BufferType.SPANNABLE);
        SocialActivityUtil.setTextLinkify(commentItem.comTextViewMessage);
        commentItem.comPostedTime.setText(SocialActivityUtil.getPostedTimeString(mContext, comment.getPostedTime()));
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
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    contentDetailLayout.removeAllViews();
    SocialActivityDetailsItem item = new SocialActivityDetailsItem(mContext, streamInfo, true);
    SocialActivityUtil.setTextLinkify(item.textViewMessage);
    SocialActivityUtil.setTextLinkify(item.textViewName);
    SocialActivityUtil.setTextLinkify(item.textViewTempMessage);
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
    int maxChild = (size > 4) ? 4 : size;
    /*
     * Set list of likers
     */
    likedLayoutWrap.removeAllViews();
    likedAvatarSize = mContext.getResources().getDimensionPixelSize(R.dimen.social_liked_avatar_size);
    LayoutParams params = new LayoutParams(likedAvatarSize, likedAvatarSize);
    params.setMargins(5, 0, 0, 0);
    ImageView likedAvatar;
    for (int i = 0; i < maxChild; i++) {
      likedAvatar = new ImageView(mContext);
      ExoPicasso.picasso(mContext)
                .load(Uri.parse(likeLinkedList.get(i).likedImageUrl))
                .placeholder(R.drawable.default_avatar)
                .transform(new RoundedCornersTranformer(mContext))
                .into(likedAvatar);
      likedLayoutWrap.addView(likedAvatar, params);
    }
    /*
     * If have more than 4 likers, we put a "more_likers" image icon at the last
     */
    if (size > 4) {
      likedAvatar = new ImageView(mContext);
      Picasso.with(mContext)
             .load(R.drawable.activity_detail_more_likers)
             .transform(new RoundedCornersTranformer(mContext))
             .into(likedAvatar);
      likedLayoutWrap.addView(likedAvatar, params);
    }
    likedLayoutWrap.requestLayout();
  }

  /*
   * Call this method when click on the likes frame
   */
  public void onClickLikesFrame() {
    Intent intent = new Intent(mContext, LikeListActivity.class);
    /*
     * put like list intent extra to LikeListActivity
     */
    intent.putParcelableArrayListExtra(ExoConstants.SOCIAL_LIKED_LIST_EXTRA, likeList);
    mContext.startActivity(intent);
  }

  /*
   * When user click on like button, only update the liker part UI
   */
  public void onLikePress(int pos) {
    onLikeLoad(activityId, pos);
  }
}
