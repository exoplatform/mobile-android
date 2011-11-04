package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;

import org.exoplatform.controller.social.SocialDetailController;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SocialDetailActivity extends MyActionBar implements OnClickListener {

  public LinearLayout                startScreen;

  private LinearLayout               commentLayoutWrap;

  private EditText                   editTextComment;

  private LinearLayout               contentDetailLayout;

  private TextView                   textView_Like_Count;

  private Button                     likeButton;

  private String                     yourCommentText;

  private SocialDetailController     detailController;

  public static SocialDetailActivity socialDetailActivity;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.activity_display_view);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.gd_action_bar_refresh);
    socialDetailActivity = this;
    changeLanguage();
    initComponent();

  }

  private void initComponent() {

    startScreen = (LinearLayout) findViewById(R.id.details_start_screen);
    commentLayoutWrap = (LinearLayout) findViewById(R.id.activity_display_comment_wrap);

    contentDetailLayout = (LinearLayout) findViewById(R.id.social_detail_wrap_layout);
    textView_Like_Count = (TextView) findViewById(R.id.textView_Like_Count);
    editTextComment = (EditText) findViewById(R.id.editText_Comment);
    editTextComment.setHint(yourCommentText);
    editTextComment.setOnClickListener(this);
    likeButton = (Button) findViewById(R.id.like_button);
    likeButton.setOnClickListener(this);
    detailController = new SocialDetailController(this,
                                                  commentLayoutWrap,
                                                  likeButton,
                                                  contentDetailLayout,
                                                  textView_Like_Count);
    detailController.onLoad();
  }

  @Override
  protected void onResume() {
    super.onResume();
    detailController.onLoad();
  }

  private void destroy() {
    super.onDestroy();
    detailController.onCancelLoad();
    finish();
  }

  @Override
  public void onBackPressed() {
    detailController.onCancelLoad();
    finish();
  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      if (SocialActivity.socialActivity != null) {
        SocialActivity.socialActivity.finish();
      }
      destroy();
      break;
    case 0:
      detailController.onLoad();
      break;

    }

    return true;
  }

  // @Override
  public void onClick(View view) {
    if (view == editTextComment) {
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_COMMENT_TYPE);
      startActivity(intent);
    }
    if (view == likeButton) {
      detailController.onLikePress();
    }
  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    String strTitle = location.getString("ActivityDetail");
    setTitle(strTitle);
    yourCommentText = location.getString("YourComment");

  }

}
