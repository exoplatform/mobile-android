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
package org.exoplatform.mobile.tests;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.accountswitcher.AccountSwitcherActivity;
import org.exoplatform.accountswitcher.AccountSwitcherFragment;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.ui.login.LoginActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowResources.ShadowTheme;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.content.IntentCompat;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Sep 9, 2014
 */
@RunWith(ExoRobolectricTestRunner.class)
public class AccountSwitcherTest extends ExoActivityTestUtils<AccountSwitcherActivity> {

    final String            FRAGMENT_TAG = "account_switcher_fragment_dialog";

    AccountSwitcherFragment accountSwitcherFragment;

    ListView                accountListView;

    TextView                titleTextView;

    ArrayList<ExoAccount>   accounts;

    @Override
    @Before
    public void setup() {
        controller = Robolectric.buildActivity(AccountSwitcherActivity.class);
    }

    @Override
    public void create() {
        Context ctx = Robolectric.application.getApplicationContext();
        accounts = createXAccounts(2);
        addServersInPreferences(ctx, accounts);
        super.create();
    }

    @Override
    @After
    public void teardown() {
        Context ctx = Robolectric.application.getApplicationContext();
        deleteAllAccounts(ctx);
        super.teardown();
    }

    public void init() {
        accountSwitcherFragment = (AccountSwitcherFragment) activity.getSupportFragmentManager()
                                                                    .findFragmentByTag(FRAGMENT_TAG);
        accountListView = (ListView) activity.findViewById(R.id.account_list_view);
        titleTextView = (TextView) activity.findViewById(R.id.account_switcher_fragment_title);
    }

    @Test
    public void verifyDefaultLayout() {
        create();
        init();

        assertThat(accountSwitcherFragment).isAdded()
                                           .isVisible()
                                           .isNotDetached()
                                           .hasTag(FRAGMENT_TAG);
        String titleText = titleTextView.getText().toString();
        assertTrue("Title should be capitalized", Character.isUpperCase(titleText.charAt(0)));
        assertTrue("Title should be 'Accounts'",
                   titleText.equalsIgnoreCase(activity.getResources().getString(R.string.Server)));

        final int numberOfAccounts = 2;
        assertThat(accountListView).hasCount(numberOfAccounts);

        for (int i = 0; i < numberOfAccounts; i++) {
            View v = accountListView.getAdapter().getView(i, null, accountListView);
            TextView name = (TextView) v.findViewById(R.id.account_name_textview);
            assertThat(name).containsText(TEST_SERVER_NAME.toUpperCase() + " " + (i + 1));
            TextView server = (TextView) v.findViewById(R.id.account_server_textview);
            assertThat(server).containsText(TEST_SERVER_URL);
            TextView username = (TextView) v.findViewById(R.id.account_username_textview);
            assertThat(username).containsText(TEST_USER_NAME + "_" + (i + 1));
        }
    }

    @Test
    public void verifyDefaultLayout_Dialog() {
        // Sets a large screen layout
        // @Config(qualifiers="large") doesn't work
        Robolectric.application.getResources().getConfiguration().screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;

        create();
        init();

        assertThat(accountSwitcherFragment).isAdded()
                                           .isVisible()
                                           .isNotDetached()
                                           .hasTag(FRAGMENT_TAG);
        enableLog();

        ShadowTheme sTheme = shadowOf(activity.getTheme());
        assertThat("Theme should be 'Theme_eXo_Dialog'",
                   sTheme.getStyleResourceId(),
                   equalTo(R.style.Theme_eXo_Dialog));

        disableLog();

    }

    @Test
    public void shouldDismissFragmentAndActivityWhenCurrentAccountIsSelected() {
        Context ctx = Robolectric.application.getApplicationContext();
        create();
        init();

        // Current account is at position 0
        ExoAccount oldA = getCurrentAccount(ctx);
        assertThat("1st account should be selected", oldA.accountName, equalTo(TEST_SERVER_NAME
                + " 1"));

        // Simulate a click on the item at position 0
        Robolectric.shadowOf(accountListView).performItemClick(0);

        ShadowActivity shadowActivity = shadowOf(activity);
        // Activity should be finishing
        assertTrue("Account Switcher activity should be finishing", shadowActivity.isFinishing());

        // There should be no intent (the screen is simply closed)
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNull("No intent should have been fired", startedIntent);

        // Same account is still selected
        ExoAccount newA = getCurrentAccount(ctx);
        assertThat("1st account should be selected", newA.accountName, equalTo(TEST_SERVER_NAME
                + " 1"));
    }

    @Test
    public void shouldSignOutCurrentAccountAndSignInSelectedAccount() {
        Context ctx = Robolectric.application.getApplicationContext();
        create();
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_PLATFORM_INFO_USER_2),
                                        getResponseOKForRequest(REQ_PLATFORM_INFO_USER_2));
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_JCR_USER_2),
                                        getResponseOKForRequest(REQ_JCR_USER_2));
        init();

        // Current account is at position 0
        ExoAccount oldA = getCurrentAccount(ctx);
        assertThat("1st account should be selected", oldA.accountName, equalTo(TEST_SERVER_NAME
                + " 1"));

        // Simulate a click on the item at position 1
        Robolectric.shadowOf(accountListView).performItemClick(1);

        ShadowActivity shadowActivity = shadowOf(activity);
        // Activity should be finishing
        assertTrue("Account Switcher activity should be finishing", shadowActivity.isFinishing());

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        // App should be redirected to the Home screen
        assertThat("Should be redirecting to Home screen after a successful login.",
                   shadowIntent.getComponent().getClassName(),
                   equalTo(HomeActivity.class.getName()));
        int flags = startedIntent.getFlags();
        final int expectedFlags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK;
        assertEquals("Intent should have flags FLAG_ACTIVITY_CLEAR_TOP, FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_CLEAR_TASK",
                     expectedFlags,
                     flags);

        // Current account is at position 1
        ExoAccount newA = getCurrentAccount(ctx);
        assertThat("2nd account should be selected", newA.accountName, equalTo(TEST_SERVER_NAME
                + " 2"));
    }

    @Test
    public void shouldDisableAutoLoginWhenLeavingAccount() {
        Context ctx = Robolectric.application.getApplicationContext();
        create();
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_PLATFORM_INFO_USER_2),
                getResponseOKForRequest(REQ_PLATFORM_INFO_USER_2));
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_JCR_USER_2),
                                        getResponseOKForRequest(REQ_JCR_USER_2));
        init();

        // Current account is at position 0
        ExoAccount acc = getCurrentAccount(ctx);
        assertTrue("AL should be enabled", acc.isAutoLoginEnabled);
        assertEquals("1st account should be selected", getAccounts(ctx).indexOf(acc), 0);

        // Switch to account at position 1
        Robolectric.shadowOf(accountListView).performItemClick(1);

        // Current account is now at position 1
        ExoAccount newA = getCurrentAccount(ctx);
        assertEquals("2nd account should be selected", getAccounts(ctx).indexOf(newA), 1);

        // Auto Login should be disabled on account 0
        // acc = getAccounts(ctx).get(0);
        acc = null;
        acc = ServerSettingHelper.getInstance().getServerInfoList(ctx).get(0);
        assertFalse("AL should be disabled", acc.isAutoLoginEnabled);
    }

    @Test
    public void shouldSignOutAndRedirectToLoginScreenWhenSwitchingFails() {
        create();
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_PLATFORM_INFO),
                                        getResponseFailedWithStatus(404));
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_JCR_USER_2),
                                        getResponseFailedWithStatus(404));
        init();

        // Current account is at position 0
        // Simulate a click on the item at position 1
        Robolectric.shadowOf(accountListView).performItemClick(1);

        // Test that the Warning dialog exists and is visible, and dismisses it
        // to continue
        Dialog warningDialog = ShadowDialog.getLatestDialog();

        assertNotNull("There should be a warning Dialog after a failed account switch",
                      warningDialog);
        assertTrue("The warning Dialog should be visible", warningDialog.isShowing());
        shadowOf(warningDialog).clickOn(R.id.warning_dialog_btn);

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        // After a failed switch, the app should be redirected to the Login
        // screen
        assertThat("Should be redirecting to Login screen after a successful login.",
                   shadowIntent.getComponent().getClassName(),
                   equalTo(LoginActivity.class.getName()));
        int flags = startedIntent.getFlags();
        final int expectedFlags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK;
        assertEquals("Intent should have flags FLAG_ACTIVITY_CLEAR_TOP, FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_CLEAR_TASK",
                     expectedFlags,
                     flags);
    }

}
