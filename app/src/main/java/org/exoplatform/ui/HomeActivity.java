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
package org.exoplatform.ui;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.accountswitcher.AccountSwitcherActivity;
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
import org.exoplatform.utils.ExoUtils;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.utils.image.ExoPicasso;
import org.exoplatform.utils.image.RoundedCornersTranformer;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.SocialHomeTickerItem;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Represents the home screen with dashboard
 */
public class HomeActivity extends Activity {

  private TextView            activityButton;

  private TextView            documentButton;

  private TextView            appsButton;

  private String              newsTitle;

  private String              documentTitle;

  private String              appsTitle;

  private Resources           resource;

  private HomeController      homeController;

  private ImageView           homeUserAvatar;

  private TextView            homeUserName;

  private ViewFlipper         viewFlipper;

  public static HomeActivity  homeActivity;

  private static final String TAG = HomeActivity.class.getName();

  private AccountSetting      mSetting;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.home_layout);
    setTitle("");

    mSetting = AccountSetting.getInstance();

    homeActivity = this;
    if (bundle != null) {
      mSetting = bundle.getParcelable(ExoConstants.ACCOUNT_SETTING);
      if (mSetting == null)
        mSetting = AccountSetting.getInstance();
      ServerSettingHelper settingHelper = bundle.getParcelable(ExoConstants.SERVER_SETTING_HELPER);
      ServerSettingHelper.getInstance().setInstance(settingHelper);
      ArrayList<String> cookieList = mSetting.cookiesList;
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
    }

    homeController = new HomeController(this);
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
    SettingUtils.setDefaultLanguage(this);
    init();
    setInfo();
    startSocialService();
  }

  @Override
  protected void onResume() {
    super.onResume();
    SettingUtils.setDefaultLanguage(this);
    setInfo();
    startSocialService();
  }

  @Override
  protected void onPause() {
    super.onPause();
    viewFlipper.removeAllViews();
    // getGDApplication().getImageCache().flush();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.home, menu);
    // keep an instance of the refresh button to display a loading indicator
    MenuItem loaderItem = menu.findItem(R.id.menu_home_refresh);
    homeController.setLoader(loaderItem);
    // we're already loading the profile and activities,
    // so we start the loading indicator now
    ExoUtils.setLoadingItem(loaderItem, true);
    return super.onCreateOptionsMenu(menu); // true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // show/hide the account switcher button depending on the nb of accounts
    MenuItem accountSwitcherItem = menu.findItem(R.id.menu_home_account_switcher);
    if (accountSwitcherItem != null) {
      if (ServerSettingHelper.getInstance().twoOrMoreAccountsExist(this))
        accountSwitcherItem.setVisible(true);
      else
        accountSwitcherItem.setVisible(false);
    }
    return super.onPrepareOptionsMenu(menu); // true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_home_refresh:
      startSocialService();
      break;

    case R.id.menu_home_account_switcher:
      Intent accSwitcherIntent = new Intent(this, AccountSwitcherActivity.class);
      startActivity(accSwitcherIntent);
      break;

    case R.id.menu_home_logout:
      logoutAndRedirect();
      break;

    case R.id.menu_home_settings:
      Intent next = new Intent(HomeActivity.this, SettingActivity.class);
      next.putExtra(ExoConstants.SETTING_TYPE, SettingActivity.PERSONAL_TYPE);
      startActivity(next);
      break;
    }

    return false;
  }

  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
    onLoggingOut();

    super.onBackPressed();
  }

  private void init() {
    activityButton = (TextView) findViewById(R.id.home_btn_activity);
    documentButton = (TextView) findViewById(R.id.home_btn_document);
    appsButton = (TextView) findViewById(R.id.home_btn_apps);
    homeUserAvatar = (ImageView) findViewById(R.id.home_user_avatar);
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

  private void startSocialService() {
    // if social activity service is null then loads all social services
    if (SocialServiceHelper.getInstance().activityService == null)
      homeController.launchNewsService();
    else
      homeController.onLoad(ExoConstants.NUMBER_OF_ACTIVITY_HOME, HomeController.FLIPPER_VIEW);
  }

  public void setProfileInfo(String[] profile) {
    homeUserAvatar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.home_anim_right_to_left));
    ExoPicasso.picasso(this)
              .load(Uri.parse(profile[0]))
              .transform(new RoundedCornersTranformer(this))
              .error(R.drawable.default_avatar)
              .into(homeUserAvatar);
    // e.g. John Smith (Intranet)
    String userAndAccount = String.format("%s (%s)", profile[1], mSetting.getCurrentAccount().accountName);
    homeUserName.setText(userAndAccount);
    homeUserName.setAnimation(AnimationUtils.loadAnimation(this, R.anim.home_anim_right_to_left));
    homeUserAvatar.setVisibility(View.VISIBLE);
  }

  /*
   * Set Social Information and start animation
   */
  public void setSocialInfo(ArrayList<SocialActivityInfo> list) {
    if (list == null)
      return;

    SocialHomeTickerItem socialItem;
    viewFlipper.removeAllViews();
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    /*
     * Display maximum 10 activities
     */
    int listSize = list.size();
    int maxChild = (listSize > ExoConstants.NUMBER_OF_ACTIVITY_HOME) ? ExoConstants.NUMBER_OF_ACTIVITY_HOME : listSize;
    for (int i = 0; i < maxChild; i++) {
      socialItem = new SocialHomeTickerItem(this, list.get(i));
      viewFlipper.addView(socialItem, params);
    }
    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.home_anim_right_to_left));
    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.home_anim_left_out));
    viewFlipper.startFlipping();
  }

  /**
   * Signs out and Redirects to the Login screen.<br/>
   * Disables Auto Login.
   */
  private void logoutAndRedirect() {
    // // Disable auto login for the current account and store the parameter
    SettingUtils.disableAutoLogin(this);
    // Log out
    Log.d(TAG, "Logging out...");
    onLoggingOut();
    redirectToLogIn();
  }

  /**
   * Cleaning up necessary data to log out
   */
  private void onLoggingOut() {
    ExoConnectionUtils.loggingOut();
    homeController.finishService();
    homeActivity = null;
  }

  private void redirectToLogIn() {
    Intent next = new Intent(this, LoginActivity.class);
    next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(next);
    finish(); /* do not come back to home - since it's logged out */
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
