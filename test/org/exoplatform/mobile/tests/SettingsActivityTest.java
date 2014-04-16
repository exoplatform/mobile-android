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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.setting.CheckBox;
import org.exoplatform.ui.setting.CheckBoxWithImage;
import org.exoplatform.ui.setting.ServerList;
import org.exoplatform.ui.setting.SettingActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ServerItemLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 15, 2014  
 */
@RunWith(RobolectricTestRunner.class)
public class SettingsActivityTest extends ExoActivityTestUtils<SettingActivity>{
  
  CheckBox mRememberMeCbx;
  CheckBox mAutoLoginCbx;
  
  CheckBoxWithImage mEnCbx;
  CheckBoxWithImage mFrCbx;
  CheckBoxWithImage mDeCbx;
  CheckBoxWithImage mEsCbx;
  
  CheckBox mRememberFilterCbx;
  
  ServerList serverList;
  
  Button mStartCloudSignUpBtn;
  
  AccountSetting accSettings;
  ServerSettingHelper srvSettings;
  
  /**
   * Create the activity with default parameters:
   * - simulate a signed-in user
   */
  public void createWithDefaultIntent() {
    Intent i = new Intent(Robolectric.getShadowApplication().getApplicationContext(), SettingActivity.class);
    i.putExtra(ExoConstants.SETTING_TYPE, SettingActivity.PERSONAL_TYPE); // simulate signed-in user
    createWithIntent(i);
  }
  
  @Override
  public void createWithIntent(Intent i) {
    super.createWithIntent(i);
    init();
  }
  
  private void init() {
    // Login
    mRememberMeCbx  = (CheckBox) activity.findViewById(R.id.setting_remember_me_ckb);
    mAutoLoginCbx   = (CheckBox) activity.findViewById(R.id.setting_autologin_ckb);
    
    // Languages
    mEnCbx = (CheckBoxWithImage) activity.findViewById(R.id.setting_en_ckb);
    mFrCbx = (CheckBoxWithImage) activity.findViewById(R.id.setting_fr_ckb);
    mDeCbx = (CheckBoxWithImage) activity.findViewById(R.id.setting_de_ckb);
    mEsCbx = (CheckBoxWithImage) activity.findViewById(R.id.setting_es_ckb);
    
    // Social
    mRememberFilterCbx   = (CheckBox) activity.findViewById(R.id.setting_remember_filter_ckb);
    
    // Assistant
    mStartCloudSignUpBtn = (Button) activity.findViewById(R.id.setting_start_cloud_signup_btn);
    
    // Setup a server
    ArrayList<ServerObjInfo> list = new ArrayList<ServerObjInfo>();
    list.add(TEST_SERVER);
    accSettings = AccountSetting.getInstance();
    accSettings.setCurrentServer(TEST_SERVER);
    srvSettings = ServerSettingHelper.getInstance();
    srvSettings.setServerInfoList(list);
    // Server List
    serverList = (ServerList)activity.findViewById(R.id.setting_list_server);
  }

  @Override
  @Before
  public void setup() {
    controller = Robolectric.buildActivity(SettingActivity.class);
  }
  
  
  @Test
  public void verifyDefaultLayout() {
    createWithDefaultIntent();
    
    //     Login
    assertNotNull(mRememberMeCbx); // checkbox remember-me and auto-login should exist
    assertNotNull(mAutoLoginCbx);  // and be unchecked by default
    assertThat(mRememberMeCbx.isChecked(), equalTo(false));
    assertThat(mAutoLoginCbx.isChecked(), equalTo(false));
    
    //    Languages
    assertNotNull(mEnCbx); // checkbox for EN, FR, DE, ES should exist
    assertThat(mEnCbx.isChecked(), equalTo(true)); // EN is selected by default
    assertNotNull(mFrCbx);
    assertNotNull(mDeCbx);
    assertNotNull(mEsCbx);
    
    //    Social
    assertNotNull(mRememberFilterCbx);
    
    //    Server List
    assertNotNull(serverList);
//    TODO should have 1 server
//    org.fest.assertions.api.ANDROID.assertThat(serverList).hasChildCount(1); // should have 1 server
//    ServerItemLayout serverItem = (ServerItemLayout)serverList.getChildAt(0);
//    TextView serverNameView = (TextView)serverItem.findViewById(R.id.TextView_Server_Name);
//    TextView serverURLView = (TextView)serverItem.findViewById(R.id.TextView_URL);
//    org.fest.assertions.api.ANDROID.assertThat(serverNameView).containsText(TEST_SERVER_NAME);
//    org.fest.assertions.api.ANDROID.assertThat(serverURLView).containsText(TEST_SERVER_URL);
    
    //    Assistant
    assertNotNull(mStartCloudSignUpBtn);
    
    //    App Info ** 3 cells should exist
    assertNotNull(activity.findViewById(R.id.setting_server_version_value_txt));
    assertNotNull(activity.findViewById(R.id.setting_server_edition_value_txt));
    assertNotNull(activity.findViewById(R.id.setting_app_version_value_txt));
    // TODO should have correct values in the cells
    
  }
  
//  TODO @Test
  public void shouldEnableAutoLoginWhenTurningOnRememberMe() {
    
    createWithDefaultIntent();
    
    assertThat(mAutoLoginCbx.isEnabled(), equalTo(false));
    
    mRememberMeCbx.enabled(true);
    Robolectric.clickOn(mRememberMeCbx);
    
    assertThat(mRememberMeCbx.isChecked(), equalTo(true));
    assertThat(mAutoLoginCbx.isEnabled(), equalTo(true));
    assertThat(mAutoLoginCbx.isChecked(), equalTo(false));
  }
  
//  TODO @Test
  public void shouldStartExoCloudSignupAssistant() {
    
  }
  
//  TODO @Test
  public void shouldChangeLanguage() {
    
  }
  
  
  
}
