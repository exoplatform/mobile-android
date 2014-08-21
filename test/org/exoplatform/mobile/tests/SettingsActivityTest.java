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
import static org.robolectric.Robolectric.shadowOf;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.WelcomeActivity;
import org.exoplatform.ui.setting.CheckBox;
import org.exoplatform.ui.setting.CheckBoxWithImage;
import org.exoplatform.ui.setting.ServerList;
import org.exoplatform.ui.setting.SettingActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.ServerItemLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 15, 2014  
 */
@RunWith(ExoRobolectricTestRunner.class)
public class SettingsActivityTest extends ExoActivityTestUtils<SettingActivity>{
  
  final String SRV_VERSION = "4.0.0";
  final String SRV_EDITION = "Enterprise";
  final String APP_VERSION = "2.5.0";
  
  CheckBox mRememberMeCbx;
  CheckBox mAutoLoginCbx;
  
  CheckBoxWithImage mEnCbx;
  CheckBoxWithImage mFrCbx;
  CheckBoxWithImage mDeCbx;
  CheckBoxWithImage mEsCbx;
  
  CheckBox mRememberFilterCbx;
  
  ServerList serverList;
  
  // now it's the New Accounts button, under the accounts list
  Button mStartCloudSignUpBtn;
  
  ServerSettingHelper srvSettings;
  
  /**
   * Create the activity with default parameters:
   * - simulate a signed-in user
   */
  public void createWithDefaultIntent() {
    initSettings();
    Intent i = new Intent(Robolectric.getShadowApplication().getApplicationContext(), SettingActivity.class);
    i.putExtra(ExoConstants.SETTING_TYPE, SettingActivity.PERSONAL_TYPE); // simulate signed-in user
    createWithIntent(i);
    init();
  }
  
  private void initSettings() {
    // Setup and save a server
    srvSettings = ServerSettingHelper.getInstance();
    ServerObjInfo srv = getServerWithDefaultValues();
    ArrayList<ServerObjInfo> list = new ArrayList<ServerObjInfo>();
    list.add(srv);
    srvSettings.setServerInfoList(list);
    SettingUtils.persistServerSetting(Robolectric.getShadowApplication().getApplicationContext());
    AccountSetting.getInstance().setCurrentServer(srv);
    AccountSetting.getInstance().setDomainIndex("0");
    
    // Set server version, edition and app version
    srvSettings.setServerVersion(SRV_VERSION);
    srvSettings.setServerEdition(SRV_EDITION);
    srvSettings.setApplicationVersion(APP_VERSION);
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
    mStartCloudSignUpBtn = (Button) activity.findViewById(R.id.setting_new_account_btn);
    
    // Server List
    serverList = (ServerList)activity.findViewById(R.id.setting_list_accounts);
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
    assertThat(mRememberMeCbx.isChecked(), equalTo(true));
    assertThat(mAutoLoginCbx.isChecked(), equalTo(true));
    
    //    Languages
    assertNotNull(mEnCbx); // checkboxes for EN, FR, DE, ES should exist
    assertThat(mEnCbx.isChecked(), equalTo(true)); // EN is selected by default
    assertNotNull(mFrCbx);
    assertThat(mFrCbx.isChecked(), equalTo(false));
    assertNotNull(mDeCbx);
    assertThat(mDeCbx.isChecked(), equalTo(false));
    assertNotNull(mEsCbx);
    assertThat(mEsCbx.isChecked(), equalTo(false));
    
    //    Social
    assertNotNull(mRememberFilterCbx);
    
    //    Server List
    assertThat(srvSettings.getServerInfoList(activity).isEmpty(), equalTo(false)); // should have 1 server    
    org.fest.assertions.api.ANDROID.assertThat(serverList).hasChildCount(1); 
    ServerItemLayout serverItem = (ServerItemLayout)serverList.getChildAt(0);
    org.fest.assertions.api.ANDROID.assertThat(serverItem.serverName).containsText(TEST_SERVER_NAME);
    org.fest.assertions.api.ANDROID.assertThat(serverItem.serverUrl).containsText(TEST_SERVER_URL);
    
    //    Assistant
    assertNotNull(mStartCloudSignUpBtn);
    
    //    App Info ** 3 cells should exist and contain default values set in initSettings()
    TextView srvVersion = (TextView)activity.findViewById(R.id.setting_server_version_value_txt);
    TextView srvEdition = (TextView)activity.findViewById(R.id.setting_server_edition_value_txt);
    TextView appVersion = (TextView)activity.findViewById(R.id.setting_app_version_value_txt);
    org.fest.assertions.api.ANDROID.assertThat(appVersion).containsText(APP_VERSION);
    org.fest.assertions.api.ANDROID.assertThat(srvVersion).containsText(SRV_VERSION);
    org.fest.assertions.api.ANDROID.assertThat(srvEdition).containsText(SRV_EDITION);
    

  }
  
  @Test
  public void shouldChangeAutoLoginWhenChangingRememberMe() {
    
    createWithDefaultIntent();
    
    Robolectric.clickOn(mRememberMeCbx); //turning off remember me
    
    assertThat(mRememberMeCbx.isChecked(), equalTo(false));
    assertThat(mAutoLoginCbx.isEnabled(), equalTo(false));
    assertThat(mAutoLoginCbx.isChecked(), equalTo(false));
    
    Robolectric.clickOn(mRememberMeCbx); // turning on remember me
    
    assertThat(mRememberMeCbx.isChecked(), equalTo(true));
    assertThat(mAutoLoginCbx.isEnabled(), equalTo(true));
    assertThat(mAutoLoginCbx.isChecked(), equalTo(false));
    
    Robolectric.clickOn(mAutoLoginCbx); // turning on auto login
    
    assertThat(mAutoLoginCbx.isEnabled(), equalTo(true));
    assertThat(mAutoLoginCbx.isChecked(), equalTo(true));
  }
  
  @Test
  public void shouldStartExoCloudSignupAssistant() {
    
    createWithDefaultIntent();
    
    Robolectric.clickOn(mStartCloudSignUpBtn);
    
    ShadowActivity sActivity = shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);
    
    assertThat(sIntent.getComponent().getClassName(), equalTo(WelcomeActivity.class.getName()));
    
  }
  
  @Test
  public void shouldChangeLanguage() {
    
    createWithDefaultIntent();
    
    SharedPreferences prefs = activity.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    
    Robolectric.clickOn(mDeCbx); // turn on German
    assertThat(mEnCbx.isChecked(), equalTo(false)); // English is off
    assertThat(mDeCbx.isChecked(), equalTo(true));
    assertThat(prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, ""), equalTo(ExoConstants.GERMAN_LOCALIZATION));
    
    Robolectric.clickOn(mFrCbx); // turn on French
    assertThat(mDeCbx.isChecked(), equalTo(false)); // German is off
    assertThat(mFrCbx.isChecked(), equalTo(true));
    assertThat(prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, ""), equalTo(ExoConstants.FRENCH_LOCALIZATION));
    
    Robolectric.clickOn(mEsCbx); // turn on Spanish
    assertThat(mFrCbx.isChecked(), equalTo(false)); // French is off
    assertThat(mEsCbx.isChecked(), equalTo(true));
    assertThat(prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, ""), equalTo(ExoConstants.SPANISH_LOCALIZATION));
    
    Robolectric.clickOn(mEnCbx); // turn on English
    assertThat(mEsCbx.isChecked(), equalTo(false)); // Spanish is off
    assertThat(mEnCbx.isChecked(), equalTo(true));
    assertThat(prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, ""), equalTo(ExoConstants.ENGLISH_LOCALIZATION));
  }
  
  
  
}
