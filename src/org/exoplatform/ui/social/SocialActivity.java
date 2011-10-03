package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;

import org.exoplatform.controller.social.SocialController;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;

import com.cyrilmottier.android.greendroid.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

public class SocialActivity extends MyActionBar {
  private LinearLayout         activityStreamWrap;

  private SocialController     socialController;

  public static SocialActivity socialActivity;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    setTheme(R.style.Theme_eXo);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.gd_action_bar_refresh);
    addActionBarItem();
    getActionBar().getItem(1).setDrawable(R.drawable.gd_action_bar_compose);
    setActionBarContentView(R.layout.activitybrowserview);
    socialActivity = this;
    activityStreamWrap = (LinearLayout) findViewById(R.id.activity_stream_wrap);
    String title = LocalizationHelper.getInstance().getString("ActivityStream");
    setTitle(title);
    init();
  }

  private void init() {
    socialController = new SocialController(this, activityStreamWrap);
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

}
