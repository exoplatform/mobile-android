package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;

import org.exoplatform.controller.social.SocialController;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ImageDownloader;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SocialActivity extends MyActionBar {
  private LinearLayout         activityStreamWrap;

  private SocialController     socialController;

  public static SocialActivity socialActivity;

  private View                 emptyStubView;

  private String               title;

  private String               emptyString;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // setTheme(R.style.Theme_eXo);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);
    addActionBarItem();
    getActionBar().getItem(1).setDrawable(R.drawable.action_bar_icon_compose);
    setActionBarContentView(R.layout.activitybrowserview);
    socialActivity = this;
    activityStreamWrap = (LinearLayout) findViewById(R.id.activity_stream_wrap);
    changeLanguage();
    init();
  }

  private void init() {
    SocialDetailHelper.getInstance().imageDownloader = new ImageDownloader();
    socialController = new SocialController(this, activityStreamWrap);
    socialController.onLoad();

  }

  public void setEmptyView(int status) {
    if (emptyStubView == null) {
      initStubView();
    }
    emptyStubView.setVisibility(status);
  }

  private void initStubView() {
    emptyStubView = ((ViewStub) findViewById(R.id.social_empty_stub)).inflate();
    ImageView emptyImage = (ImageView) emptyStubView.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_no_activities);
    TextView emptyStatus = (TextView) emptyStubView.findViewById(R.id.empty_status);
    emptyStatus.setText(emptyString);
  }

  public void reloadActivity() {
    socialController.onLoad();
  }

  @Override
  public void onBackPressed() {
    socialController.onCancelLoad();
    finish();
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      finish();
      break;
    case 0:
      socialController.onLoad();
      break;
    case 1:
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_POST_TYPE);
      startActivity(intent);
      break;

    }
    return true;

  }

  private void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    title = bundle.getString("ActivityStream");
    setTitle(title);
    emptyString = bundle.getString("EmptyActivity");
  }

}
