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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;
import greendroid.widget.ActionBar;

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
import org.exoplatform.widget.ShaderImageView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import android.content.Context;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Apr 16, 2014
 */
@RunWith(ExoRobolectricTestRunner.class)
public class HomeActivityTest extends ExoActivityTestUtils<HomeActivity> {

    ViewFlipper     flipper;

    TextView        activitiesTV, documentsTV, appsTV, userNameTV;

    LinearLayout    activitiesBtn, documentsBtn, appsBtn;

    ShaderImageView userAvatar;

    ActionBar       actionBar;

    private void init() {
        flipper = (ViewFlipper) activity.findViewById(R.id.home_social_flipper);
        activitiesTV = (TextView) activity.findViewById(R.id.home_btn_activity);
        documentsTV = (TextView) activity.findViewById(R.id.home_btn_document);
        appsTV = (TextView) activity.findViewById(R.id.home_btn_apps);
        userNameTV = (TextView) activity.findViewById(R.id.home_textview_name);
        userAvatar = (ShaderImageView) activity.findViewById(R.id.home_user_avatar);

        activitiesBtn = (LinearLayout) activitiesTV.getParent();
        documentsBtn = (LinearLayout) documentsTV.getParent();
        appsBtn = (LinearLayout) appsTV.getParent();

        actionBar = activity.getActionBar();

    }

    @Override
    @Before
    public void setup() {
        controller = Robolectric.buildActivity(HomeActivity.class);

        // mock response for HTTP GET /rest/api/social/version/latest.json
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_VERSION_LATEST),
                                        getResponseOKForRequest(REQ_SOCIAL_VERSION_LATEST));
        // mock response for HTTP GET
        // /rest/private/api/social/v1-alpha3/portal/identity/organization/{testuser}.json
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_IDENTITY),
                                        getResponseOKForRequest(REQ_SOCIAL_IDENTITY));
        // mock response for HTTP GET
        // /rest/private/api/social/v1-alpha3/portal/identity/{testidentity}.json
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_IDENTITY_2),
                                        getResponseOKForRequest(REQ_SOCIAL_IDENTITY_2));
        // mock response for HTTP GET
        // /rest/private/api/social/v1-alpha3/portal/activity_stream/feed.json
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_NEWS),
                                        getResponseOKForRequest(REQ_SOCIAL_NEWS));
    }

    @Override
    @After
    public void teardown() {
        deleteAllAccounts(Robolectric.application.getApplicationContext());
        // do not call super.teardown() to avoid error
        // HomeActivity has leaked IntentReceiver that was originally registered
        // here. Are you missing a call to unregisterReceiver()?
    }

    @Test
    public void verifyDefaultLayout() {
        Context ctx = Robolectric.application.getApplicationContext();
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

        // because it's a greendroid actionbar, the number of items is not
        // exposed we use a trick to verify that only 2 items are present
        // (without account switcher)
        assertNotNull(actionBar.getItem(0)); // refresh button, must not be null
        assertNotNull(actionBar.getItem(1)); // sign-out button, must not be
                                             // null
        assertNull(actionBar.getItem(2)); // ActionBar.getItem() returns null
                                          // when there is no item at the given
                                          // position

    }

    @Test
    public void shouldOpenNewsActivity() {
        create();
        init();

        Robolectric.clickOn(activitiesBtn);

        ShadowActivity sActivity = shadowOf(activity);
        Intent welcomeIntent = sActivity.getNextStartedActivity();
        ShadowIntent sIntent = shadowOf(welcomeIntent);

        assertThat(sIntent.getComponent().getClassName(),
                   equalTo(SocialTabsActivity.class.getName()));
    }

    @Test
    public void shouldOpenNewsActivityFromFlipper() {
        create();
        init();

        Robolectric.clickOn(flipper);

        ShadowActivity sActivity = shadowOf(activity);
        Intent welcomeIntent = sActivity.getNextStartedActivity();
        ShadowIntent sIntent = shadowOf(welcomeIntent);

        assertThat(sIntent.getComponent().getClassName(),
                   equalTo(SocialTabsActivity.class.getName()));
    }

    @Test
    public void shouldOpenDocumentsActivity() {
        create();
        init();

        Robolectric.clickOn(documentsBtn);

        ShadowActivity sActivity = shadowOf(activity);
        Intent welcomeIntent = sActivity.getNextStartedActivity();
        ShadowIntent sIntent = shadowOf(welcomeIntent);

        assertThat(sIntent.getComponent().getClassName(), equalTo(DocumentActivity.class.getName()));

    }

    @Test
    public void shouldOpenDashboardActivity() {
        create();
        init();

        Robolectric.clickOn(appsBtn);

        ShadowActivity sActivity = shadowOf(activity);
        Intent welcomeIntent = sActivity.getNextStartedActivity();
        ShadowIntent sIntent = shadowOf(welcomeIntent);

        assertThat(sIntent.getComponent().getClassName(),
                   equalTo(DashboardActivity.class.getName()));
    }

    @Test
    public void shouldSignOutAndOpenLoginActivity() {
        create();
        init();

        Robolectric.clickOn(actionBar.getItem(1).getItemView()); // sign out
                                                                 // button

        ShadowActivity sActivity = shadowOf(activity);
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
        Context ctx = Robolectric.application.getApplicationContext();
        setDefaultServerInPreferences(ctx, getServerWithDefaultValues());
        create();
        init();

        assertNull("Action Bar should contain 2 items (refresh, logout), not 3",
                   actionBar.getItem(2));
        assertThat(actionBar.getItem(1).getDrawable()).isEqualTo(ctx.getResources()
                                                                    .getDrawable(R.drawable.action_bar_logout_button));
    }

    @Test
    public void shouldHaveAccountSwitcherButtonWithTwoAndMoreAccounts() {
        Context ctx = Robolectric.application.getApplicationContext();
        ArrayList<ExoAccount> accounts = createXAccounts(2);
        addServersInPreferences(ctx, accounts);
        create();
        init();

        assertNotNull("Action Bar should contain 3 items (refresh, account switcher, logout)",
                      actionBar.getItem(2));
        assertThat(actionBar.getItem(1).getDrawable()).isEqualTo(ctx.getResources()
                                                                    .getDrawable(R.drawable.action_bar_switcher_icon));
        assertThat(actionBar.getItem(2).getDrawable()).isEqualTo(ctx.getResources()
                                                                    .getDrawable(R.drawable.action_bar_logout_button));

    }

    @Test
    public void shouldOpenAccountSwitcherActivity() {
        Context ctx = Robolectric.application.getApplicationContext();
        ArrayList<ExoAccount> accounts = createXAccounts(2);
        addServersInPreferences(ctx, accounts);
        create();
        init();

        // simulate a tap on the account switcher button
        Robolectric.clickOn(actionBar.getItem(1).getItemView());

        ShadowActivity sActivity = shadowOf(activity);
        Intent welcomeIntent = sActivity.getNextStartedActivity();
        ShadowIntent sIntent = shadowOf(welcomeIntent);

        // On small and normal screens, the account switcher is opened as an
        // activity (full screen)
        assertThat(sIntent.getComponent().getClassName(),
                   equalTo(AccountSwitcherActivity.class.getName()));
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
        Robolectric.clickOn(actionBar.getItem(1).getItemView());

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
