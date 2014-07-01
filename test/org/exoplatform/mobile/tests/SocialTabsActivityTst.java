/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.mobile.tests;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;
import greendroid.widget.ActionBar;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.ui.social.ActivityStreamFragment;
import org.exoplatform.ui.social.AllUpdatesFragment;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.ui.social.TabPageIndicator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.FragmentTestUtil;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 21, 2014  
 */
//@RunWith(ExoRobolectricTestRunner.class)
public class SocialTabsActivityTst extends ExoActivityTestUtils<SocialTabsActivity> {

  private static final String FRAGMENT_TAG = "fragment";
  
  TabPageIndicator tabs;
  ViewPager pages;
  ActionBar actionBar;
  ActivityStreamFragment fragment;

  @Override
//  @Before
  public void setup() {
    controller = Robolectric.buildActivity(SocialTabsActivity.class);
    
// mock response for HTTP GET /rest/api/social/version/latest.json
  Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_VERSION_LATEST), getResponseOKForRequest(REQ_SOCIAL_VERSION_LATEST));
// mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/identity/organization/{testuser}.json
  Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_IDENTITY), getResponseOKForRequest(REQ_SOCIAL_IDENTITY));
// mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/identity/{testidentity}.json
  Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_IDENTITY_2), getResponseOKForRequest(REQ_SOCIAL_IDENTITY_2));
// mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/activity_stream/feed.json?limit=10
  Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_NEWS), getResponseOKForRequest(REQ_SOCIAL_NEWS));
// mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/activity_stream/feed.json?limit=50
  Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_ALL_UPDATES), getResponseOKForRequest(REQ_SOCIAL_ALL_UPDATES));
// mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/activity_stream/connections.json
  Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_MY_CONNECTIONS), getResponseOKForRequest(REQ_SOCIAL_MY_CONNECTIONS));
  }
  
  private void init() {
    
    tabs = (TabPageIndicator)activity.findViewById(R.id.indicator);
    pages = (ViewPager)activity.findViewById(R.id.pager);
    actionBar = activity.getActionBar();
    
  }
  
  private void startFragment() {
    FragmentManager manager = activity.getSupportFragmentManager();
    manager.beginTransaction()
        .add(fragment, FRAGMENT_TAG).commit();
  }
  
  private Bundle createBundleWithDefaultSettings() {
    Bundle b = new Bundle();
    AccountSetting s = AccountSetting.getInstance();
    s.setCurrentServer(getServerWithDefaultValues());
    s.cookiesList = new ArrayList<String>();
    b.putParcelable("account_setting", s);
    return b;
  }
  
//  @Test
  public void verifyDefaultLayout() {
    create();
    init();
    
    assertNotNull("Button should exist at pos 0", actionBar.getItem(0)); // Refresh
    assertNotNull("Button should exist at pos 1", actionBar.getItem(1)); // Compose
    assertNull("Button should not exist at pos 2", actionBar.getItem(2)); // ActionBar.getItem() returns null when there is no item at the given position
    
    assertThat(pages.getAdapter()).hasCount(4); // Should have 4 pages  
    assertThat("Default tab should be All Updates", pages.getCurrentItem(), equalTo(SocialTabsActivity.ALL_UPDATES));
    
    LinearLayout tabsLayout = (LinearLayout)tabs.getChildAt(0);
    String[] tabsTitle = activity.getResources().getStringArray(R.array.SocialTabs); // check each tab's title
    assertThat((TextView)tabsLayout.getChildAt(SocialTabsActivity.ALL_UPDATES)).containsText(tabsTitle[SocialTabsActivity.ALL_UPDATES]);
    assertThat((TextView)tabsLayout.getChildAt(SocialTabsActivity.MY_CONNECTIONS)).containsText(tabsTitle[SocialTabsActivity.MY_CONNECTIONS]);
    assertThat((TextView)tabsLayout.getChildAt(SocialTabsActivity.MY_SPACES)).containsText(tabsTitle[SocialTabsActivity.MY_SPACES]);
    assertThat((TextView)tabsLayout.getChildAt(SocialTabsActivity.MY_STATUS)).containsText(tabsTitle[SocialTabsActivity.MY_STATUS]);
    
    
  }
  
//  @Test
  public void shouldMoveToOtherPage() {
    create();
    init();
    
    tabs.setCurrentItem(SocialTabsActivity.ALL_UPDATES);
    assertThat("Should be on All Updates tab", pages.getCurrentItem(), equalTo(SocialTabsActivity.ALL_UPDATES)); // remains on All Updates
    
    tabs.setCurrentItem(SocialTabsActivity.MY_CONNECTIONS);
    assertThat("Should be on My Connections tab", pages.getCurrentItem(), equalTo(SocialTabsActivity.MY_CONNECTIONS)); // moves to My Connections
    
    LinearLayout tabsLayout = (LinearLayout)tabs.getChildAt(0);
    
    Robolectric.clickOn((View)tabsLayout.getChildAt(SocialTabsActivity.MY_SPACES));
    assertThat("Should be on My Spaces tab", pages.getCurrentItem(), equalTo(SocialTabsActivity.MY_SPACES)); // moves to My Spaces
    
  }
  
//  @Test
  public void shouldFinishActivity() {
    create();
    init();
    
    ImageButton homeBtn = (ImageButton)actionBar.getChildAt(0); // home button is the 1st child of the action bar layout
    Robolectric.clickOn(homeBtn); 
    
    
    ShadowActivity sActivity = shadowOf(activity);
    
    assertTrue("Activity should be finishing", sActivity.isFinishing());
  }
  
  /*
   * TEST FRAGMENTS
   */
  
//  @Test
  public void shouldLoadAllUpdatesFragment() {
    createWithBundle(createBundleWithDefaultSettings());
    init();
    
    HomeController c = new HomeController(activity);
    c.launchNewsService(activity.loaderItem);
    
    fragment = AllUpdatesFragment.getInstance();
    
    // Adding a call to Robolectric.runUiThreadTasksIncludingDelayedTasks() ensures that the async tasks execute completely
    Robolectric.runUiThreadTasksIncludingDelayedTasks();
    
    assertThat("Fragment ID should be ALL_UPDATES", ((AllUpdatesFragment)fragment).getThisTabId(), equalTo(SocialTabsActivity.ALL_UPDATES));
    assertNotNull("SocialServiceHelper should not be null", SocialServiceHelper.getInstance());
    
    assertNotNull("Social Info List should not be null", SocialServiceHelper.getInstance().socialInfoList);
    assertThat("Social Info List should contain 2 items", SocialServiceHelper.getInstance().socialInfoList.size(), equalTo(2));
    
    Robolectric.clearPendingHttpResponses();
  }

}
