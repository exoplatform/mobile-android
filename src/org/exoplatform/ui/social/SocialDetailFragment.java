package org.exoplatform.ui.social;

import java.util.ArrayList;
import java.util.LinkedList;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.exoplatform.R;
import org.exoplatform.controller.social.LikeLoadTask;
import org.exoplatform.controller.social.SocialDetailLoadTask;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.model.SocialCommentInfo;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.CommentItemLayout;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.ShaderImageView;
import org.exoplatform.widget.SocialActivityStreamItem;
import org.exoplatform.widget.SocialDetailsWarningDialog;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 23, 2012
 */
public class SocialDetailFragment extends Fragment implements View.OnClickListener,
    LikeLoadTask.AsyncTaskListener, SocialDetailLoadTask.AsyncTaskListener {

  public LinearLayout                mStartScreen;

  private View                       mEmptyCommentStub;

  private LinearLayout               mCommentListSection;

  private EditText                   mAddCommentEditTxt;

  private LinearLayout               mSocialActivityTitle;

  private LinearLayout               mPeopleLikeThisAvatar;

  private RelativeLayout             mPeopleLikeThisSection;

  private TextView                   mLikeCountTxt;

  private Button                     mLikeBtn;

  private SocialDetailLoadTask       mLoadTask;

  private LikeLoadTask               mLikeLoadTask;

  private LinkedList<SocialLikeInfo> mLikeList;

  private int                        mActivityPosition;

  private View                       mRootView;

  private Refreshable                mRefreshListener;


  private static final String TAG = "eXo____SocialDetailFragment____";

  public SocialDetailFragment() {}

  public SocialDetailFragment(int activityPosition) {
    mActivityPosition = activityPosition;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.i(TAG, "onCreateView");
    mRootView = inflater.inflate(R.layout.activity_details_fragment, container, false);
    return mRootView;
  }


  public void setActivityPosition(int position) {
    mActivityPosition = position;
  }


  private void initComponent() {
    Log.i(TAG, "initComponent");

    mStartScreen          = (LinearLayout) mRootView.findViewById(R.id.details_start_screen);
    mCommentListSection   = (LinearLayout) mRootView.findViewById(R.id.details_comment_list_section);
    mSocialActivityTitle  = (LinearLayout) mRootView.findViewById(R.id.details_activity_title);
    mPeopleLikeThisAvatar = (LinearLayout) mRootView.findViewById(R.id.details_people_like_this_avatar);
    mLikeCountTxt         = (TextView) mRootView.findViewById(R.id.details_like_count_txt);

    mAddCommentEditTxt = (EditText) mRootView.findViewById(R.id.details_add_comment_edit_txt);
    mAddCommentEditTxt.setHint(getString(R.string.YourComment));
    mAddCommentEditTxt.setKeyListener(null);
    mAddCommentEditTxt.setOnClickListener(this);

    mLikeBtn = (Button) mRootView.findViewById(R.id.details_like_btn);
    mLikeBtn.setOnClickListener(this);

    mPeopleLikeThisSection = (RelativeLayout) mRootView.findViewById(R.id.details_people_like_this_section);
    mPeopleLikeThisSection.setOnClickListener(this);

    startLoadingActivityData();
  }


  public void setRefreshListener(Refreshable listener) {
    mRefreshListener = listener;
  }

  public void startLoadingActivityData() {
    if (!ExoConnectionUtils.isNetworkAvailableExt(getActivity())) {
      new ConnectionErrorDialog(getActivity()).show();
      return ;
    }

    if (mLoadTask == null || mLoadTask.getStatus() == SocialDetailLoadTask.Status.FINISHED) {
      if (mRefreshListener != null) mRefreshListener.setRefreshActionButtonState(true);
      mLoadTask = new SocialDetailLoadTask(getActivity(), mActivityPosition);
      mLoadTask.setListener(this);
      mLoadTask.execute(false);
    }
  }

  @Override
  public void onClick(View view) {
    if (view.equals(mAddCommentEditTxt)) {
      Intent intent = new Intent(getActivity(), ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_COMMENT_TYPE);
      intent.putExtra(ExoConstants.ACTIVITY_CURRENT_POSITION, mActivityPosition);
      startActivity(intent);
    }

    if (view.equals(mLikeBtn)) {
      onLikePress(mActivityPosition);
    }

    if (view.equals(mPeopleLikeThisSection)) {
      onClickLikedFrame();
    }
  }


  /** When user click on like button, only update the liker part UI */
  public void onLikePress(int pos) {
    onLikeLoad(SocialDetailHelper.getInstance().getActivityId(), pos);
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() != LikeLoadTask.Status.FINISHED) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }

    if (mLikeLoadTask != null && mLikeLoadTask.getStatus() == LikeLoadTask.Status.FINISHED) {
      mLikeLoadTask.cancel(true);
      mLikeLoadTask = null;
    }
  }

  /**
   * Loading like information
   * @param id
   * @param position
   */
  public void onLikeLoad(String id, int position) {
    Log.i(TAG, "onLikeLoad - id: " + id + " - pos: " + position);
    if (!ExoConnectionUtils.isNetworkAvailableExt(getActivity())) {
      new ConnectionErrorDialog(getActivity()).show();
      return ;
    }

    if (mLikeLoadTask == null || mLikeLoadTask.getStatus() == LikeLoadTask.Status.FINISHED) {
      if (mRefreshListener != null) mRefreshListener.setRefreshActionButtonState(true);
      mLikeLoadTask = new LikeLoadTask(getActivity(), position);
      mLikeLoadTask.setListener(this);
      mLikeLoadTask.execute(id);
    }
  }


  @Override
  public void onLoadingLikeListFinished(LinkedList<SocialLikeInfo> result) {
    Log.i(TAG, "onLoadingLikeListFinished - result " + result);
    if (mRefreshListener != null) mRefreshListener.setRefreshActionButtonState(false);

    if (result == null) {
      new SocialDetailsWarningDialog(getActivity(), getString(R.string.Warning), getString(R.string.DetailsNotAvaiable),
          getString(R.string.OK), false).show();
      return ;
    }

    setLike();
    mLikeList = result;
    setLikedInfo();
  }

  public void setEmptyComment(int status) {
    if (mEmptyCommentStub == null) {
      initEmptyCommentStub();
    }
    mEmptyCommentStub.setVisibility(status);
  }

  private void initEmptyCommentStub() {
    mEmptyCommentStub = ((ViewStub) getView().findViewById(R.id.details_empty_comment)).inflate();
    ImageView emptyImage = (ImageView) mEmptyCommentStub.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_no_comment);
    TextView emptyStatus = (TextView) mEmptyCommentStub.findViewById(R.id.empty_status);
    emptyStatus.setTextSize(14);
    emptyStatus.setText(getString(R.string.EmptyComment));
  }




  private void setLike() {
    boolean liked = SocialDetailHelper.getInstance().getLiked();
    if (liked) mLikeBtn.setBackgroundResource(R.drawable.activity_dislike_button_background_shape);
    else
      mLikeBtn.setBackgroundResource(R.drawable.activity_like_button_background_shape);
  }

  @Override
  public void onLoadingActivityFinished(int result, SocialActivityInfo activityInfo,
                                        ArrayList<SocialCommentInfo> socialCommentList,
                                        LinkedList<SocialLikeInfo> likeLinkedList) {

    if (mRefreshListener != null) mRefreshListener.setRefreshActionButtonState(false);

    if (result != 1) {
      new SocialDetailsWarningDialog(getActivity(), getString(R.string.Warning),
          getString(R.string.DetailsNotAvaiable), getString(R.string.OK), false).show();
      mStartScreen.setVisibility(View.GONE);
      return ;
    }

    setComponentInfo(activityInfo);
    createCommentList(socialCommentList);
    mLikeList = likeLinkedList;
    setLikedInfo();

    mStartScreen.setVisibility(View.GONE);
  }


  public void setComponentInfo(SocialActivityInfo streamInfo) {
    setLike();

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT);
    mSocialActivityTitle.removeAllViews();
    SocialActivityStreamItem item = new SocialActivityStreamItem(getActivity(), streamInfo, true);
    SocialActivityUtil.setTextLinkfy(item.textViewMessage);
    SocialActivityUtil.setTextLinkfy(item.textViewName);
    SocialActivityUtil.setTextLinkfy(item.textViewTempMessage);
    mSocialActivityTitle.addView(item, params);
  }


  public void createCommentList(ArrayList<SocialCommentInfo> commentList) {
    int commentListSize = commentList.size();

    if (commentListSize > 0) {
      setEmptyComment(View.GONE);

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.WRAP_CONTENT);

      mCommentListSection.removeAllViews();
      for (int i = 0; i < commentListSize; i++) {
        SocialCommentInfo comment = commentList.get(i);
        CommentItemLayout commentItem = new CommentItemLayout(getActivity());
        String avatarUrl = comment.getImageUrl();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        options.inPurgeable  = true;
        options.inInputShareable = true;
        commentItem.comAvatarImage.setOptions(options);

        if (avatarUrl == null) {
          commentItem.comAvatarImage.setImageResource(ExoConstants.DEFAULT_AVATAR);
        } else
          commentItem.comAvatarImage.setUrl(avatarUrl);
        String commentName = comment.getCommentName();
        commentItem.comTextViewName.setText(commentName);
        commentItem.comTextViewMessage.setText(Html.fromHtml(comment.getCommentTitle()),
            TextView.BufferType.SPANNABLE);
        SocialActivityUtil.setTextLinkfy(commentItem.comTextViewMessage);
        commentItem.comPostedTime.setText(SocialActivityUtil.getPostedTimeString(getActivity(),
            comment.getPostedTime()));
        mCommentListSection.addView(commentItem, params);

      }
    }
    else {
      setEmptyComment(View.VISIBLE);
    }

  }

  /**
   * Call this method when click on the people like this frame
   */
  public void onClickLikedFrame() {
    Log.i(TAG, "onClickLikedFrame");
    Intent intent = new Intent(getActivity(), LikeListActivity.class);
    /** put liked list intent extra to LikeListActivity */
    intent.putParcelableArrayListExtra(ExoConstants.SOCIAL_LIKED_LIST_EXTRA, new ArrayList<SocialLikeInfo>(mLikeList));
    this.startActivity(intent);
  }

  /**
   * The liker information image view
   */
  public void setLikedInfo() {
    mLikeCountTxt.setText(SocialActivityUtil.getComment(getActivity(), mLikeList));

    /** We only display maximum 4 likers at the detail screen */
    int maxChild = (mLikeList.size() > 4) ? 4 : mLikeList.size();

    /** Set list of likers */
    mPeopleLikeThisAvatar.removeAllViews();
    int likedAvatarSize = getResources().getDimensionPixelSize(R.dimen.social_liked_avatar_size);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(likedAvatarSize, likedAvatarSize);
    params.setMargins(5, 0, 0, 0);

    ShaderImageView likedAvatar;
    for (int i = 0; i < maxChild; i++) {
      likedAvatar = new ShaderImageView(getActivity(), true);
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = 4;
      options.inPurgeable  = true;
      options.inInputShareable = true;
      likedAvatar.setOptions(options);
      likedAvatar.setDefaultImageResource(R.drawable.default_avatar);
      likedAvatar.setUrl(mLikeList.get(i).likedImageUrl);
      mPeopleLikeThisAvatar.addView(likedAvatar, params);
    }

    /** If have more than 4 likers, we put a "more_likers" image icon at the last */
    if (mLikeList.size() > 4) {
      likedAvatar = new ShaderImageView(getActivity(), true);
      likedAvatar.setDefaultImageDrawable(getResources().getDrawable(R.drawable.activity_detail_more_likers));
      mPeopleLikeThisAvatar.addView(likedAvatar, params);
    }
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Log.i(TAG, "onActivityCreated");
    initComponent();
  }

}

