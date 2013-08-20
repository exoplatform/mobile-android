package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.LoaderActionBarItem;

import org.exoplatform.R;
import org.exoplatform.controller.social.SocialDetailController;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;

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

/**
 * Screen for activity details
 */
public class SocialDetailActivity extends MyActionBar implements OnClickListener {
  public LinearLayout                startScreen;

  private View                       emptyCommentStubView;

  private LinearLayout               commentLayoutWrap;

  private EditText                   editTextComment;

  private LinearLayout               contentDetailLayout;

  private LinearLayout               likedLayoutWrap;

  private RelativeLayout             likedFrame;

  private TextView                   textView_Like_Count;

  private Button                     likeButton;

  private String                     yourCommentText;

  private String                     commentEmptyString;

  private SocialDetailController     detailController;

  public static SocialDetailActivity socialDetailActivity;

  private LoaderActionBarItem        loaderItem;

  private int                        currentPosition;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.activity_display_view);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem(Type.Refresh);
    getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);
    socialDetailActivity = this;
    currentPosition = getIntent().getIntExtra(ExoConstants.ACTIVITY_CURRENT_POSITION,
                                              currentPosition);
    changeLanguage();
    if (savedInstanceState != null)
      finish();
    else
      initComponent();

  }

  private void initComponent() {

    startScreen = (LinearLayout) findViewById(R.id.details_start_screen);
    commentLayoutWrap = (LinearLayout) findViewById(R.id.activity_display_comment_wrap);

    contentDetailLayout = (LinearLayout) findViewById(R.id.social_detail_wrap_layout);
    likedLayoutWrap = (LinearLayout) findViewById(R.id.social_detail_like_wrap);
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
    onLoad();

  }

  public void onLoad() {
    detailController = new SocialDetailController(this,
                                                  commentLayoutWrap,
                                                  likedLayoutWrap,
                                                  likeButton,
                                                  contentDetailLayout,
                                                  textView_Like_Count);
    loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);
    detailController.onLoad(loaderItem, false, currentPosition);
  }

  @Override
  public void finish() {
    if (detailController != null) {
      detailController.onCancelLoad();
    }
    super.finish();
  }

  @Override
  public void onBackPressed() {
    finish();
  }

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

  @Override
  public void onClick(View view) {
    if (view.equals(editTextComment)) {
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_COMMENT_TYPE);
      intent.putExtra(ExoConstants.ACTIVITY_CURRENT_POSITION, currentPosition);
      startActivity(intent);
    }
    if (view.equals(likeButton)) {
      detailController.onLikePress(loaderItem, currentPosition);
    }

    if (view.equals(likedFrame)) {
      detailController.onClickLikedFrame();
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

}
