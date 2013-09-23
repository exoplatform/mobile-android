package org.exoplatform.ui;

import android.util.Log;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.ui.setting.SettingActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.HomeSocialItem;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.ShaderImageView;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Represents the home screen with dashboard
 */
public class HomeActivity extends MyActionBar {

  private TextView            activityButton;

  private TextView            documentButton;

  private TextView            appsButton;

  private String              newsTitle;

  private String              documentTitle;

  private String              appsTitle;

  private Resources           resource;

  private HomeController      homeController;

  private ShaderImageView     homeUserAvatar;

  private TextView            homeUserName;

  private int                 dafault_avatar        = R.drawable.default_avatar;

  private ViewFlipper         viewFlipper;

  private LoaderActionBarItem loaderItem;

  public static HomeActivity  homeActivity;

  private static final String TAG = "eXo____HomeActivity____";

  private AccountSetting      mSetting;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.home_layout);

    super.getActionBar().setType(greendroid.widget.ActionBar.Type.Dashboard);
    addActionBarItem(Type.Refresh);
    getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);
    addActionBarItem();
    getActionBar().getItem(1).setDrawable(R.drawable.action_bar_logout_button);

    mSetting = AccountSetting.getInstance();

    homeActivity = this;
    if (bundle != null) {
      mSetting = bundle.getParcelable(ExoConstants.ACCOUNT_SETTING);
      if (mSetting == null) mSetting = AccountSetting.getInstance();
      ServerSettingHelper settingHelper = bundle.getParcelable(ExoConstants.SERVER_SETTING_HELPER);
      ServerSettingHelper.getInstance().setInstance(settingHelper);
      ArrayList<String> cookieList = mSetting.cookiesList;
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
    }
    loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);
    homeController = new HomeController(this, loaderItem);
    init();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(ExoConstants.SERVER_SETTING_HELPER, ServerSettingHelper.getInstance());
    outState.putParcelable(ExoConstants.ACCOUNT_SETTING, mSetting);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    getContentView().removeAllViews();
    setActionBarContentView(R.layout.home_layout);
    SettingUtils.setDefaultLanguage(this);
    init();
    startSocialService(loaderItem);
  }

  @Override
  protected void onResume() {
    super.onResume();
    SettingUtils.setDefaultLanguage(this);
    setInfo();
    startSocialService(loaderItem);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    menu.add(0, 1, 0, getResources().getString(R.string.Settings))
        .setIcon(R.drawable.optionsettingsbutton);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    int selectedItemIndex = item.getItemId();

    if (selectedItemIndex == 1) {
      Intent next = new Intent(HomeActivity.this, SettingActivity.class);
      next.putExtra(ExoConstants.SETTING_TYPE, SettingActivity.PERSONAL_TYPE);
      startActivity(next);
    }
    return false;
  }

  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
    onLoggingOut();

    super.onBackPressed();

    // new LogoutDialog(HomeActivity.this, homeController).show();
  }

  private void init() {
    activityButton = (TextView) findViewById(R.id.home_btn_activity);
    documentButton = (TextView) findViewById(R.id.home_btn_document);
    appsButton = (TextView) findViewById(R.id.home_btn_apps);
    homeUserAvatar = (ShaderImageView) findViewById(R.id.home_user_avatar);
    homeUserAvatar.setDefaultImageResource(dafault_avatar);
    homeUserAvatar.setVisibility(View.GONE);
    homeUserName = (TextView) findViewById(R.id.home_textview_name);
    viewFlipper = (ViewFlipper) findViewById(R.id.home_social_flipper);
  }

  private void setInfo() {
    resource = getResources();
    changeLanguage();
    activityButton.setText(newsTitle);
    documentButton.setText(documentTitle);
    appsButton.setText(appsTitle);
    if (SocialServiceHelper.getInstance().userProfile != null) {
      setProfileInfo(SocialServiceHelper.getInstance().userProfile);
    }

    if (SocialServiceHelper.getInstance().socialInfoList != null) {
      setSocialInfo(SocialServiceHelper.getInstance().socialInfoList);
    }
  }

  private void startSocialService(LoaderActionBarItem loader) {
    Log.i(TAG, "startSocialService");

    /** if soc activity service is null then loads all soc services */
    if (SocialServiceHelper.getInstance().activityService == null) {
      homeController.launchNewsService();
    } else {
      homeController.onLoad(ExoConstants.HOME_SOCIAL_MAX_NUMBER, SocialTabsActivity.ALL_UPDATES);
    }
  }

  public void setProfileInfo(String[] profile) {
    homeUserAvatar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.home_anim_right_to_left));
    homeUserAvatar.setUrl(profile[0]);
    homeUserName.setText(profile[1]);
    homeUserName.setAnimation(AnimationUtils.loadAnimation(this, R.anim.home_anim_right_to_left));
    homeUserAvatar.setVisibility(View.VISIBLE);
  }

  /*
   * Set Social Information and start animation
   */
  public void setSocialInfo(ArrayList<SocialActivityInfo> list) {
    if (list == null) {
      return;
    }

    HomeSocialItem socialItem;
    viewFlipper.removeAllViews();
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    /*
     * Display maximum 10 activities
     */
    int listSize = list.size();
    int maxChild = (listSize > ExoConstants.HOME_SOCIAL_MAX_NUMBER) ? ExoConstants.HOME_SOCIAL_MAX_NUMBER
                                                                   : listSize;
    for (int i = 0; i < maxChild; i++) {
      socialItem = new HomeSocialItem(this, list.get(i));
      viewFlipper.addView(socialItem, params);
    }
    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.home_anim_right_to_left));
    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.home_anim_left_out));
    viewFlipper.startFlipping();
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:      // click on home
      break;
    case 0:       // click on refresh button
      loaderItem = (LoaderActionBarItem) item;
      if (SocialServiceHelper.getInstance().activityService == null) {
        homeController.launchNewsService();
      } else {
        homeController.onLoad(ExoConstants.HOME_SOCIAL_MAX_NUMBER, SocialTabsActivity.ALL_UPDATES);
      }
      break;
    case 1:       // click on log out
      onLoggingOut();
      redirectToLogIn();
      break;
    }
    return true;

  }

  /**
   * Cleaning up necessary data to log out
   */
  private void onLoggingOut() {
    if (ExoConnectionUtils.httpClient != null) {
      ExoConnectionUtils.httpClient.getConnectionManager().shutdown();
      ExoConnectionUtils.httpClient = null;
    }

    AccountSetting.getInstance().cookiesList = null;

    /* Clear all social service data */
    SocialServiceHelper.getInstance().clearData();
    homeController.finishService();
    homeActivity = null;
  }

  private void redirectToLogIn() {
    Intent next = new Intent(this, LoginActivity.class);
    next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(next);
    finish();  /* do not come back to home - since it's logged out */
  }

  public void onNewsClick(View view) {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      if (!homeController.isLoadingTask())
    	  launchNewsService();
    } else {
      new ConnectionErrorDialog(this).show();
    }
  }

  public void onDocumentClick(View view) {

    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      launchDocumentApp();
    } else {
      new ConnectionErrorDialog(this).show();
    }
  }

  public void onDashboardClick(View view) {

    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      launchDashboardApp();
    } else {
      new ConnectionErrorDialog(this).show();
    }
  }

  private void launchNewsService() {
    Intent next = new Intent(this, SocialTabsActivity.class);
    startActivity(next);
  }

  private void launchDocumentApp() {

    Intent next = new Intent(this, DocumentActivity.class);
    startActivity(next);
  }

  private void launchDashboardApp() {
    Intent intent = new Intent(this, DashboardActivity.class);
    startActivity(intent);
  }

  private void changeLanguage() {
    newsTitle = resource.getString(R.string.ActivityStream);
    documentTitle = resource.getString(R.string.Documents);
    appsTitle = resource.getString(R.string.Dashboard);

  }

}
