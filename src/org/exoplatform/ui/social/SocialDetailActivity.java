package org.exoplatform.ui.social;

import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
//import greendroid.widget.ActionBarItem;
//import greendroid.widget.ActionBarItem.Type;
//import greendroid.widget.LoaderActionBarItem;

import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import org.exoplatform.R;
import org.exoplatform.controller.social.LikeLoadTask;
import org.exoplatform.controller.social.SocialDetailController;
import org.exoplatform.controller.social.SocialDetailLoadTask;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.model.SocialCommentInfo;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.ui.setting.SettingActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
//import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.CommentItemLayout;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.ShaderImageView;
import org.exoplatform.widget.SocialActivityStreamItem;
import org.exoplatform.widget.SocialDetailsWarningDialog;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Screen for activity details
 */
public class SocialDetailActivity
    //extends MyActionBar
    extends ActionBarActivity
    implements OnClickListener, SocialDetailLoadTask.AsyncTaskListener, LikeLoadTask.AsyncTaskListener {

  public LinearLayout                startScreen;

  private View                       emptyCommentStubView;

  private LinearLayout               commentLayoutWrap;

  private EditText                   editTextComment;

  private LinearLayout               mContentDetailLayout;

  private LinearLayout               mLikedLayout;

  private RelativeLayout             likedFrame;

  private TextView                   textView_Like_Count;

  private Button                     likeButton;

  private String                     yourCommentText;

  private String                     commentEmptyString;

  //private SocialDetailController     detailController;

  public static SocialDetailActivity socialDetailActivity;

  //private LoaderActionBarItem        loaderItem;

  private int                        mActivityPosition;

  private SocialDetailLoadTask mLoadTask;

  private LikeLoadTask mLikeLoadTask;

  private LinkedList<SocialLikeInfo> mLikeList;

  private Menu mOptionsMenu;

  private static final String TAG = "eXo____SocialDetailActivity____";


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //setActionBarContentView(R.layout.activity_display_view);
    setContentView(R.layout.activity_display_view);

    //getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    //addActionBarItem(Type.Refresh);
    //getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);


    socialDetailActivity = this;
    mActivityPosition = getIntent().getIntExtra(ExoConstants.ACTIVITY_CURRENT_POSITION, mActivityPosition);
    changeLanguage();
    if (savedInstanceState != null)
      finish();
    else
      initComponent();

  }

  private void initComponent() {

    startScreen = (LinearLayout) findViewById(R.id.details_start_screen);
    commentLayoutWrap = (LinearLayout) findViewById(R.id.activity_display_comment_wrap);

    mContentDetailLayout = (LinearLayout) findViewById(R.id.social_detail_wrap_layout);
    mLikedLayout = (LinearLayout) findViewById(R.id.social_detail_like_wrap);
    textView_Like_Count = (TextView) findViewById(R.id.textView_Like_Count);
    editTextComment = (EditText) findViewById(R.id.editText_Comment);
    editTextComment.setHint(yourCommentText);
    // uses this instead of deprecated android:editable="false"
    editTextComment.setKeyListener(null);
    editTextComment.setOnClickListener(this);
    likeButton = (Button) findViewById(R.id.like_button);
    likeButton.setOnClickListener(this);
    likedFrame = (RelativeLayout) findViewById(R.id.detail_likers_layout_warpper);
    likedFrame.setOnClickListener(this);
    //onLoad();

    startLoadingActivityData();
  }



  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.i(TAG, "onCreateOptionsMenu");
    getMenuInflater().inflate(R.menu.social, menu);
    mOptionsMenu = menu;
    menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
    menu.findItem(R.id.menu_add).setVisible(false);
    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_refresh:
        startLoadingActivityData();
        return true;

      case R.id.menu_settings:
        redirectToSetting();
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Change state of refresh icon on action bar
   *
   * @param refreshing
   */
  public void setRefreshActionButtonState(boolean refreshing) {
    Log.i(TAG, "setRefreshActionButtonState: " + refreshing);

    if (mOptionsMenu == null) return ;
    final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
    Log.i(TAG, "setRefreshActionButtonState - refreshItem: " + refreshItem);
    if (refreshItem == null) return ;

    //boolean currentState = refreshItem.getActionView() != null;

    if (refreshing)
      refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
    else {
      refreshItem.setActionView(null);
      //supportInvalidateOptionsMenu();
    }
  }

  public void onLoad() {
    //detailController = new SocialDetailController(this, commentLayoutWrap, mLikedLayout,
    //                                              likeButton, contentDetailLayout, textView_Like_Count);

    //loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);

    //detailController.onLoad(loaderItem, false, currentPosition);
  }

  // replace onLoad of SocialDetailController
  public void startLoadingActivityData() {
    if (!ExoConnectionUtils.isNetworkAvailableExt(this)) {
      new ConnectionErrorDialog(this).show();
      return ;
    }

    if (mLoadTask == null || mLoadTask.getStatus() == SocialDetailLoadTask.Status.FINISHED) {
      setRefreshActionButtonState(true);
      mLoadTask = new SocialDetailLoadTask(this, mActivityPosition);
      mLoadTask.setListener(this);
      mLoadTask.execute(false);
    }
  }


  @Override
  public void onLoadingLikeListFinished(LinkedList<SocialLikeInfo> result) {
    setRefreshActionButtonState(false);

    if (result == null) {
      new SocialDetailsWarningDialog(this, getString(R.string.Warning), getString(R.string.DetailsNotAvaiable),
          getString(R.string.OK), false).show();
      return ;
    }

    setLike();
    mLikeList = result;
    setLikedInfo();
  }

  private void setLike() {
    boolean liked = SocialDetailHelper.getInstance().getLiked();
    if (liked) likeButton.setBackgroundResource(R.drawable.activity_dislike_button_background_shape);
    else
      likeButton.setBackgroundResource(R.drawable.activity_like_button_background_shape);
  }

  @Override
  public void onLoadingActivityFinished(int result, SocialActivityInfo activityInfo,
                                        ArrayList<SocialCommentInfo> socialCommentList,
                                        LinkedList<SocialLikeInfo> likeLinkedList) {

    setRefreshActionButtonState(false);

    if (result != 1) {
      new SocialDetailsWarningDialog(this, getString(R.string.Warning),
          getString(R.string.DetailsNotAvaiable), getString(R.string.OK), false).show();
      SocialDetailActivity.socialDetailActivity.startScreen.setVisibility(View.GONE);
      return ;
    }

    setComponentInfo(activityInfo);
    createCommentList(socialCommentList);
    mLikeList = likeLinkedList;
    setLikedInfo();

    startScreen.setVisibility(View.GONE);
  }


  public void setComponentInfo(SocialActivityInfo streamInfo) {
    setLike();

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT);
    mContentDetailLayout.removeAllViews();
    SocialActivityStreamItem item = new SocialActivityStreamItem(this, streamInfo, true);
    SocialActivityUtil.setTextLinkfy(item.textViewMessage);
    SocialActivityUtil.setTextLinkfy(item.textViewName);
    SocialActivityUtil.setTextLinkfy(item.textViewTempMessage);
    mContentDetailLayout.addView(item, params);
  }


  public void createCommentList(ArrayList<SocialCommentInfo> commentList) {
    int commentListSize = commentList.size();
    if (commentListSize > 0) {
      SocialDetailActivity.socialDetailActivity.setEmptyView(View.GONE);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.WRAP_CONTENT);
      commentLayoutWrap.removeAllViews();
      for (int i = 0; i < commentListSize; i++) {
        SocialCommentInfo comment = commentList.get(i);
        CommentItemLayout commentItem = new CommentItemLayout(this);
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
        commentItem.comPostedTime.setText(SocialActivityUtil.getPostedTimeString(this,
            comment.getPostedTime()));
        commentLayoutWrap.addView(commentItem, params);

      }
    } else {
      SocialDetailActivity.socialDetailActivity.setEmptyView(View.VISIBLE);
    }

  }

  /*
 * Call this method when click on the liked frame
 */
  public void onClickLikedFrame() {
    Intent intent = new Intent(this, LikeListActivity.class);
    /*
     * put liked list intent extra to LikeListActivity
     */
    intent.putParcelableArrayListExtra(ExoConstants.SOCIAL_LIKED_LIST_EXTRA, new ArrayList<SocialLikeInfo>(mLikeList));
    this.startActivity(intent);
  }

  /*
   * The liker information image view
   */

  public void setLikedInfo() {
    textView_Like_Count.setText(SocialActivityUtil.getComment(this, mLikeList));

    /**
    int size = likeLinkedList.size();
    ArrayList<SocialLikeInfo> likeList = new ArrayList<SocialLikeInfo>();
    for (SocialLikeInfo item : likeLinkedList) {
      likeList.add(item);
    }
     **/

    /** We only display maximum 4 likers at the detail screen */
    int maxChild = (mLikeList.size() > 4) ? 4 : mLikeList.size();

    /** Set list of likers */
    mLikedLayout.removeAllViews();
    int likedAvatarSize = getResources().getDimensionPixelSize(R.dimen.social_liked_avatar_size);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(likedAvatarSize, likedAvatarSize);
    params.setMargins(5, 0, 0, 0);

    ShaderImageView likedAvatar;
    for (int i = 0; i < maxChild; i++) {
      likedAvatar = new ShaderImageView(this, true);
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = 4;
      options.inPurgeable  = true;
      options.inInputShareable = true;
      likedAvatar.setOptions(options);
      likedAvatar.setDefaultImageResource(R.drawable.default_avatar);
      likedAvatar.setUrl(mLikeList.get(i).likedImageUrl);
      mLikedLayout.addView(likedAvatar, params);
    }

    /** If have more than 4 likers, we put a "more_likers" image icon at the last */
    if (mLikeList.size() > 4) {
      likedAvatar = new ShaderImageView(this, true);
      likedAvatar.setDefaultImageDrawable(getResources().getDrawable(R.drawable.activity_detail_more_likers));
      mLikedLayout.addView(likedAvatar, params);
    }
  }

  @Override
  public void finish() {
    //if (detailController != null) {
    //  detailController.onCancelLoad();
    //}
    super.finish();
  }

  @Override
  public void onBackPressed() {
    finish();
  }


  /** TODO replace
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      if (SocialTabsActivity.instance != null) {
        SocialTabsActivity.instance.finish();
      }
      finish();
      break;
    case 0:
      loaderItem = (LoaderActionBarItem) item;
      detailController.onLoad(loaderItem, false, currentPosition);
      break;

    }

    return true;
  }
  **/

  @Override
  public void onClick(View view) {
    if (view.equals(editTextComment)) {
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_COMMENT_TYPE);
      intent.putExtra(ExoConstants.ACTIVITY_CURRENT_POSITION, mActivityPosition);
      startActivity(intent);
    }
    if (view.equals(likeButton)) {
      onLikePress(mActivityPosition);
    }

    if (view.equals(likedFrame)) {
      onClickLikedFrame();
    }
  }


  /** When user click on like button, only update the liker part UI */
  public void onLikePress(int pos) {
    onLikeLoad(SocialDetailHelper.getInstance().getActivityId(), pos);
  }

  public void onLikeLoad(String id, int position) {
    if (!ExoConnectionUtils.isNetworkAvailableExt(this)) {
      new ConnectionErrorDialog(this).show();
      return ;
    }

    if (mLikeLoadTask == null || mLikeLoadTask.getStatus() == LikeLoadTask.Status.FINISHED) {
      mLikeLoadTask = (LikeLoadTask) new LikeLoadTask(this, position).execute(id);
    }
  }

  public void setEmptyView(int status) {
    if (emptyCommentStubView == null) {
      initStubView();
    }
    emptyCommentStubView.setVisibility(status);
  }

  private void initStubView() {
    emptyCommentStubView = ((ViewStub) findViewById(R.id.comment_details_empty_stub)).inflate();
    ImageView emptyImage = (ImageView) emptyCommentStubView.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_no_comment);
    TextView emptyStatus = (TextView) emptyCommentStubView.findViewById(R.id.empty_status);
    emptyStatus.setTextSize(14);
    emptyStatus.setText(commentEmptyString);
  }

  private void changeLanguage() {
    Resources resource = getResources();
    String strTitle = resource.getString(R.string.ActivityDetail);
    setTitle(strTitle);
    yourCommentText = resource.getString(R.string.YourComment);
    commentEmptyString = resource.getString(R.string.EmptyComment);
  }

  private void redirectToSetting() {
    Intent next = new Intent(this, SettingActivity.class);
    next.putExtra(ExoConstants.SETTING_TYPE, SettingActivity.PERSONAL_TYPE);
    startActivity(next);
  }
}
