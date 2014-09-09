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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.accountswitcher.AccountSwitcherActivity;
import org.exoplatform.accountswitcher.AccountSwitcherFragment;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.ui.HomeActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Sep 9, 2014  
 */
@RunWith(ExoRobolectricTestRunner.class)
public class AccountSwitcherTest extends ExoActivityTestUtils<AccountSwitcherActivity> {

  final String FRAGMENT_TAG = "account_switcher_fragment_dialog";
  
  AccountSwitcherFragment accountSwitcherFragment;
  ListView accountListView;
  TextView titleTextView;
  ArrayList<ExoAccount> accounts;
  
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
  
  public void init() {
    accountSwitcherFragment = (AccountSwitcherFragment) activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    accountListView = (ListView)activity.findViewById(R.id.account_list_view);
    titleTextView = (TextView)activity.findViewById(R.id.account_switcher_fragment_title);
  }

  @Test
  public void verifyDefaultLayout() {
    create();
    init();
    
    assertThat(accountSwitcherFragment).isAdded().isVisible().isNotDetached().hasTag(FRAGMENT_TAG);
    String titleText = titleTextView.getText().toString();
    assertTrue("Title should be capitalized", Character.isUpperCase(titleText.charAt(0)));
    assertTrue("Title should be 'Accounts'", titleText.equalsIgnoreCase(activity.getResources().getString(R.string.Server)));
    
    final int numberOfAccounts = 2;
    assertThat(accountListView).hasCount(numberOfAccounts);
    
    for (int i=0; i<numberOfAccounts; i++) {
      View v = accountListView.getAdapter().getView(i, null, accountListView);
      TextView name = (TextView)v.findViewById(R.id.account_name_textview);
      assertThat(name).containsText(TEST_SERVER_NAME.toUpperCase()+" "+(i+1));
      TextView server = (TextView)v.findViewById(R.id.account_server_textview);
      assertThat(server).containsText(TEST_SERVER_URL);
      TextView username = (TextView)v.findViewById(R.id.account_username_textview);
      assertThat(username).containsText(TEST_USER_NAME+"_"+(i+1));
    }
  }
  
//  @Test TODO 
  public void shouldDismissFragmentAndActivityWhenCurrentAccountIsSelected() {
    create();
    init();
    
    // Current account is at position 0
    // Simulate a click on the item at position 0
    Robolectric.shadowOf(accountListView).performItemClick(0);

  }
  
  @Test
  public void shouldSignOutCurrentAccountAndSignInSelectedAccount() {
    create();
    Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_PLATFORM_INFO), getResponseOKForRequest(REQ_PLATFORM_INFO));
    Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_JCR_USER_2), getResponseOKForRequest(REQ_JCR_USER_2));
    init();
    
    // Current account is at position 0
    // Simulate a click on the item at position 1
    Robolectric.shadowOf(accountListView).performItemClick(1);
    
    ShadowActivity shadowActivity = shadowOf(activity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();
    ShadowIntent shadowIntent = shadowOf(startedIntent);
    // After successful login, the app should be redirected to the Home screen
    assertThat("Should be redirecting to Home screen after a successful login.", shadowIntent.getComponent().getClassName(), equalTo(HomeActivity.class.getName()));
    int flags = startedIntent.getFlags();
    final int expectedFlags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;
    assertEquals("Intent should have flags FLAG_ACTIVITY_CLEAR_TOP and FLAG_ACTIVITY_NEW_TASK.", expectedFlags, flags);
  }

}
