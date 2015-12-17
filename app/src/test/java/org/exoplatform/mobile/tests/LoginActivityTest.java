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

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.login.AccountPanel;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.ui.login.ServerPanel;
import org.exoplatform.utils.ExoConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Apr 10, 2014
 */
public class LoginActivityTest extends ExoActivityTestUtils<LoginActivity> {

  ImageView           accountBtn, serverBtn;

  AccountPanel        accPanel;

  ServerPanel         srvPanel;

  Button              loginBtn;

  EditText            user, pass;

  ServerSettingHelper srvSettings;

  @Before
  public void setup() {
    controller = Robolectric.buildActivity(LoginActivity.class);
    deleteAllAccounts(ShadowApplication.getInstance().getApplicationContext());
  }

  @After
  public void teardown() {
    deleteAllAccounts(ShadowApplication.getInstance().getApplicationContext());
    super.teardown();
  }

  @Override
  public void create() {
    super.create();
    init();
  }

  @Override
  public void createWithIntent(Intent i) {
    super.createWithIntent(i);
    init();
  }

  @Override
  public void createWithContext(Context ctx) {
    super.createWithContext(ctx);
    init();
  }

  private void init() {
    accountBtn = (ImageView) activity.findViewById(R.id.login_account_btn);
    serverBtn = (ImageView) activity.findViewById(R.id.login_server_btn);
    accPanel = (AccountPanel) activity.findViewById(R.id.login_account_panel);
    srvPanel = (ServerPanel) activity.findViewById(R.id.login_server_panel);
    loginBtn = (Button) activity.findViewById(R.id.Button_Login);
    user = (EditText) activity.findViewById(R.id.EditText_UserName);
    pass = (EditText) activity.findViewById(R.id.EditText_Password);
  }

  @Test
  public void verifyDefaultLayout() {
    create();

    ImageView logo = (ImageView) activity.findViewById(R.id.login_exo_logo);
    assertNotNull(logo); // exo logo should exist

    assertNotNull(accountBtn); // account and server tabs should exist
    assertNotNull(serverBtn); // and should be clickable
    assertThat(accountBtn).isClickable();
    assertThat(serverBtn).isClickable();
    assertThat(accountBtn).isVisible();
    assertThat(serverBtn).isInvisible(); // with 0 account, the switch
                                         // accounts button is invisible

    assertNotNull(accPanel); // account and server panels should exist
    assertNotNull(srvPanel); // and only account panel is visible
    assertThat(accPanel).isVisible();
    assertThat(srvPanel).isInvisible();

    assertNotNull(loginBtn); // login button should exist and be disabled
    assertThat(loginBtn).isDisabled();
    assertThat(loginBtn).containsText("Connect"); // only Connect when no
                                                  // account is selected

    assertNotNull(user); // username and password fields should exist
    assertNotNull(pass); // and should be empty
    assertThat(user).isEmpty();
    assertThat(pass).isEmpty();

  }

  @Test
  public void shouldDisplayPanelWhenTappingButton() {

    ArrayList<ExoAccount> list = createXAccounts(2);
    ServerSettingHelper.getInstance().setServerInfoList(list);

    create();

    ShadowView.clickOn(serverBtn); // tap the accounts/server button
    assertThat(srvPanel).isVisible(); // should display accounts panel
    assertThat(accPanel).isInvisible(); // should hide login panel
    assertThat(serverBtn).isSelected(); // account switcher button should be
                                        // selected
    assertThat(accountBtn).isNotSelected(); // login panel button should not
                                            // be selected

    ShadowView.clickOn(accountBtn); // tap the login panel button
    assertThat(srvPanel).isInvisible(); // should hide servers/accounts
                                        // panel
    assertThat(accPanel).isVisible(); // should display login panel
    assertThat(serverBtn).isNotSelected(); // account switcher button should
                                           // not be selected
    assertThat(accountBtn).isSelected(); // login panel button should be
                                         // selected
  }

  @Test
  public void shouldDisplaySwitchIconWhenAddingSecondAccount() {

    // create only 1 account
    ServerSettingHelper.getInstance().setServerInfoList(createXAccounts(1));

    create();

    // switch button should be invisible
    assertThat(serverBtn).isInvisible();

    // create a 2nd account
    ServerSettingHelper.getInstance().setServerInfoList(createXAccounts(2));

    // switch button should be visible after the activity is resumed
    controller.pause().resume();
    assertThat(serverBtn).isVisible();
  }

  @Test
  public void shouldActivateLoginButtonWhenUsernamePasswordAreTyped() {
    create();

    user.setText(TEST_USER_NAME);
    pass.setText(TEST_USER_PWD);

    assertThat(loginBtn).isEnabled(); // login button should be enabled

  }

  @Test
  public void shouldAppendAccountNameToLoginButtonLabel() {
    Context ctx = ShadowApplication.getInstance().getApplicationContext();
    // create 1 account named testserver (TEST_SERVER_NAME)
    ExoAccount account = getServerWithDefaultValues();
    setDefaultServerInPreferences(ctx, account);
    create();
    AccountSetting.getInstance().setCurrentAccount(account);

    // open the login panel to refresh the button label
    ShadowView.clickOn(accountBtn);

    // the label on the button should be "Connect to testserver"
    assertThat(loginBtn).containsText("Connect to testserver");
  }

  @Test
  public void shouldPopulateServerAndUsernameWhenStartingFromUrl() {

    Context ctx = ShadowApplication.getInstance().getApplicationContext();

    Intent i = new Intent(ctx, LoginActivity.class);
    Uri uri = new Uri.Builder().scheme("exomobile"). // exomobile://
                               encodedAuthority(ExoConstants.EXO_URL_USERNAME + "=" + TEST_USER_NAME)
                               . // username=testuser
                               appendQueryParameter(ExoConstants.EXO_URL_SERVER, TEST_SERVER_URL)
                               .build(); // ?serverUrl=http://www.test.com
    i.setData(uri);
    createWithIntent(i);

    user = (EditText) activity.findViewById(R.id.EditText_UserName);
    // username text field should contain the value passed in the URL
    assertThat(user).containsText(TEST_USER_NAME);
    ServerSettingHelper helper = ServerSettingHelper.getInstance();
    org.junit.Assert.assertThat(helper.getServerInfoList(ctx).isEmpty(), equalTo(false));
    ExoAccount srv = helper.getServerInfoList(ctx).get(0);
    // server URL should be that passed in the start URL
    org.junit.Assert.assertThat(srv.serverUrl, equalTo(TEST_SERVER_URL));

  }

  @Test
  public void shouldSetUsernamePasswordInTextfieldsWhenRememberMeIsOn() {
    Context ctx = ShadowApplication.getInstance().getApplicationContext();
    // create 1 account named testserver (TEST_SERVER_NAME)
    ExoAccount account = getServerWithDefaultValues();
    account.isRememberEnabled = true;
    setDefaultServerInPreferences(ctx, account);
    create();
    AccountSetting.getInstance().setCurrentAccount(account);

    // open the login panel to refresh the content
    ShadowView.clickOn(accountBtn);

    assertThat(user).hasTextString(TEST_USER_NAME);
    assertThat(pass).hasTextString(TEST_USER_PWD);

  }

  @Test
  public void shouldSetNothingInTextfieldsWhenRememberMeIsOff() {
    Context ctx = ShadowApplication.getInstance().getApplicationContext();
    // create 1 account named testserver (TEST_SERVER_NAME)
    ExoAccount account = getServerWithDefaultValues();
    setDefaultServerInPreferences(ctx, account);
    create();
    AccountSetting.getInstance().setCurrentAccount(account);

    // open the login panel to refresh the content
    ShadowView.clickOn(accountBtn);

    assertThat(user).hasTextString("");
    assertThat(pass).hasTextString("");
  }

}
