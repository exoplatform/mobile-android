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
package org.exoplatform.ui.social;

import org.exoplatform.R;
import org.exoplatform.controller.social.SocialDetailController;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Screen for activity details
 */
public class SocialDetailActivity extends Activity implements OnClickListener {

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

  private int                        currentPosition;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_display_view);
    socialDetailActivity = this;
    currentPosition = getIntent().getIntExtra(ExoConstants.ACTIVITY_CURRENT_POSITION, currentPosition);
    changeLanguage();
    initComponent();
    onLoad();
  }

  @Override
  protected void onDestroy() {
    if (detailController != null) {
      detailController.onCancelLoad();
    }
    socialDetailActivity = null;
    super.onDestroy();
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
    likedFrame = (RelativeLayout) findViewById(R.id.detail_likers_layout_wrapper);
    likedFrame.setOnClickListener(this);
  }

  public void onLoad() {
    detailController = new SocialDetailController(this,
                                                  commentLayoutWrap,
                                                  likedLayoutWrap,
                                                  likeButton,
                                                  contentDetailLayout,
                                                  textView_Like_Count);
    detailController.onLoad(false, currentPosition);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_act_detail_refresh:
      detailController.onLoad(false, currentPosition);
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_detail, menu);
    MenuItem loader = menu.findItem(R.id.menu_act_detail_refresh);
    if (detailController != null)
      detailController.setLoaderItem(loader);
    ExoUtils.setLoadingItem(loader, true);
    return super.onCreateOptionsMenu(menu);
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
      detailController.onLikePress(currentPosition);
    }
    if (view.equals(likedFrame)) {
      detailController.onClickLikesFrame();
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
    TextView emptyStatus = (TextView) emptyCommentStubView.findViewById(R.id.empty_status);
    emptyStatus.setTextSize(14);
    emptyStatus.setText(commentEmptyString);
    Drawable icon = getResources().getDrawable(R.drawable.icon_for_no_comment);
    icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
    emptyStatus.setCompoundDrawables(null, icon, null, null);
  }

  private void changeLanguage() {
    Resources resource = getResources();
    String strTitle = resource.getString(R.string.ActivityDetail);
    setTitle(strTitle);
    yourCommentText = resource.getString(R.string.YourComment);
    commentEmptyString = resource.getString(R.string.EmptyComment);
  }
}
