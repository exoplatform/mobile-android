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
package org.exoplatform.ui.setting;

import greendroid.widget.ActionBarItem;

import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.WelcomeActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ServerConfigurationUtils;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Represents the setting screen<br/>
 *
 * Requires setting_type
 */
public class SettingActivity extends MyActionBar implements OnClickListener,
  CheckBox.ViewListener {


  private AccountSetting        mSetting;

  private ServerSettingHelper   mServerSetting = ServerSettingHelper.getInstance();

  private SharedPreferences     mSharedPerf;

  private Resources             mResources;

  /**=== Components ===**/
  private CheckBox              mRememberMeCbx;

  private CheckBox              mAutoLoginCbx;

  private CheckBoxWithImage     mEnCbx;

  private CheckBoxWithImage     mFrCbx;

  private CheckBoxWithImage     mDeCbx;

  private CheckBoxWithImage     mEsCbx;

  private CheckBox              mRememberFilterCbx;

  private CheckBox              mPrivateDriveCbx;

  private Button                mNewAccountBtn;

  /* list of accounts */
  private ServerList            mListAccounts;

  /**=== State ===**/
  private int                   mSettingType;

  /** launch setting without logging in */
  public static final int       GLOBAL_TYPE           = 0;

  /** launch setting while logging in */
  public static final int       PERSONAL_TYPE         = 1;

  @SuppressWarnings("unused")
  private static final String   TAG = "eXo____SettingActivity____";


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mSharedPerf  = getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    mSettingType = getIntent().getIntExtra(ExoConstants.SETTING_TYPE, GLOBAL_TYPE);
    mSetting     = AccountSetting.getInstance();
    mResources   = getResources();

    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.settings);
    super.getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    super.getActionBar().setTitle(mResources.getString(R.string.Settings));

    initSubViews();
    initStates();
  }

  /**
   * On returning from Server Edition Activity
   *
   * @param intent
   */
  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    int operation = intent.getIntExtra(ServerEditionActivity.SETTING_OPERATION, -1);
    int serverIdx = intent.getIntExtra(ServerEditionActivity.SERVER_IDX, -1);
    mListAccounts.updateServerList(operation, serverIdx);
  }


  private void initSubViews() {
    /** Login */
    mRememberMeCbx  = (CheckBox) findViewById(R.id.setting_remember_me_ckb);
    mAutoLoginCbx   = (CheckBox) findViewById(R.id.setting_autologin_ckb);

    mRememberMeCbx.setText(mResources.getString(R.string.RememberMe))
                  .setChecked(mSetting.isRememberMeEnabled(), false)
                  .setViewListener(this);
    mAutoLoginCbx.setText(mResources.getString(R.string.Autologin))
                 .setChecked(mSetting.isAutoLoginEnabled(), false)
                 .setViewListener(this);

    /** Language */
    mEnCbx = (CheckBoxWithImage) findViewById(R.id.setting_en_ckb);
    mEnCbx.setText(mResources.getString(R.string.English))
          .setViewListener(this);

    mFrCbx = (CheckBoxWithImage) findViewById(R.id.setting_fr_ckb);
    mFrCbx.setText(mResources.getString(R.string.French));
    mFrCbx.setHeadImage(R.drawable.settingslanguagefrench)
          .setViewListener(this);

    mDeCbx = (CheckBoxWithImage) findViewById(R.id.setting_de_ckb);
    mDeCbx.setText(mResources.getString(R.string.German));
    mDeCbx.setHeadImage(R.drawable.settingslanguagegerman)
          .setViewListener(this);

    mEsCbx = (CheckBoxWithImage) findViewById(R.id.setting_es_ckb);
    mEsCbx.setText(mResources.getString(R.string.Spanish));
    mEsCbx.setHeadImage(R.drawable.settingslanguagespain)
        .setViewListener(this);

    String currentLanguage = getCurrentLanguage();
    if (currentLanguage.equalsIgnoreCase(ExoConstants.ENGLISH_LOCALIZATION))      mEnCbx.setChecked(true, false);
    else if (currentLanguage.equalsIgnoreCase(ExoConstants.FRENCH_LOCALIZATION))  mFrCbx.setChecked(true, false);
    else if (currentLanguage.equalsIgnoreCase(ExoConstants.GERMAN_LOCALIZATION))  mDeCbx.setChecked(true, false);
    else if (currentLanguage.equalsIgnoreCase(ExoConstants.SPANISH_LOCALIZATION)) mEsCbx.setChecked(true, false);

    /** Social */
    mRememberFilterCbx   = (CheckBox) findViewById(R.id.setting_remember_filter_ckb);
    mRememberFilterCbx.setText(mResources.getString(R.string.SocialSettingContent))
                      .setChecked(mSharedPerf.getBoolean(mSetting.socialKey, false), false);
    /** Server */
    mNewAccountBtn = (Button) findViewById(R.id.setting_new_account_btn);
    mNewAccountBtn.setOnClickListener(this);

    mListAccounts = (ServerList) findViewById(R.id.setting_list_accounts);

    /** Documents */
    mPrivateDriveCbx = (CheckBox) findViewById(R.id.setting_private_drive_ckb);
    mPrivateDriveCbx.setText(mResources.getString(R.string.DocumentShowPrivateDrive))
                    .setChecked(mSharedPerf.getBoolean(mSetting.documentKey, true), false);

    /** App Info */
    ((TextView) findViewById(R.id.setting_server_version_value_txt)).setText(mServerSetting.getServerVersion());
    ((TextView) findViewById(R.id.setting_server_edition_value_txt)).setText(mServerSetting.getServerEdition());
    ((TextView) findViewById(R.id.setting_app_version_value_txt)).setText(mServerSetting.getApplicationVersion());
  }

  private void initStates() {
    if (mSettingType == GLOBAL_TYPE) {
      /* disable login section */
      mRememberMeCbx.enabled(false);
      mAutoLoginCbx.enabled(false);

      /* disable social and document section */
      mRememberFilterCbx.enabled(false);
      mPrivateDriveCbx.enabled(false);
    }

    if (SocialActivityUtil.getPlatformVersion() >= 4.0f || mSettingType == GLOBAL_TYPE) {
      /** as for PLF 4, private drive is removed */
      mPrivateDriveCbx.setVisibility(View.GONE);
      findViewById(R.id.setting_document_title).setVisibility(View.GONE);
    }

    /** update language */
    if (!getCurrentLanguage().equalsIgnoreCase(ExoConstants.ENGLISH_LOCALIZATION))
      onChangeLanguage(SettingUtils.getPrefsLanguage(this));
  }


  private String getCurrentLanguage() {
    return mSharedPerf.getString(ExoConstants.EXO_PRF_LOCALIZE, ExoConstants.ENGLISH_LOCALIZATION);
  }

  private void onChangeLanguage(String language) {
    SettingUtils.setLocale(this, language);

    super.getActionBar().setTitle(mResources.getString(R.string.Settings));

    ((TextView) findViewById(R.id.setting_login_title_txt)).setText(mResources.getString(R.string.LoginTitle));
    mRememberMeCbx.setText(mResources.getString(R.string.RememberMe));
    mAutoLoginCbx.setText(mResources.getString(R.string.Autologin));

    ((TextView) findViewById(R.id.setting_language_title)).setText(mResources.getString(R.string.Language));
    mEnCbx.setText(mResources.getString(R.string.English));
    mFrCbx.setText(mResources.getString(R.string.French));
    mDeCbx.setText(mResources.getString(R.string.German));
    mEsCbx.setText(mResources.getString(R.string.Spanish));

    mRememberFilterCbx.setText(mResources.getString(R.string.SocialSettingContent));

    ((TextView) findViewById(R.id.setting_social_title)).setText(mResources.getString(R.string.SocialSettingTitle));
    ((TextView) findViewById(R.id.setting_accounts_title)).setText(mResources.getString(R.string.Server));

    ((TextView) findViewById(R.id.setting_document_title)).setText(mResources.getString(R.string.DocumentSettingTitle));

    ((TextView) findViewById(R.id.setting_app_info_title)).setText(mResources.getString(R.string.ApplicationInformation));
    ((TextView) findViewById(R.id.setting_server_edition_txt)).setText(mResources.getString(R.string.ServerEdition));
    ((TextView) findViewById(R.id.setting_server_version_txt)).setText(mResources.getString(R.string.ServerVersion));
    ((TextView) findViewById(R.id.setting_app_version_txt)).setText(mResources.getString(R.string.ApplicationVersion));

    mNewAccountBtn.setText(mResources.getString(R.string.AddAServer));
  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    finish();
    return true;
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  @Override
  public void onClick(View view) {

	  // To add a new account, the user must use the assistant
    if (view.equals(mNewAccountBtn)) {
      Intent next = new Intent(this, WelcomeActivity.class);
      startActivity(next);
    }

  }

  @Override
  protected void onPause(){
    super.onPause();

    if (mSettingType == GLOBAL_TYPE) return ;
    SharedPreferences.Editor editor = mSharedPerf.edit();
    editor.putBoolean(mSetting.socialKey,   mRememberFilterCbx.isChecked());
    editor.putBoolean(mSetting.documentKey, mPrivateDriveCbx.isChecked());
    editor.commit();

    ServerConfigurationUtils.generateXmlFileWithServerList(this,
      ServerSettingHelper.getInstance().getServerInfoList(this), ExoConstants.EXO_SERVER_SETTING_FILE, "");
  }

  @Override
  public void onClickCheckBox(CheckBox checkBox, boolean isChecked) {

    /** Change language */
    if (checkBox.equals(mEnCbx)) {
      mFrCbx.setChecked(false, false);
      mDeCbx.setChecked(false, false);
      mEsCbx.setChecked(false, false);
      onChangeLanguage(ExoConstants.ENGLISH_LOCALIZATION);
    }
    else if (checkBox.equals(mFrCbx)) {
      mEnCbx.setChecked(false, false);
      mDeCbx.setChecked(false, false);
      mEsCbx.setChecked(false, false);
      onChangeLanguage(ExoConstants.FRENCH_LOCALIZATION);
    }
    else if (checkBox.equals(mDeCbx)) {
      mEnCbx.setChecked(false, false);
      mFrCbx.setChecked(false, false);
      mEsCbx.setChecked(false, false);
      onChangeLanguage(ExoConstants.GERMAN_LOCALIZATION);
    }
    else if (checkBox.equals(mEsCbx)) {
      mEnCbx.setChecked(false, false);
      mFrCbx.setChecked(false, false);
      mDeCbx.setChecked(false, false);
      onChangeLanguage(ExoConstants.SPANISH_LOCALIZATION);
    }
    /** Remember Me and Auto login */
    else if (checkBox.equals(mRememberMeCbx)) {
      if (!mRememberMeCbx.isChecked()) {
        mAutoLoginCbx.setChecked(false, false);
        mAutoLoginCbx.enabled(false);
      }
      else mAutoLoginCbx.enabled(true);

      mSetting.getCurrentServer().isRememberEnabled  = mRememberMeCbx.isChecked();
      mSetting.getCurrentServer().isAutoLoginEnabled = mAutoLoginCbx.isChecked();
    }
    else if (checkBox.equals(mAutoLoginCbx)) {
      mSetting.getCurrentServer().isAutoLoginEnabled = mAutoLoginCbx.isChecked();
    }

  }
}
