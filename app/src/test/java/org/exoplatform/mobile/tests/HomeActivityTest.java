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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.accountswitcher.AccountSwitcherActivity;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.httpclient.FakeHttp;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Apr 16, 2014
 */
public class HomeActivityTest extends ExoActivityTestUtils<HomeActivity> {

  ViewFlipper  flipper;

  TextView     activitiesTV, documentsTV, appsTV, userNameTV;

  LinearLayout activitiesBtn, documentsBtn, appsBtn;

  ImageView    userAvatar;

  Menu         actionBar;

  private void init() {

    flipper = (ViewFlipper) activity.findViewById(R.id.home_social_flipper);
    activitiesTV = (TextView) activity.findViewById(R.id.home_btn_activity);
    documentsTV = (TextView) activity.findViewById(R.id.home_btn_document);
    appsTV = (TextView) activity.findViewById(R.id.home_btn_apps);
    userNameTV = (TextView) activity.findViewById(R.id.home_textview_name);
    userAvatar = (ImageView) activity.findViewById(R.id.home_user_avatar);

    activitiesBtn = (LinearLayout) activitiesTV.getParent();
    documentsBtn = (LinearLayout) documentsTV.getParent();
    appsBtn = (LinearLayout) appsTV.getParent();

    actionBar = new RoboMenu(activity);
    activity.onCreateOptionsMenu(actionBar);
    activity.onPrepareOptionsMenu(actionBar);
  }

  @Override
  @Before
  public void setup() {
    controller = Robolectric.buildActivity(HomeActivity.class);

    // mock response for HTTP GET /rest/api/social/version/latest.json
    FakeHttp.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_VERSION_LATEST),
                                 getResponseOKForRequest(REQ_SOCIAL_VERSION_LATEST));
    // mock response for HTTP GET
    // /rest/private/api/social/v1-alpha3/portal/identity/organization/{testuser}.json
    FakeHttp.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_IDENTITY), getResponseOKForRequest(REQ_SOCIAL_IDENTITY));
    // mock response for HTTP GET
    // /rest/private/api/social/v1-alpha3/portal/identity/{testidentity}.json
    FakeHttp.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_IDENTITY_2), getResponseOKForRequest(REQ_SOCIAL_IDENTITY_2));
    // mock response for HTTP GET
    // /rest/private/api/social/v1-alpha3/portal/activity_stream/feed.json
    FakeHttp.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_NEWS), getResponseOKForRequest(REQ_SOCIAL_NEWS));
  }

  @Override
  @After
  public void teardown() {
    deleteAllAccounts(RuntimeEnvironment.application.getApplicationContext());
    // do not call super.teardown() to avoid error
    // HomeActivity has leaked IntentReceiver that was originally registered
    // here. Are you missing a call to unregisterReceiver()?
  }

  @Test
  public void verifyDefaultLayout() {
    Context ctx = RuntimeEnvironment.application.getApplicationContext();
    setDefaultServerInPreferences(ctx, getServerWithDefaultValues());
    create();
    init();

    final String nameAndAccount = TEST_USER_NAME + " (" + TEST_SERVER_NAME + ")";

    // text field is filled by data returned in RESP_SOCIAL_IDENTITY
    assertThat(userNameTV).containsText(nameAndAccount);

    // should have only 1 activity in the flipper since RESP_SOCIAL_NEWS
    // contains just 1 activity
    assertThat(flipper).hasChildCount(1);

    assertThat(activitiesTV).containsText(R.string.ActivityStream);
    assertThat(documentsTV).containsText(R.string.Documents);
    assertThat(appsTV).containsText(R.string.Dashboard);

    // Check that by default the acc switcher menu is not visible
    MenuItem accSwitcherBtn = actionBar.findItem(R.id.menu_home_account_switcher);
    assertFalse("Account Switcher menu should be invisible", accSwitcherBtn.isVisible());
  }

  @Test
  public void shouldOpenNewsActivity() {
    create();
    init();

    ShadowView.clickOn(activitiesBtn);

    ShadowActivity sActivity = shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);

    assertThat(sIntent.getComponent().getClassName(), equalTo(SocialTabsActivity.class.getName()));
  }

  @Test
  public void shouldOpenNewsActivityFromFlipper() {
    create();
    init();

    ShadowView.clickOn(flipper);

    ShadowActivity sActivity = shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);

    assertThat(sIntent.getComponent().getClassName(), equalTo(SocialTabsActivity.class.getName()));
  }

  @Test
  public void shouldOpenDocumentsActivity() {
    create();
    init();

    ShadowView.clickOn(documentsBtn);

    ShadowActivity sActivity = shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);

    assertThat(sIntent.getComponent().getClassName(), equalTo(DocumentActivity.class.getName()));

  }

  @Test
  public void shouldOpenDashboardActivity() {
    create();
    init();

    ShadowView.clickOn(appsBtn);

    ShadowActivity sActivity = shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);

    assertThat(sIntent.getComponent().getClassName(), equalTo(DashboardActivity.class.getName()));
  }

  @Test
  public void shouldSignOutAndOpenLoginActivity() {
    create();
    init();

    ShadowActivity sActivity = shadowOf(activity);

    // simulate tap on the sign out button
    sActivity.clickMenuItem(R.id.menu_home_logout);

    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);

    assertNull(ExoConnectionUtils.httpClient);
    assertNull(AccountSetting.getInstance().cookiesList);
    assertNull(SocialServiceHelper.getInstance().userIdentity);
    assertNull(SocialServiceHelper.getInstance().activityService);
    assertNull(SocialServiceHelper.getInstance().identityService);
    assertNull(SocialServiceHelper.getInstance().socialInfoList);
    assertNull(SocialServiceHelper.getInstance().userProfile);
    assertNull(SocialServiceHelper.getInstance().myConnectionsList);
    assertNull(SocialServiceHelper.getInstance().mySpacesList);
    assertNull(SocialServiceHelper.getInstance().myStatusList);

    assertThat(sIntent.getComponent().getClassName(), equalTo(LoginActivity.class.getName()));

  }

  @Test
  public void shouldNotHaveAccountSwitcherButtonWithOneAccount() {
    Context ctx = RuntimeEnvironment.application.getApplicationContext();
    setDefaultServerInPreferences(ctx, getServerWithDefaultValues());
    create();
    init();

    // Check that the acc switcher menu is not visible
    MenuItem accSwitcherBtn = actionBar.findItem(R.id.menu_home_account_switcher);
    assertFalse("Account Switcher menu should be invisible", accSwitcherBtn.isVisible());
  }

  @Test
  public void shouldHaveAccountSwitcherButtonWithTwoAndMoreAccounts() {
    Context ctx = RuntimeEnvironment.application.getApplicationContext();
    ArrayList<ExoAccount> accounts = createXAccounts(2);
    addServersInPreferences(ctx, accounts);
    create();
    init();

    // Check that the acc switcher menu is visible
    MenuItem accSwitcherBtn = actionBar.findItem(R.id.menu_home_account_switcher);
    assertTrue("Account Switcher menu should be visible", accSwitcherBtn.isVisible());

  }

  @Test
  public void shouldOpenAccountSwitcherActivity() {
    Context ctx = RuntimeEnvironment.application.getApplicationContext();
    ArrayList<ExoAccount> accounts = createXAccounts(2);
    addServersInPreferences(ctx, accounts);
    create();
    init();

    ShadowActivity sActivity = shadowOf(activity);

    // simulate a tap on the account switcher button
    sActivity.clickMenuItem(R.id.menu_home_account_switcher);

    // On small/normal screens, the account switcher is opened as an
    // activity (full screen)
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);
    assertThat(sIntent.getComponent().getClassName(), equalTo(AccountSwitcherActivity.class.getName()));
  }

  @Test
  public void shouldDisableAutoLoginOnAccountWhenSigningOut() {
    create();
    init();
    ExoAccount acc = getServerWithDefaultValues();
    acc.isRememberEnabled = true;
    acc.isAutoLoginEnabled = true;
    setDefaultServerInPreferences(activity, acc);
    acc = null;
    acc = getAccounts(activity).get(0);
    assertTrue("AL should be enabled", acc.isAutoLoginEnabled);

    // simulate tap on the sign out button
    shadowOf(activity).clickMenuItem(R.id.menu_home_logout);

    acc = null;
    acc = getAccounts(activity).get(0);

    assertFalse("AL should be disabled", acc.isAutoLoginEnabled);
  }

  @Test
  public void shouldNotDisableAutoLoginWhenBackButtonPressed() {
    create();
    init();
    ExoAccount acc = getServerWithDefaultValues();
    acc.isRememberEnabled = true;
    acc.isAutoLoginEnabled = true;
    setDefaultServerInPreferences(activity, acc);

    acc = ServerSettingHelper.getInstance().getServerInfoList(activity).get(0);
    assertTrue("AL should be enabled", acc.isAutoLoginEnabled);

    activity.onBackPressed();

    acc = ServerSettingHelper.getInstance().getServerInfoList(activity).get(0);
    assertTrue("AL should still be enabled", acc.isAutoLoginEnabled);
  }

}
