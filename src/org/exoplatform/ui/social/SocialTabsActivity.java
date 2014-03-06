/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ui.social;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import org.exoplatform.controller.home.SocialLoadTask;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.poc.tabletversion.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.ui.setting.SettingActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.StandardArrayAdapter;
import org.exoplatform.widget.WarningDialog;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 23, 2012
 */
public class SocialTabsActivity extends ActionBarActivity implements SocialLoadTask.AsyncTaskListener,
    Refreshable, StandardArrayAdapter.OnItemClickListener {

  public static final int              ALL_UPDATES             = 0;

  public static final int              MY_CONNECTIONS          = 1;

  public static final int              MY_SPACES               = 2;

  public static final int              MY_STATUS               = 3;

  private static final String          NUMBER_OF_ACTIVITY      = "NUMBER_OF_ACTIVITY";

  private static final String          NUMBER_OF_MORE_ACTIVITY = "NUMBER_OF_MORE_ACTIVITY";

  private static final String          ACCOUNT_SETTING         = "account_setting";

  private static final String          DOCUMENT_HELPER         = "document_helper";

  private static final String          CURRENT_FM              = "CURRENT_FM";

  private static final String          REFRESH_STATE           = "REFRESH_STATE";

  private static final String          DETAILS_FM              = "DETAILS_FM";


  private static String[]              TAB_NAMES;

  public static boolean mIsTablet;

  public static SocialTabsActivity     instance;

  private static String[] SOCIAL_TABS = null;


  public ViewPager                     mPager;

  private PageIndicator                mIndicator;

  private SocialTabsAdapter            mAdapter;

  public int                           number_of_activity;

  public int                           number_of_more_activity;

  private SharedPreferences            prefs;

  private boolean                      isSocialFilterEnable    = false;

  private Menu                         mOptionsMenu;

  /** Keep state for refresh icon during orientation */
  private boolean                      mIsRefreshing;

  /** Store the Id of current fragment shown */
  private int                          mCurrentFragment = -1;

  private SocialDetailFragment         mDetailFragment;

  private ActivityStreamFragment       mListFragment;

  private static final String TAG = "eXo____SocialTabsActivity____";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    detectScreenSize();
    super.onCreate(savedInstanceState);
    instance = this;

    SOCIAL_TABS = getResources().getStringArray(R.array.SocialTabs);

    if (savedInstanceState != null) {
      number_of_activity = savedInstanceState.getInt(NUMBER_OF_ACTIVITY);
      number_of_more_activity = savedInstanceState.getInt(NUMBER_OF_MORE_ACTIVITY);
      mCurrentFragment   = savedInstanceState.getInt(CURRENT_FM, -1);
      mIsRefreshing      = savedInstanceState.getBoolean(REFRESH_STATE, true);
      AccountSetting accountSetting = savedInstanceState.getParcelable(ACCOUNT_SETTING);
      AccountSetting.getInstance().setInstance(accountSetting);
      ArrayList<String> cookieList = AccountSetting.getInstance().cookiesList;
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
      DocumentHelper helper = savedInstanceState.getParcelable(DOCUMENT_HELPER);
      DocumentHelper.getInstance().setInstance(helper);
    } else {
      number_of_activity = ExoConstants.NUMBER_OF_ACTIVITY;
      number_of_more_activity = ExoConstants.NUMBER_OF_MORE_ACTIVITY;
    }

    Log.i(TAG, "isTablet: " + mIsTablet);
    if (!mIsTablet) {
      /** phone */
      setContentView(R.layout.social_activity_tabs);
      setTitle(getString(R.string.ActivityStream));

      TAB_NAMES  = getResources().getStringArray(R.array.SocialTabs);
      mPager     = (ViewPager) findViewById(R.id.pager);
      mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
      mAdapter   = new SocialTabsAdapter(getSupportFragmentManager());
      mPager.setAdapter(mAdapter);
      mIndicator.setViewPager(mPager);
    }
    else {
      /** tablet */
      initSubViewsForTablet();
    }

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    /** hide all other fragments except current one */
    if (mCurrentFragment != -1) {
      Log.i(TAG, "hide fragment - current FM : " + mCurrentFragment);
      FragmentManager fragmentManager = getSupportFragmentManager();
      FragmentTransaction ft = fragmentManager.beginTransaction();

      for (String fragmentName : SOCIAL_TABS) {
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentName);
        if (fragmentName.equals(SOCIAL_TABS[mCurrentFragment])) {
          mListFragment = (ActivityStreamFragment) fragment;
          continue;
        }
        if (fragment != null) ft.hide(fragment);
      }

      ft.commit();
    }

    prefs = getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    isSocialFilterEnable = prefs.getBoolean(AccountSetting.getInstance().socialKey, false);
    if (isSocialFilterEnable) {
      int savedIndex = prefs.getInt(AccountSetting.getInstance().socialKeyIndex, ALL_UPDATES);
      mPager.setCurrentItem(savedIndex);
    }
  }


  private void initSubViewsForTablet() {
    Log.i(TAG, "initSubViewsForTablet");
    setContentView(R.layout.social_activity_tabs_tablet);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.SocialTabs,
        android.R.layout.simple_spinner_dropdown_item);

    final ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    actionBar.setListNavigationCallbacks(mSpinnerAdapter, new ActionBar.OnNavigationListener() {

      @Override
      public boolean onNavigationItemSelected(int position, long itemId) {
        Log.i(TAG, "onNavigationItemSelected - position: " + position + " - current fragment: " + mCurrentFragment);

        if (position == mCurrentFragment) {

          /** in case of orientation switch, need to set up listener */
          if (mListFragment.mActivityListAdapter != null) {
            mListFragment.mActivityListAdapter.setOnItemClickListener(SocialTabsActivity.this);
          }
          return true;
        }

        ActivityStreamFragment activityStreamFragment = null;

        switch (position) {

          case ALL_UPDATES:
            activityStreamFragment = AllUpdatesFragment.getInstance();
            break;

          case MY_CONNECTIONS:
            activityStreamFragment = MyConnectionsFragment.getInstance();
            break;

          case MY_SPACES:
            activityStreamFragment = MySpacesFragment.getInstance();
            break;

          case MY_STATUS:
            activityStreamFragment = MyStatusFragment.getInstance();
            break;
        }

        Log.i(TAG, "mCurrentFragment: " + mCurrentFragment);
        if (mCurrentFragment > -1)
          Log.i(TAG, "tag: " + SOCIAL_TABS[mCurrentFragment]);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (mCurrentFragment == -1) {
          /** first run */
          Log.i(TAG, "first run, add the fragment : " + SOCIAL_TABS[position]);
          ft.add(R.id.streams_container, activityStreamFragment, SOCIAL_TABS[position]);
          mListFragment = activityStreamFragment;
        }
        else {
          /** not first run */

          Fragment fragment = fragmentManager.findFragmentByTag(SOCIAL_TABS[mCurrentFragment]);
          if (fragment != null) {
            Log.i(TAG, "hide current fragment: " + fragment.getTag());
            ft.hide(fragment);
          }

          Log.i(TAG, "find requested fragment : " + SOCIAL_TABS[position]);
          /** find requested fragment */
          fragment = fragmentManager.findFragmentByTag(SOCIAL_TABS[position]);
          if (fragment != null) {
            Log.i(TAG, "show current fragment: " + fragment.getTag());
            ft.show(fragment);
            mListFragment = (ActivityStreamFragment) fragment;
            mListFragment.clearSelectedItem();
          }
          else {
            /** fragment not added yet, add it */
            Log.i(TAG, "fragment not added yet, add it : " + SOCIAL_TABS[position]);
            ft.add(R.id.streams_container, activityStreamFragment, SOCIAL_TABS[position]);
            mListFragment = activityStreamFragment;
          }
        }

        ft.commit();
        mCurrentFragment = position;
        if (mListFragment.mActivityListAdapter != null) {
          mListFragment.mActivityListAdapter.setOnItemClickListener(SocialTabsActivity.this);
        }

        return true;
      }
    });

  }


  /**
   * Force screen orientation for small and medium size devices
   */
  private void detectScreenSize() {

    int size = getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK;

    switch (size) {
      case Configuration.SCREENLAYOUT_SIZE_NORMAL:  // 320x470 dp units
        Log.i(TAG, "normal");
        mIsTablet = false;
        break;
      case Configuration.SCREENLAYOUT_SIZE_SMALL:   // 320x426 dp units
        Log.i(TAG, "small");
        mIsTablet = false;
        break;
      case Configuration.SCREENLAYOUT_SIZE_LARGE:   // 480x640 dp units
        Log.i(TAG, "large");
        mIsTablet = true;
        break;
      case Configuration.SCREENLAYOUT_SIZE_XLARGE:  // 720x960 dp units
        Log.i(TAG, "xlarge");
        mIsTablet = true;
        break;
      default:
        break;
    }
  }


  public int getDisplayMode() {
    return mDetailFragment == null ? ActivityStreamFragment.WIDE_MODE : ActivityStreamFragment.NARROW_MODE;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.i(TAG, "onCreateOptionsMenu");

    getMenuInflater().inflate(R.menu.social, menu);
    mOptionsMenu = menu;
    if (mIsRefreshing) menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);

    /** Double pane */
    if (mDetailFragment != null) {
      menu.findItem(R.id.menu_add).setVisible(false);
    }

    return true;
  }


  public int getCurrentFragment() {
    return mCurrentFragment;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {

      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;

      /** Click on Refresh */
      case R.id.menu_refresh:
        Log.i(TAG, "click on refresh - tablet : " + mIsTablet);

        /** on tablet we don't have pager */
        int tabId = !mIsTablet ? mPager.getCurrentItem() : mCurrentFragment;
        switch (tabId) {

          case ALL_UPDATES:
            AllUpdatesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, 0);
            break;

          case MY_CONNECTIONS:
            MyConnectionsFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, 0);
            break;

          case MY_SPACES:
            MySpacesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, 0);
            break;

          case MY_STATUS:
            MyStatusFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, 0);
            break;
        }
        return true;


      case R.id.menu_settings:
        redirectToSetting();
        break;

      case R.id.menu_add:
        redirectToComposeMessage();
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

    if (refreshing)
      refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
    else
      refreshItem.setActionView(null);
  }


  @Override
  protected void onSaveInstanceState(Bundle savedState) {
    super.onSaveInstanceState(savedState);
    Log.i(TAG, "save state");
    savedState.putInt(NUMBER_OF_ACTIVITY, number_of_activity);
    savedState.putInt(NUMBER_OF_MORE_ACTIVITY, number_of_more_activity);
    savedState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
    savedState.putParcelable(DOCUMENT_HELPER, DocumentHelper.getInstance());
    savedState.putInt(CURRENT_FM, mCurrentFragment);
    savedState.putBoolean(REFRESH_STATE, false);

    /** at orientation switch, terminate network call */
    if (mListFragment != null) {
      mListFragment.onCancelLoad();
    }
    if (mDetailFragment != null) {
      mDetailFragment.onCancelLoad();
      mDetailFragment = null;
    }

  }


  @Override
  public void onBackPressed() {
    Log.i(TAG, "onBackPressed");
    /** Single pane */
    if (mDetailFragment == null) {
      super.onBackPressed();
      finishFragment();
    }
    /** Double pane */
    else {
      mDetailFragment.onCancelLoad();

      /** remove the details fragment */
      FragmentManager fragmentManager = getSupportFragmentManager();
      Fragment detailFragment = fragmentManager.findFragmentByTag(DETAILS_FM);
      fragmentManager.beginTransaction().remove(detailFragment).commit();
      mDetailFragment = null;

      /** Make the list fragment takes up whole screen */
      findViewById(R.id.streams_container).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 5.0f));
      findViewById(R.id.activity_detail).setVisibility(View.GONE);
      findViewById(R.id.streams_shadow).setVisibility(View.GONE);

      /** Reload list fragment */
      mListFragment.switchMode(ActivityStreamFragment.WIDE_MODE, true);
      mListFragment.mActivityListAdapter.setOnItemClickListener(this);
      mListFragment.mActivityListView.setSelection(mListFragment.mActivityListView.getFirstVisibleItemPos());
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.i(TAG, "onResume");

    if (mListFragment != null) mListFragment.clearSelectedItem();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    finishFragment();
  }

  private void finishFragment() {
    if (isSocialFilterEnable) {
      int tabId = getTabId();
      Editor editor = prefs.edit();
      editor.putInt(AccountSetting.getInstance().socialKeyIndex, tabId);
      editor.commit();
    }
    instance = null;
  }

  @Override
  public void onLoadingSocialActivitiesFinished(ArrayList<SocialActivityInfo> result) {
    Log.i(TAG, "onLoadingSocialActivitiesFinished");
    setRefreshActionButtonState(false);

    if (result == null) {
      WarningDialog dialog = new WarningDialog(this, getString(R.string.OK),
          getString(R.string.Warning), getString(R.string.LoadingDataError));
      dialog.show();
      return ;
    }
  }

  private void redirectToSetting() {
    Intent next = new Intent(this, SettingActivity.class);
    next.putExtra(ExoConstants.SETTING_TYPE, SettingActivity.PERSONAL_TYPE);
    startActivity(next);
  }


  private void redirectToComposeMessage() {
    Intent intent = new Intent(this, ComposeMessageActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_POST_TYPE);
    startActivity(intent);
  }


  /**
   * Click on activity in stream to open up detail view
   *
   * @param activityInfo
   * @param position
   */
  @Override
  public void onClickActivityItem(View socialItemView, SocialActivityInfo activityInfo, int position) {

    Log.i(TAG, "onClickActivityItem - position : " + position + " - view: " + socialItemView);
    String activityId = activityInfo.getActivityId();
    SocialDetailHelper.getInstance().setActivityId(activityId);
    SocialDetailHelper.getInstance().setAttachedImageUrl(activityInfo.getAttachedImageUrl());

    if (mIsTablet && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

      /** Stop current network if any */
      if (mDetailFragment != null) mDetailFragment.onCancelLoad();

      /** reset the whole list */
      mListFragment.switchMode(ActivityStreamFragment.NARROW_MODE, mDetailFragment == null);

      /** highlight the selected item */
      int idx = mListFragment.sectionAdapter.getSectionPosFromActivityPos(position);
      mListFragment.mActivityListView.setSelection(idx == -1 ? 0 : idx);
      mListFragment.mActivityListView.setItemChecked(idx, true);
      mListFragment.mActivityListAdapter.setOnItemClickListener(this);

      /** Show the container for activity detail */
      if (mDetailFragment == null) {
        View streamContainer = findViewById(R.id.streams_container);
        streamContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 2.0f));
        streamContainer.setAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_right_to_left));

        View activityDetailContainer = findViewById(R.id.activity_detail);
        activityDetailContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3.0f));
        activityDetailContainer.setVisibility(View.VISIBLE);

        /** Show the shadow */
        findViewById(R.id.streams_shadow).setVisibility(View.VISIBLE);
      }

      /** Add the SocialDetailFragment */
      mDetailFragment = new SocialDetailFragment(position);
      mDetailFragment.setRefreshListener(this);

      getSupportFragmentManager().beginTransaction().replace(R.id.activity_detail, mDetailFragment, DETAILS_FM).commit();

      /** Change action bar */
      supportInvalidateOptionsMenu();
    }
    else {

      if (mListFragment != null) {
        Log.i(TAG, "list fragment not null");
        int idx = mListFragment.sectionAdapter.getSectionPosFromActivityPos(position);
        mListFragment.mActivityListView.setItemChecked(idx, true);
      }

      /** fire up activity details */
      Intent intent = new Intent(this, SocialDetailActivity.class);
      intent.putExtra(ExoConstants.ACTIVITY_CURRENT_POSITION, position);
      intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      startActivity(intent);
    }
  }


  public int getTabId() {
    return !SocialTabsActivity.mIsTablet ? SocialTabsActivity.instance.mPager.getCurrentItem() :
        SocialTabsActivity.instance.getCurrentFragment();
  }


  /**===  FragmentPager Adapter for tabs  ===**/
  private class SocialTabsAdapter extends FragmentPagerAdapter {

    public SocialTabsAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      ActivityStreamFragment fragment = null;

      switch (position) {

        case ALL_UPDATES:
          fragment = AllUpdatesFragment.getInstance();
          break;

        case MY_CONNECTIONS:
          fragment = MyConnectionsFragment.getInstance();
          break;

        case MY_SPACES:
          fragment = MySpacesFragment.getInstance();
          break;

        case MY_STATUS:
          fragment = MyStatusFragment.getInstance();
          break;
      }

      return fragment;
    }

    @Override
    public int getCount() {
      return SocialTabsActivity.TAB_NAMES.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return SocialTabsActivity.TAB_NAMES[position];
    }

  }

}
