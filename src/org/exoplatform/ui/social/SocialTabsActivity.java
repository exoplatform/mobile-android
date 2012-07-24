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

import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 23, 2012
 */
public class SocialTabsActivity extends MyActionBar {

  public static final int              ALL_UPDATES             = 0;

  public static final int              MY_CONNECTIONS          = 1;

  public static final int              MY_SPACES               = 2;

  public static final int              MY_STATUS               = 3;

  private ViewPager                    mPager;

  private PageIndicator                mIndicator;

  private SocialTabsAdapter            mAdapter;

  private static String[]              TAB_NAMES;

  private static final String          NUMBER_OF_ACTIVITY      = "NUMBER_OF_ACTIVITY";

  private static final String          NUMBER_OF_MORE_ACTIVITY = "NUMBER_OF_MORE_ACTIVITY";

  private static final String          ACCOUNT_SETTING         = "account_setting";

  private static final String          DOCUMENT_HELPER         = "document_helper";

  private HomeController               homeController;

  public ArrayList<SocialActivityInfo> socialList;

  private LoaderActionBarItem          loaderItem;

  public int                           number_of_activity;

  public int                           number_of_more_activity;

  private CommonFragment               commonFragment;

  public static SocialTabsActivity     instance;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    instance = this;
    setActionBarContentView(R.layout.social_activity_tabs);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem(Type.Refresh);
    getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);
    addActionBarItem();
    getActionBar().getItem(1).setDrawable(R.drawable.action_bar_icon_compose);
    String title = getString(R.string.ActivityStream);
    setTitle(title);

    if (savedInstanceState != null) {
      number_of_activity = savedInstanceState.getInt(NUMBER_OF_ACTIVITY);
      number_of_more_activity = savedInstanceState.getInt(NUMBER_OF_MORE_ACTIVITY);
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

    loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);

    TAB_NAMES = getResources().getStringArray(R.array.SocialTabs);

    setActivityList(socialList);

    homeController = new HomeController(this);
    commonFragment = new CommonFragment(this,
                                        homeController,
                                        loaderItem,
                                        number_of_activity,
                                        number_of_more_activity);

    loadActivity(false);
  }

  public void loadActivity(boolean isRefresh) {

    socialList = SocialServiceHelper.getInstance().socialInfoList;

    if (socialList == null) {
      if (SocialServiceHelper.getInstance().activityService == null) {
        homeController.launchNewsService(loaderItem);
      } else
        homeController.onLoad(number_of_activity, loaderItem);
    } else {
      if (isRefresh) {
        homeController.onLoad(number_of_activity, loaderItem);
      } else {
        setActivityList(socialList);
      }

    }
  }

  public void setActivityList(ArrayList<SocialActivityInfo> list) {
    socialList = list;
    mPager = (ViewPager) findViewById(R.id.pager);
    mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
    mAdapter = new SocialTabsAdapter(getSupportFragmentManager());
    mPager.setAdapter(mAdapter);
    mIndicator.setViewPager(mPager);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(NUMBER_OF_ACTIVITY, number_of_activity);
    outState.putInt(NUMBER_OF_MORE_ACTIVITY, number_of_more_activity);
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
    outState.putParcelable(DOCUMENT_HELPER, DocumentHelper.getInstance());
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    instance = null;
  }
  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      finish();
      break;
    case 0:
      loaderItem = (LoaderActionBarItem) item;
      loadActivity(true);
      break;
    case 1:
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_POST_TYPE);
      startActivity(intent);
      break;

    }
    return true;

  }

  private class SocialTabsAdapter extends FragmentPagerAdapter {

    public SocialTabsAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      Fragment fragment = null;
      switch (position) {
      case ALL_UPDATES:
        fragment = AllUpdatesFragment.getInstance(commonFragment, socialList);
        break;
      case MY_CONNECTIONS:
        fragment = MyConnectionsFragment.getInstance(commonFragment, socialList);
        break;
      case MY_SPACES:
        fragment = MySpacesFragment.getInstance(commonFragment, socialList);
        break;
      case MY_STATUS:
        fragment = MyStatusFragment.getInstance(commonFragment, socialList);
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
