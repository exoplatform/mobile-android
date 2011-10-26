package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;

import org.exoplatform.controller.social.SocialDetailController;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ImageDownloader;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SocialDetailActivity extends MyActionBar implements OnClickListener {

  private LinearLayout               commentLayoutWrap;

  private EditText                   editTextComment;

  private AsyncImageView             imageView_Avatar;

  private TextView                   textView_Name;

  private TextView                   textView_Message;

  private TextView                   textView_Time;

  private ImageView                  typeImageView;

  private TextView                   textView_Like_Count;

  private Button                     likeButton;

  private View                       attachStubView;

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
    commentLayoutWrap = (LinearLayout) findViewById(R.id.activity_display_comment_wrap);
    imageView_Avatar = (AsyncImageView) findViewById(R.id.imageView_Avatar);
    textView_Name = (TextView) findViewById(R.id.textView_Name);
    textView_Message = (TextView) findViewById(R.id.textView_Message);
    textView_Time = (TextView) findViewById(R.id.textView_Time);
    typeImageView = (ImageView) findViewById(R.id.activity_detail_image_type);
    textView_Like_Count = (TextView) findViewById(R.id.textView_Like_Count);
    editTextComment = (EditText) findViewById(R.id.editText_Comment);
    editTextComment.setHint(yourCommentText);
    editTextComment.setOnClickListener(this);
    likeButton = (Button) findViewById(R.id.like_button);
    likeButton.setOnClickListener(this);
    detailController = new SocialDetailController(this,
                                                  commentLayoutWrap,
                                                  likeButton,
                                                  imageView_Avatar,
                                                  textView_Name,
                                                  textView_Message,
                                                  textView_Time,
                                                  typeImageView,
                                                  textView_Like_Count);
    detailController.onLoad();
  }

  public void displayAttachImage(String url) {
    if (attachStubView == null) {
      initAttachStubView(SocialActivityUtil.getDomain()+url);
    }
    attachStubView.setVisibility(View.VISIBLE);
  }

  private void initAttachStubView(String url) {
    attachStubView = ((ViewStub) findViewById(R.id.attached_image_stub_detail)).inflate();
    ImageView attachImage = (ImageView) attachStubView.findViewById(R.id.attached_image_view);
    // BitmapFactory.Options options = new BitmapFactory.Options();
    // options.inTempStorage = new byte[16 * 1024];
    // attachImage.setOptions(options);
    // attachImage.setDefaultImageResource(R.drawable.documenticonforunknown);
//    ImageDownloader imageDownloader = new ImageDownloader();
    SocialDetailHelper.getInstance().imageDownloader.download(url, attachImage);
    attachImage.setOnClickListener(new OnClickListener() {

      public void onClick(View arg0) {
        Intent intent = new Intent(SocialDetailActivity.this, SocialAttachedImageActivity.class);
        startActivity(intent);
      }
    });
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
