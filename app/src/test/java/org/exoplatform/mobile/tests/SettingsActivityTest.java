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
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.WelcomeActivity;
import org.exoplatform.ui.setting.CheckBox;
import org.exoplatform.ui.setting.CheckBoxWithImage;
import org.exoplatform.ui.setting.ServerEditionActivity;
import org.exoplatform.ui.setting.ServerList;
import org.exoplatform.ui.setting.SettingActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.ServerItemLayout;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Apr 15, 2014
 */
public class SettingsActivityTest extends ExoActivityTestUtils<SettingActivity> {

  final String        SRV_VERSION = "4.0.0";

  final String        SRV_EDITION = "Enterprise";

  final String        APP_VERSION = "2.5.0";

  CheckBox            mRememberMeCbx;

  CheckBox            mAutoLoginCbx;

  CheckBoxWithImage   mEnCbx, mFrCbx, mDeCbx, mEsCbx, mPtBrCbx, mElCbx;

  CheckBox            mRememberFilterCbx;

  ServerList          serverList;

  // now it's the New Accounts button, under the accounts list
  Button              mStartCloudSignUpBtn;

  ServerSettingHelper srvSettings;

  /**
   * Create the activity with default parameters.
   * 
   * @param online whether we start the activity as an online user or not
   */
  public void createWithDefaultIntent(boolean online) {
    initSettings();
    Intent i = new Intent(ShadowApplication.getInstance().getApplicationContext(), SettingActivity.class);
    int settingType = online ? SettingActivity.PERSONAL_TYPE : SettingActivity.GLOBAL_TYPE;
    i.putExtra(ExoConstants.SETTING_TYPE, settingType);

    createWithIntent(i);
    init();
  }

  private void initSettings() {
    // Setup and save a server
    srvSettings = ServerSettingHelper.getInstance();
    ExoAccount srv = getServerWithDefaultValues();
    ArrayList<ExoAccount> list = new ArrayList<ExoAccount>();
    list.add(srv);
    srvSettings.setServerInfoList(list);
    SettingUtils.persistServerSetting(ShadowApplication.getInstance().getApplicationContext());
    AccountSetting.getInstance().setCurrentAccount(srv);
    AccountSetting.getInstance().setDomainIndex("0");

    // Set server version, edition and app version
    srvSettings.setServerVersion(SRV_VERSION);
    srvSettings.setServerEdition(SRV_EDITION);
    srvSettings.setApplicationVersion(APP_VERSION);
  }

  private void init() {
    // Login
    mRememberMeCbx = (CheckBox) activity.findViewById(R.id.setting_remember_me_ckb);
    mAutoLoginCbx = (CheckBox) activity.findViewById(R.id.setting_autologin_ckb);

    // Languages
    mEnCbx = (CheckBoxWithImage) activity.findViewById(R.id.setting_en_ckb);
    mFrCbx = (CheckBoxWithImage) activity.findViewById(R.id.setting_fr_ckb);
    mDeCbx = (CheckBoxWithImage) activity.findViewById(R.id.setting_de_ckb);
    mEsCbx = (CheckBoxWithImage) activity.findViewById(R.id.setting_es_ckb);
    mPtBrCbx = (CheckBoxWithImage) activity.findViewById(R.id.setting_pt_br_ckb);
    mElCbx = (CheckBoxWithImage) activity.findViewById(R.id.setting_el_ckb);

    // Social
    mRememberFilterCbx = (CheckBox) activity.findViewById(R.id.setting_remember_filter_ckb);

    // Assistant
    mStartCloudSignUpBtn = (Button) activity.findViewById(R.id.setting_new_account_btn);

    // Server List
    serverList = (ServerList) activity.findViewById(R.id.setting_list_accounts);
  }

  @Override
  @Before
  public void setup() {
    controller = Robolectric.buildActivity(SettingActivity.class);
  }

  @Test
  public void verifyDefaultLayout_Online() {
    createWithDefaultIntent(true); // online

    // Login
    assertNotNull(mRememberMeCbx);
    assertNotNull(mAutoLoginCbx);
    assertThat("Remember Me should be disabled", mRememberMeCbx.isChecked(), equalTo(false));
    assertThat("Auto Login should be disabled", mAutoLoginCbx.isEnabled(), equalTo(false));
    // assertThat("Auto Login should be disabled",
    // mAutoLoginCbx.isClickable(), equalTo(false));
    assertThat("Auto Login should be disabled", mAutoLoginCbx.isChecked(), equalTo(false));

    // Languages
    assertNotNull(mEnCbx); // checkboxes for EN, FR, DE, ES, PT should exist
    assertThat(mEnCbx.isChecked(), equalTo(true)); // EN is selected by
                                                   // default
    assertNotNull(mFrCbx);
    assertThat(mFrCbx.isChecked(), equalTo(false));
    assertNotNull(mDeCbx);
    assertThat(mDeCbx.isChecked(), equalTo(false));
    assertNotNull(mEsCbx);
    assertThat(mEsCbx.isChecked(), equalTo(false));
    assertNotNull(mPtBrCbx);
    assertThat(mPtBrCbx.isChecked(), equalTo(false));
    assertNotNull(mElCbx);
    assertThat(mElCbx.isChecked(), equalTo(false));

    // Social
    assertNotNull(mRememberFilterCbx);

    // Server List : should have 1 server
    assertThat(srvSettings.getServerInfoList(activity).size(), equalTo(1));
    assertThat(serverList).hasChildCount(1);
    ServerItemLayout serverItem = (ServerItemLayout) serverList.getChildAt(0);
    assertThat(serverItem.serverName).containsText(TEST_SERVER_NAME);
    assertThat(serverItem.serverUrl).containsText(TEST_SERVER_URL);

    // Assistant
    assertNotNull(mStartCloudSignUpBtn);

    // App Info ** 3 cells should exist and contain default values set in
    // initSettings()
    TextView srvVersion = (TextView) activity.findViewById(R.id.setting_server_version_value_txt);
    TextView srvEdition = (TextView) activity.findViewById(R.id.setting_server_edition_value_txt);
    TextView appVersion = (TextView) activity.findViewById(R.id.setting_app_version_value_txt);
    assertThat(appVersion).containsText(APP_VERSION);
    assertThat(srvVersion).containsText(SRV_VERSION);
    assertThat(srvEdition).containsText(SRV_EDITION);

  }

  @Test
  public void verifyDefaultLayout_Offline() {
    createWithDefaultIntent(false); // offline

    // Login section is disabled when user is offline
    assertThat(mRememberMeCbx).isGone();
    assertThat(mAutoLoginCbx).isGone();

    // Languages
    assertNotNull(mEnCbx); // checkboxes for EN, FR, DE, ES should exist
    assertThat(mEnCbx.isChecked(), equalTo(true)); // EN is selected by
                                                   // default
    assertNotNull(mFrCbx);
    assertThat(mFrCbx.isChecked(), equalTo(false));
    assertNotNull(mDeCbx);
    assertThat(mDeCbx.isChecked(), equalTo(false));
    assertNotNull(mEsCbx);
    assertThat(mEsCbx.isChecked(), equalTo(false));
    assertNotNull(mPtBrCbx);
    assertThat(mPtBrCbx.isChecked(), equalTo(false));
    assertNotNull(mElCbx);
    assertThat(mElCbx.isChecked(), equalTo(false));

    // Social section is disabled when user is offline
    assertThat(mRememberFilterCbx).isGone();

    // Server List : should have 1 server
    assertThat(srvSettings.getServerInfoList(activity).size(), equalTo(1));
    assertThat(serverList).hasChildCount(1);
    ServerItemLayout serverItem = (ServerItemLayout) serverList.getChildAt(0);
    assertThat(serverItem.serverName).containsText(TEST_SERVER_NAME);
    assertThat(serverItem.serverUrl).containsText(TEST_SERVER_URL);

    // Assistant
    assertNotNull(mStartCloudSignUpBtn);

    // App Info ** 3 cells should exist and contain default values set in
    // initSettings()
    TextView srvVersion = (TextView) activity.findViewById(R.id.setting_server_version_value_txt);
    TextView srvEdition = (TextView) activity.findViewById(R.id.setting_server_edition_value_txt);
    TextView appVersion = (TextView) activity.findViewById(R.id.setting_app_version_value_txt);
    assertThat(appVersion).containsText(APP_VERSION);
    assertThat(srvVersion).containsText(SRV_VERSION);
    assertThat(srvEdition).containsText(SRV_EDITION);

  }

  @Test
  public void shouldTurnOnRememberMe() {

    createWithDefaultIntent(true);

    ShadowView.clickOn(mRememberMeCbx); // turning on remember me

    assertThat("RM should be checked", mRememberMeCbx.isChecked(), equalTo(true));
  }

  @Test
  public void shouldEnableAutoLoginWhenTurningOnRememberMe() {

    createWithDefaultIntent(true);

    ShadowView.clickOn(mRememberMeCbx); // turning on remember me

    assertThat("AL checkbox should be enabled", mAutoLoginCbx.isEnabled(), equalTo(true));
    assertThat("AL should be unchecked", mAutoLoginCbx.isChecked(), equalTo(false));

  }

  @Test
  public void shouldTurnOnAutoLogin() {

    createWithDefaultIntent(true);

    ShadowView.clickOn(mRememberMeCbx); // turning on remember me
    ShadowView.clickOn(mAutoLoginCbx); // turning on auto login

    assertThat("AL should be checked", mAutoLoginCbx.isChecked(), equalTo(true));
  }

  @Test
  public void shouldTurnOffAndDisableAutoLoginWhenTurningOffRememberMe() {

    createWithDefaultIntent(true);

    ShadowView.clickOn(mRememberMeCbx); // turning on remember me
    ShadowView.clickOn(mAutoLoginCbx); // turning on auto login

    assertThat(mAutoLoginCbx.isEnabled(), equalTo(true));
    assertThat(mAutoLoginCbx.isChecked(), equalTo(true));

    ShadowView.clickOn(mRememberMeCbx); // turning off remember me

    assertThat("RM should be unchecked", mRememberMeCbx.isChecked(), equalTo(false));
    assertThat("AL should be disabled", mAutoLoginCbx.isEnabled(), equalTo(false));
    assertThat("AL should be unchecked", mAutoLoginCbx.isChecked(), equalTo(false));

  }

  @Test
  public void shouldStartExoCloudSignupAssistant() {

    createWithDefaultIntent(true);

    ShadowView.clickOn(mStartCloudSignUpBtn);

    ShadowActivity sActivity = Shadows.shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = Shadows.shadowOf(welcomeIntent);

    assertThat(sIntent.getComponent().getClassName(), equalTo(WelcomeActivity.class.getName()));

  }

  @Test
  public void shouldChangeLanguage() {

    createWithDefaultIntent(true);

    SharedPreferences prefs = activity.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);

    ShadowView.clickOn(mDeCbx); // turn on German
    // English is off
    assertThat("English should be OFF", mEnCbx.isChecked(), equalTo(false));
    assertThat("German should be ON", mDeCbx.isChecked(), equalTo(true));
    assertThat(prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, ""), equalTo(ExoConstants.GERMAN_LOCALIZATION));

    ShadowView.clickOn(mFrCbx); // turn on French
    // German is off
    assertThat("German should be OFF", mDeCbx.isChecked(), equalTo(false));
    assertThat("French should be ON", mFrCbx.isChecked(), equalTo(true));
    assertThat(prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, ""), equalTo(ExoConstants.FRENCH_LOCALIZATION));

    ShadowView.clickOn(mEsCbx); // turn on Spanish
    // French is off
    assertThat("French should be OFF", mFrCbx.isChecked(), equalTo(false));
    assertThat("Spanish should be ON", mEsCbx.isChecked(), equalTo(true));
    assertThat(prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, ""), equalTo(ExoConstants.SPANISH_LOCALIZATION));

    ShadowView.clickOn(mPtBrCbx); // turn on Portuguese
    // Spanish is off
    assertThat("Spanish should be OFF", mEsCbx.isChecked(), equalTo(false));
    assertThat("Portuguese should be ON", mPtBrCbx.isChecked(), equalTo(true));
    assertThat(prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, ""), equalTo(ExoConstants.BRAZIL_LOCALIZATION));

    ShadowView.clickOn(mElCbx); // turn on Greek
    // Portuguese is off
    assertThat("Portuguese should be OFF", mPtBrCbx.isChecked(), equalTo(false));
    assertThat("Greek should be ON", mElCbx.isChecked(), equalTo(true));
    assertThat(prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, ""), equalTo(ExoConstants.GREEK_LOCALIZATION));

    ShadowView.clickOn(mEnCbx); // turn on English
    // Greek is off
    assertThat("Greek should be OFF", mElCbx.isChecked(), equalTo(false));
    assertThat("English should be ON", mEnCbx.isChecked(), equalTo(true));
    assertThat(prefs.getString(ExoConstants.EXO_PRF_LOCALIZE, ""), equalTo(ExoConstants.ENGLISH_LOCALIZATION));
  }

  @Test
  public void shouldEnableAccountInServerList() {
    createWithDefaultIntent(true);

    assertThat(serverList).hasChildCount(1);

    // Simulate tap on the account item
    ServerItemLayout item = (ServerItemLayout) serverList.getChildAt(0);
    // Click on item.layout because the OnClickListener is set on the layout
    ShadowView.clickOn(item.layout);

    ShadowActivity sActivity = Shadows.shadowOf(activity);
    Intent editAccount = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = Shadows.shadowOf(editAccount);

    // Should start the Server Edition activity
    assertThat("Should start the server edition activity",
               sIntent.getComponent().getClassName(),
               equalTo(ServerEditionActivity.class.getName()));
  }

}
