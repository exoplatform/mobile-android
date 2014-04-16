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
import static org.junit.Assert.assertNotNull;

import org.exoplatform.R;
import org.exoplatform.ui.login.AccountPanel;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.ui.login.ServerPanel;
import org.exoplatform.utils.ExoConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
//import static org.junit.Assert.assertThat;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 10, 2014  
 */
@RunWith(RobolectricTestRunner.class)
public class LoginActivityTest extends ExoActivityTestUtils<LoginActivity> {

  ImageView accountBtn, serverBtn;
  AccountPanel accPanel;
  ServerPanel srvPanel;
  Button loginBtn;
  EditText user, pass;
  
  @Before
  public void setup() {
    controller = Robolectric.buildActivity(LoginActivity.class);
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
  
  private void init() {
    accountBtn = (ImageView)activity.findViewById(R.id.login_account_btn);
    serverBtn = (ImageView)activity.findViewById(R.id.login_server_btn);
    accPanel = (AccountPanel)activity.findViewById(R.id.login_account_panel);
    srvPanel = (ServerPanel)activity.findViewById(R.id.login_server_panel);
    loginBtn = (Button)activity.findViewById(R.id.Button_Login);
    user = (EditText)activity.findViewById(R.id.EditText_UserName);
    pass = (EditText)activity.findViewById(R.id.EditText_Password);
  }
  
  @Test
  public void verifyDefaultLayout() {
    create();
    
    ImageView logo = (ImageView)activity.findViewById(R.id.login_exo_logo);
    assertNotNull(logo); // exo logo should exist
    
    assertNotNull(accountBtn); // account and server tabs should exist
    assertNotNull(serverBtn);  // and should be clickable
    assertThat(accountBtn).isClickable();
    assertThat(serverBtn).isClickable();
    
    assertNotNull(accPanel); // account and server panels should exist
    assertNotNull(srvPanel); // and only account panel is visible
    assertThat(accPanel).isVisible();
    assertThat(srvPanel).isInvisible();
    
    assertNotNull(loginBtn); // login button should exist and be disabled
    assertThat(loginBtn).isNotActivated();
    
    assertNotNull(user); // username and password fields should exist
    assertNotNull(pass); // and should be empty
    assertThat(user).isEmpty();
    assertThat(pass).isEmpty();
    
  }
  
  @Test
  public void shouldDisplayPanelWhenTappingButton() {
    create();
    
    serverBtn.performClick(); // should hide account, display server panel and select button when clicking the server button
    assertThat(srvPanel).isVisible();
    assertThat(accPanel).isInvisible();
    assertThat(serverBtn).isSelected();
    assertThat(accountBtn).isNotSelected();
    
    accountBtn.performClick(); // should hide server, display account panel and select button when clicking the account button
    assertThat(srvPanel).isInvisible();
    assertThat(accPanel).isVisible();
    assertThat(serverBtn).isNotSelected();
    assertThat(accountBtn).isSelected();
  }
  
  @Test
  public void shouldActivateLoginButtonWhenUsernamePasswordAreTyped() {
    create();
    
    user.setText(TEST_USER_NAME);
    pass.setText(TEST_USER_PWD);
    
    assertThat(loginBtn).isEnabled(); // login button should be enabled
    
  }
  
  @Test
  public void shouldPopulateServerAndUsernameWhenStartingFromUrl() {
    
    Intent i = new Intent(Robolectric.getShadowApplication().getApplicationContext(), LoginActivity.class);
    Uri uri = new Uri.Builder(). 
        scheme("exomobile").                                                          // exomobile://
        encodedAuthority(ExoConstants.EXO_URL_USERNAME+"="+TEST_USER_NAME).           // username=testuser
        appendQueryParameter(ExoConstants.EXO_URL_SERVER, TEST_SERVER_URL).build();   // ?serverUrl=http://www.test.com

    i.setData(uri);
    
    createWithIntent(i);
    
    user = (EditText)activity.findViewById(R.id.EditText_UserName);
    assertThat(user).containsText(TEST_USER_NAME);
    
  }
  
}
