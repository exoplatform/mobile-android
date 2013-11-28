package org.exoplatform.ui;

import java.util.ArrayList;


import android.util.Log;
import org.exoplatform.R;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.controller.home.SocialLoadTask;
import org.exoplatform.controller.home.SocialServiceLoadTask;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.common.RealtimeListAccess;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.service.QueryParams;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.ui.setting.SettingActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.*;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Represents the home screen with dashboard
 */
public class HomeActivity extends ActionBarActivity
    implements SocialServiceLoadTask.AsyncTaskListener,
    SocialLoadTask.AsyncTaskListener {
    // extends MyActionBar {

  private TextView            activityButton;

  private TextView            documentButton;

  private TextView            appsButton;

  private String              newsTitle;

  private String              documentTitle;

  private String              appsTitle;

  private Resources           mResources;

  //private HomeController      homeController;

  private ShaderImageView     homeUserAvatar;

  private TextView            homeUserName;

  private int                 dafault_avatar        = R.drawable.default_avatar;

  private ViewFlipper         viewFlipper;

  //private LoaderActionBarItem loaderItem;

  //public static HomeActivity  homeActivity;

  private AccountSetting      mSetting;

  public  SocialServiceLoadTask  mServiceLoadTask;

  private SocialLoadTask         mLoadTask;

  //public  LoaderActionBarItem    loader;

  public  static final int     FLIPPER_VIEW = 10;

  private static final String TAG = "eXo____HomeActivity____";


  /** The action bar */
  private Menu mOptionsMenu;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    setContentView(R.layout.home_layout);

    getSupportActionBar().setDisplayShowTitleEnabled(false);

    //setActionBarContentView(R.layout.home_layout);

    //super.getActionBar().setType(greendroid.widget.ActionBar.Type.Dashboard);
    //addActionBarItem(Type.Refresh);
    //getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);
    //addActionBarItem();
    //getActionBar().getItem(1).setDrawable(R.drawable.action_bar_logout_button);

    mSetting = AccountSetting.getInstance();

    //homeActivity = this;
    if (bundle != null) {
      mSetting = bundle.getParcelable(ExoConstants.ACCOUNT_SETTING);
      if (mSetting == null) mSetting = AccountSetting.getInstance();
      ServerSettingHelper settingHelper = bundle.getParcelable(ExoConstants.SERVER_SETTING_HELPER);
      ServerSettingHelper.getInstance().setInstance(settingHelper);
      ArrayList<String> cookieList = mSetting.cookiesList;
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
    }

    //loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);
    //homeController = new HomeController(this, loaderItem);

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

    //getContentView().removeAllViews();
    //setActionBarContentView(R.layout.home_layout);

    SettingUtils.setDefaultLanguage(this);
    init();

    //startSocialService(loaderItem);
    startLoadingSocialData();
  }

  @Override
  protected void onResume() {
    super.onResume();
    SettingUtils.setDefaultLanguage(this);
    setInfo();

    //startSocialService(loaderItem);
    startLoadingSocialData();
  }

  /**
   * Load a number of activities with specific type
   *
   * @param number
   * @param type
   */
  /**
  public void onLoad(int number, int type) {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      if (mLoadTask == null || mLoadTask.getStatus() == SocialLoadTask.Status.FINISHED) {
        mLoadTask = (SocialLoadTask) new SocialLoadTask(this, loader) {

          @Override
          protected ArrayList<SocialActivityInfo> getSocialActivityList() {
            return SocialServiceHelper.getInstance().socialInfoList;
          }

          @Override
          protected RealtimeListAccess<RestActivity> getRestActivityList(RestIdentity identity, QueryParams params) throws SocialClientLibException {
            return activityService.getFeedActivityStream(identity, params);
          }
        }.execute(number, type);
      }
    } else {
      new ConnectionErrorDialog(this).show();
    }
  }
   **/


  @Override
  protected void onPause() {
    super.onPause();

    viewFlipper.removeAllViews();
    //getGDApplication().getImageCache().flush();
    System.gc();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.i(TAG, "onCreateOptionsMenu");
    getMenuInflater().inflate(R.menu.home, menu);
    mOptionsMenu = menu;
    menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_refresh:
        startLoadingSocialData();
        return true;

      case R.id.menu_settings:
        redirectToSetting();
        break;

      case R.id.menu_sign_out:
        onLoggingOut();
        redirectToLogIn();
        return true;
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


  /**
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
   **/

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
    mResources = getResources();
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

  // replace startSocialService for naming purpose
  private void startLoadingSocialData() {
    setRefreshActionButtonState(true);

    // if soc activity service is null then loads all soc services
    if (SocialServiceHelper.getInstance().activityService == null)
      startSocialServiceLoadTask(); //homeController.launchNewsService();
    else
      startSocialLoadTask(ExoConstants.HOME_SOCIAL_MAX_NUMBER, SocialTabsActivity.ALL_UPDATES);
      //onLoad(ExoConstants.HOME_SOCIAL_MAX_NUMBER, SocialTabsActivity.ALL_UPDATES);
  }


  // replace launchNewsService
  /**
   * Retrieve activity service which is entry point for further social data
   */
  public void startSocialServiceLoadTask() {
    if (!ExoConnectionUtils.isNetworkAvailableExt(this)) {
      setRefreshActionButtonState(false);
      new ConnectionErrorDialog(this).show();
      return ;
    }

    if (mServiceLoadTask == null || mServiceLoadTask.getStatus() == SocialServiceLoadTask.Status.FINISHED) {
      mServiceLoadTask = new SocialServiceLoadTask(this);
      mServiceLoadTask.setListener(this);
      mServiceLoadTask.execute();
    }
  }

  @Override
  public void onLoadingSocialServiceFinished(String[] result) {
    if (result == null) {
      setRefreshActionButtonState(false);
      WarningDialog dialog = new WarningDialog(this, mResources.getString(R.string.Warning),
          mResources.getString(R.string.LoadingDataError), mResources.getString(R.string.OK));
      dialog.show();
      return ;
    }

    setProfileInfo(result);
    startSocialLoadTask(ExoConstants.HOME_SOCIAL_MAX_NUMBER, HomeController.FLIPPER_VIEW);
  }

  // replace onLoad

  /**
   * Retrieve social activities for view flipper
   *
   * @param number  number of activities to retrieve
   * @param type    type of activities
   */
  public void startSocialLoadTask(int number, int type) {
    if (!ExoConnectionUtils.isNetworkAvailableExt(this)) {
      setRefreshActionButtonState(false);
      new ConnectionErrorDialog(this).show();
      return ;
    }

    if (mLoadTask == null || mLoadTask.getStatus() == SocialLoadTask.Status.FINISHED) {

      mLoadTask = new SocialLoadTask(this) {

        @Override
        protected ArrayList<SocialActivityInfo> getSocialActivityList() {
          return SocialServiceHelper.getInstance().socialInfoList;
        }

        @Override
        protected RealtimeListAccess<RestActivity> getRestActivityList(RestIdentity identity, QueryParams params) throws SocialClientLibException {
          return activityService.getFeedActivityStream(identity, params);
        }
      };

      mLoadTask.setListener(this);
      mLoadTask.execute(number, type);
    }
  }


  @Override
  public void onLoadingSocialActivitiesFinished(ArrayList<SocialActivityInfo> result) {
    Log.i(TAG, "onLoadingSocialActivitiesFinished");
    setRefreshActionButtonState(false);

    if (result == null) {
      WarningDialog dialog = new WarningDialog(this, mResources.getString(R.string.OK),
          mResources.getString(R.string.Warning), mResources.getString(R.string.LoadingDataError));
      dialog.show();
      return ;
    }

    //if (feedType == HomeController.FLIPPER_VIEW && HomeActivity.homeActivity != null)
    setSocialInfo(result);
  }

  /**
  private void startSocialService(LoaderActionBarItem loader) {
    // if soc activity service is null then loads all soc services
    if (SocialServiceHelper.getInstance().activityService == null)
      launchNewsService(); //homeController.launchNewsService();
    else
      homeController.onLoad(ExoConstants.HOME_SOCIAL_MAX_NUMBER, SocialTabsActivity.ALL_UPDATES);
  }
  **/


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
    if (list == null) return ;

    HomeSocialItem socialItem;
    //viewFlipper.removeAllViews();
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
    //homeController.finishService();
    //homeActivity = null;
  }

  private void redirectToLogIn() {
    Intent next = new Intent(this, LoginActivity.class);
    next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(next);
    finish();  /** do not come back to home - since it's logged out */
  }

  private void redirectToSetting() {
    Intent next = new Intent(this, SettingActivity.class);
    next.putExtra(ExoConstants.SETTING_TYPE, SettingActivity.PERSONAL_TYPE);
    startActivity(next);
  }

  public void onNewsClick(View view) {
    if (ExoConnectionUtils.isNetworkAvailableExt(this))
      if (!isLoadingTask()) redirectToNews();
    else
      new ConnectionErrorDialog(this).show();
  }

  public boolean isLoadingTask() {
    return (mServiceLoadTask != null
        && mServiceLoadTask.getStatus() == SocialServiceLoadTask.Status.RUNNING);
  }

  private void redirectToNews() {
    Intent next = new Intent(this, SocialTabsActivity.class);
    startActivity(next);
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


  private void launchDocumentApp() {

    Intent next = new Intent(this, DocumentActivity.class);
    startActivity(next);
  }

  private void launchDashboardApp() {
    Intent intent = new Intent(this, DashboardActivity.class);
    startActivity(intent);
  }

  private void changeLanguage() {
    newsTitle     = mResources.getString(R.string.ActivityStream);
    documentTitle = mResources.getString(R.string.Documents);
    appsTitle     = mResources.getString(R.string.Dashboard);
  }

}
