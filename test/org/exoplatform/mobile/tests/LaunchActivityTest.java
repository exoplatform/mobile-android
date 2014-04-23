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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.shadowOf;

import org.exoplatform.ui.LaunchActivity;
import org.exoplatform.ui.WelcomeActivity;

import android.content.Intent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 8, 2014  
 */
@RunWith(ExoRobolectricTestRunner.class)
public class LaunchActivityTest extends ExoActivityTestUtils<LaunchActivity> {
  
  @Test
  /**
   * Tests that on an clean launch, the app is redirected to the Welcome Activity (sign-up assistant)
   * @throws Exception
   */
  public void shouldRedirectToWelcomeScreen() throws Exception 
  {
     create();
     
      ShadowActivity sActivity = shadowOf(activity);
      Intent welcomeIntent = sActivity.getNextStartedActivity();
      ShadowIntent sIntent = shadowOf(welcomeIntent);
      
      assertThat(sIntent.getComponent().getClassName(), equalTo(WelcomeActivity.class.getName()));
      
  }

  @Override
  @Before
  public void setup() {
    controller = Robolectric.buildActivity(LaunchActivity.class);
  }
  
//  @Test
//  public void shouldRedirectToLoginScreen() throws Exception 
//  {
//    ServerObjInfo srv = new ServerObjInfo();
//    srv.serverName = "testserver";
//    srv.serverUrl = "http://test.com";
//    srv.username = "testuser";
//    srv.password = "testpwd";
//    srv.isAutoLoginEnabled = false;
//    srv.isRememberEnabled = false;
//    AccountSetting settings = AccountSetting.getInstance();
//    settings.setCurrentServer(srv);
//     
//     create();
//      
//      ShadowActivity sActivity = shadowOf(activity);
//      Intent loginIntent = sActivity.getNextStartedActivity();
//      ShadowIntent sIntent = shadowOf(loginIntent);
//      
//      assertThat(sIntent.getComponent().getClassName(), equalTo(LoginActivity.class.getName()));
//  }
  
}
