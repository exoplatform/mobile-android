package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;

import java.util.ArrayList;

import org.exoplatform.controller.social.SocialController;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.SocialWaitingDialog;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SocialActivity extends MyActionBar {

  private static final String  NUMBER_OF_ACTIVITY      = "NUMBER_OF_ACTIVITY";

  private static final String  NUMBER_OF_MORE_ACTIVITY = "NUMBER_OF_MORE_ACTIVITY";

  private static final String  ACCOUNT_SETTING         = "account_setting";

  private SocialWaitingDialog  _progressDialog;

  private LinearLayout         activityStreamWrap;

  private SocialController     socialController;

  public static SocialActivity socialActivity;

  private View                 emptyStubView;

  private String               title;

  private String               emptyString;

  public int                   number_of_activity;

  public int                   number_of_more_activity;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);
    addActionBarItem();
    getActionBar().getItem(1).setDrawable(R.drawable.action_bar_icon_compose);

    setActionBarContentView(R.layout.activitybrowserview);

    socialActivity = this;
    activityStreamWrap = (LinearLayout) findViewById(R.id.activity_stream_wrap);
    changeLanguage();
    /*
     * Restore the previous state
     */
    if (savedInstanceState != null) {
      number_of_activity = savedInstanceState.getInt(NUMBER_OF_ACTIVITY);
      number_of_more_activity = savedInstanceState.getInt(NUMBER_OF_MORE_ACTIVITY);
      AccountSetting accountSetting = savedInstanceState.getParcelable(ACCOUNT_SETTING);
      AccountSetting.getInstance().setInstance(accountSetting);
      ArrayList<String> cookieList = AccountSetting.getInstance().cookiesList;
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
    } else {
      number_of_activity = ExoConstants.NUMBER_OF_ACTIVITY;
      number_of_more_activity = ExoConstants.NUMBER_OF_MORE_ACTIVITY;
    }

    loadActivity();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(NUMBER_OF_ACTIVITY, number_of_activity);
    outState.putInt(NUMBER_OF_MORE_ACTIVITY, number_of_more_activity);
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
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

  public void loadActivity() {
    socialController = new SocialController(this, activityStreamWrap, _progressDialog);
    if (SocialServiceHelper.getInstance().getActivityService() == null) {
      socialController.launchNewsService();
    } else
      socialController.onLoad(number_of_activity);
  }

  @Override
  public void onBackPressed() {
    socialController.finishService();
    if (_progressDialog != null) {
      _progressDialog.dismiss();
      _progressDialog = null;
    }
    finish();
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      finish();
      break;
    case 0:
      loadActivity();
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
    Resources resource = getResources();
    title = resource.getString(R.string.ActivityStream);
    setTitle(title);
    emptyString = resource.getString(R.string.EmptyActivity);
  }

}
