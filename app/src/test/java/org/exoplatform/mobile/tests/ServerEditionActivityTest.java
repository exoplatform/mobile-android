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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.login.LoginProxy;
import org.exoplatform.ui.setting.ServerEditionActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.shadows.ShadowView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ServerEditionActivityTest extends ExoActivityTestUtils<ServerEditionActivity> {

  TextView /* mTitleTxt, */ mServerName, mServerURL, mUsername, mPassword;

  EditText                  mServerNameEditTxt, mServerUrlEditTxt, mUserEditTxt, mPassEditTxt;

  Button                    mOkBtn, mDeleteBtn;

  ExoAccount                thisServer = null;

  @Override
  @Before
  public void setup() {
    controller = Robolectric.buildActivity(ServerEditionActivity.class);
  }

  @Override
  @After
  public void teardown() {
    Context ctx = RuntimeEnvironment.application.getApplicationContext();
    deleteAllAccounts(ctx);
    LoginProxy.userIsLoggedIn = false;
    super.teardown();
  }

  /**
   * Create the activity with a server passed in extra and configured as the
   * only and default server
   */
  public void createWithDefaultServer() {
    thisServer = getServerWithDefaultValues();
    Context ctx = RuntimeEnvironment.application.getApplicationContext();
    setDefaultServerInPreferences(ctx, thisServer);
    Intent i = new Intent(ctx, ServerEditionActivity.class);
    i.putExtra("EXO_SERVER_OBJ", thisServer);
    super.createWithIntent(i);
    init();
  }

  private void init() {
    // mTitleTxt = (TextView)
    // activity.findViewById(R.id.server_setting_title_txt);
    mServerName = (TextView) activity.findViewById(R.id.server_setting_server_name_txt);
    mServerURL = (TextView) activity.findViewById(R.id.server_setting_server_url_txt);
    mUsername = (TextView) activity.findViewById(R.id.server_setting_user_txt);
    mPassword = (TextView) activity.findViewById(R.id.server_setting_pass_txt);
    mServerNameEditTxt = (EditText) activity.findViewById(R.id.server_setting_server_name_edit_txt);
    mServerUrlEditTxt = (EditText) activity.findViewById(R.id.server_setting_server_url_edit_txt);
    mUserEditTxt = (EditText) activity.findViewById(R.id.server_setting_user_edit_txt);
    mPassEditTxt = (EditText) activity.findViewById(R.id.server_setting_pass_edit_txt);
    mOkBtn = (Button) activity.findViewById(R.id.server_setting_ok_btn);
    mDeleteBtn = (Button) activity.findViewById(R.id.server_setting_delete_btn);
  }

  @Test
  public void verifyDefaultLayout() {
    createWithDefaultServer();

    // TODO check
    assertTrue("Incorrect activity title", thisServer.accountName.equalsIgnoreCase(activity.getTitle().toString()));
    assertThat(mServerName).containsText(R.string.ServerName);
    assertThat(mServerNameEditTxt).containsText(thisServer.accountName);
    assertThat(mServerURL).containsText(R.string.ServerUrl);
    assertThat(mServerUrlEditTxt).containsText(thisServer.serverUrl);
    assertThat(mUsername).containsText(R.string.UserNameCellTitle);
    assertThat(mUsername).hasHint(null); // hint is null because username is
                                         // set
    assertThat(mUserEditTxt).containsText(thisServer.username);
    assertThat(mPassword).containsText(R.string.PasswordCellTitle);
    assertThat(mPassword).hasHint(null); // hint is null because password is
                                         // set
    assertThat(mPassEditTxt).containsText(thisServer.password);
    assertThat(mOkBtn).isVisible().isClickable().containsText(R.string.OK);
    assertThat(mDeleteBtn).isVisible().isClickable().containsText(R.string.Delete);

  }

  @Test
  public void verifyDefaultLayoutWithLimitedEdit() {
    LoginProxy.userIsLoggedIn = true; // simulate signed-in user
    createWithDefaultServer();
    // Server name is editable and contains the current account name
    assertThat(mServerNameEditTxt).containsText(thisServer.accountName);
    assertThat(mServerNameEditTxt).isEnabled();

    // Server URL is *not* editable and contains the current server URL
    assertThat(mServerUrlEditTxt).containsText(thisServer.serverUrl);
    assertThat(mServerUrlEditTxt).isDisabled().overridingErrorMessage("Server URL field should be disabled");

    // Account username is *not* editable and contains the current account
    // username
    assertThat(mUserEditTxt).containsText(thisServer.username);
    assertThat(mUserEditTxt).isDisabled().overridingErrorMessage("Username field should be disabled");

    // Account password is *not* editable and contains the current account
    // password
    assertThat(mPassEditTxt).containsText(thisServer.password);
    assertThat(mPassEditTxt).isDisabled().overridingErrorMessage("Password field should be disabled");

    // Delete button is not displayed
    assertThat(mDeleteBtn).isGone().overridingErrorMessage("Delete button should be hidden");
  }

  @Test
  public void verifyEditOneAccount() {

    createWithDefaultServer();
    Context ctx = RuntimeEnvironment.application.getApplicationContext();
    final String newName = TEST_SERVER_NAME + " new";
    final String newURL = TEST_SERVER_URL + ".fr";
    final String newUser = TEST_USER_NAME + "_new";
    final String newPass = TEST_USER_PWD + "_new";

    assertThat("1 server should already exist", ServerSettingHelper.getInstance().getServerInfoList(ctx).size(), equalTo(1));

    mServerNameEditTxt.setText(newName);
    mServerUrlEditTxt.setText(newURL);
    mUserEditTxt.setText(newUser);
    mPassEditTxt.setText(newPass);

    ShadowView.clickOn(mOkBtn);

    assertThat("Only 1 server should exist", ServerSettingHelper.getInstance().getServerInfoList(ctx).size(), equalTo(1));
    assertTrue("Should have displayed toast ServerUpdated but displayed '" + ShadowToast.getTextOfLatestToast() + "' instead.",
               ShadowToast.showedToast(ctx.getResources().getString(R.string.ServerUpdated)));

    ExoAccount srv = ServerSettingHelper.getInstance().getServerInfoList(ctx).get(0);

    assertThat("Account name should have been modified", srv.accountName, equalTo(newName));
    assertThat("Account server should have been modified", srv.serverUrl, equalTo(newURL));
    assertThat("Account username should have been modified", srv.username, equalTo(newUser));
    assertThat("Account password should have been modified", srv.password, equalTo(newPass));
  }

  @Test
  public void verifyDeleteOneAccount() {
    createWithDefaultServer();
    Context ctx = RuntimeEnvironment.application.getApplicationContext();

    assertThat("1 server should already exist", ServerSettingHelper.getInstance().getServerInfoList(ctx).size(), equalTo(1));

    ShadowView.clickOn(mDeleteBtn);

    assertThat("Server should have been deleted", ServerSettingHelper.getInstance().getServerInfoList(ctx).size(), equalTo(0));
    assertTrue("Should have displayed toast ServerDeleted but displayed '" + ShadowToast.getTextOfLatestToast() + "' instead.",
               ShadowToast.showedToast(ctx.getResources().getString(R.string.ServerDeleted)));
  }

  @Test
  public void verifyEditAccountFailsWithIncorrectAccountName() {
    createWithDefaultServer();
    Context ctx = RuntimeEnvironment.application.getApplicationContext();

    final String newInvalidAccountName = TEST_SERVER_NAME + " ** new";

    mServerNameEditTxt.setText(newInvalidAccountName);

    ShadowView.clickOn(mOkBtn);

    ExoAccount srv = ServerSettingHelper.getInstance().getServerInfoList(ctx).get(0);

    assertThat("Account name should NOT have been modified", srv.accountName, equalTo(TEST_SERVER_NAME));
    assertTrue("Should have displayed toast AccountNameInvalid but displayed '" + ShadowToast.getTextOfLatestToast()
        + "' instead.", ShadowToast.showedToast(ctx.getResources().getString(R.string.AccountNameInvalid)));
  }

  @Test
  public void verifyEditAccountFailsWithIncorrectAccountServer() {
    createWithDefaultServer();
    Context ctx = RuntimeEnvironment.application.getApplicationContext();

    final String newInvalidAccountServer = TEST_WRONG_SERVER_URL;

    mServerUrlEditTxt.setText(newInvalidAccountServer);

    ShadowView.clickOn(mOkBtn);

    ExoAccount srv = ServerSettingHelper.getInstance().getServerInfoList(ctx).get(0);

    assertThat("Account Server URL should NOT have been modified", srv.serverUrl, equalTo(TEST_SERVER_URL));
    assertTrue("Should have displayed toast AccountServerInvalid but displayed '" + ShadowToast.getTextOfLatestToast()
        + "' instead.", ShadowToast.showedToast(ctx.getResources().getString(R.string.AccountServerInvalid)));
  }

  @Test
  public void verifyEditAccountFailsWithForbiddenAccountServer() {
    createWithDefaultServer();
    Context ctx = RuntimeEnvironment.application.getApplicationContext();

    final String newForbiddenAccountServer = "http://exoplatform.net";

    mServerUrlEditTxt.setText(newForbiddenAccountServer);

    ShadowView.clickOn(mOkBtn);

    ExoAccount srv = ServerSettingHelper.getInstance().getServerInfoList(ctx).get(0);

    assertThat("Account Server URL should NOT have been modified", srv.serverUrl, equalTo(TEST_SERVER_URL));
    assertTrue("Should have displayed toast AccountServerForbidden but displayed '" + ShadowToast.getTextOfLatestToast()
        + "' instead.", ShadowToast.showedToast(ctx.getResources().getString(R.string.AccountServerForbidden)));
  }

  @Test
  public void verifyEditAccountFailsWithIncorrectAccountUserName() {
    createWithDefaultServer();
    Context ctx = RuntimeEnvironment.application.getApplicationContext();

    final String newInvalidUsername = TEST_USER_NAME + " ** new";

    mUserEditTxt.setText(newInvalidUsername);

    ShadowView.clickOn(mOkBtn);

    ExoAccount srv = ServerSettingHelper.getInstance().getServerInfoList(ctx).get(0);

    assertThat("Account username should NOT have been modified", srv.username, equalTo(TEST_USER_NAME));
    assertTrue("Should have displayed toast AccountUsernameInvalid but displayed '" + ShadowToast.getTextOfLatestToast()
        + "' instead.", ShadowToast.showedToast(ctx.getResources().getString(R.string.AccountUsernameInvalid)));
  }

  @Test
  public void verifyAccountIsSelectedWhenOnlyOneRemainsAfterDelete() {
    Context ctx = RuntimeEnvironment.application.getApplicationContext();

    // Create 2 accounts
    thisServer = getServerWithDefaultValues();
    thisServer.accountName = TEST_SERVER_NAME + " one";
    thisServer.serverUrl = TEST_SERVER_URL + ".net";

    ExoAccount acc2 = getServerWithDefaultValues();
    acc2.accountName = TEST_SERVER_NAME + " two";
    acc2.serverUrl = TEST_SERVER_URL + ".org";

    ArrayList<ExoAccount> servers = new ArrayList<ExoAccount>(2);
    servers.add(thisServer);
    servers.add(acc2);
    addServersInPreferences(ctx, servers);

    // Select the 1st account
    SharedPreferences.Editor prefs = ctx.getSharedPreferences("exo_preference", 0).edit();
    prefs.putString("exo_prf_domain_index", "0");
    prefs.commit();

    // Start activity with thisServer (the 1st account)
    Intent i = new Intent(ctx, ServerEditionActivity.class);
    i.putExtra("EXO_SERVER_OBJ", thisServer);
    super.createWithIntent(i);
    init();

    // Delete the current account (the 1st)
    ShadowView.clickOn(mDeleteBtn);

    ExoAccount remainingAcc = AccountSetting.getInstance().getCurrentAccount();

    Log.d(TAG_TEST, remainingAcc.accountName);
    Log.d(TAG_TEST, acc2.accountName);

    assertTrue("The 2nd account should have been selected automatically when the 1st was deleted", acc2.equals(remainingAcc));

  }

}
