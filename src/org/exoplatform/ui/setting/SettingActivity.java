package org.exoplatform.ui.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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

  private CheckBox              mRememberFilterCbx;

  private CheckBox              mPrivateDriveCbx;

  private Button                mAddServerBtn;

  private Button                mStartCloudSignUpBtn;

  /* list of servers */
  private ServerList            mListServer;

  /**=== State ===**/
  private int                   mSettingType;

  /** launch setting without logging in */
  public static final int       GLOBAL_TYPE           = 0;

  /** launch setting while logging in */
  public static final int       PERSONAL_TYPE         = 1;

  private static final String   TAG = "eXoSettingActivity";


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
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    getActionBar().setTitle(mResources.getString(R.string.Settings));

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
    mListServer.updateServerList(operation, serverIdx);
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
          .setChecked(usesEnglish(), false)
          .setViewListener(this);

    mFrCbx = (CheckBoxWithImage) findViewById(R.id.setting_fr_ckb);
    mFrCbx.setText(mResources.getString(R.string.French));
    mFrCbx.setHeadImage(R.drawable.settingslanguagefrench)
          .setChecked(!usesEnglish(), false)
          .setViewListener(this);

    /** Social */
    mRememberFilterCbx   = (CheckBox) findViewById(R.id.setting_remember_filter_ckb);
    mRememberFilterCbx.setText(mResources.getString(R.string.SocialSettingContent))
                      .setChecked(mSharedPerf.getBoolean(mSetting.socialKey, false), false);
    /** Server */
    mAddServerBtn = (Button) findViewById(R.id.setting_modify_server_btn);
    mAddServerBtn.setOnClickListener(this);

    mListServer = (ServerList) findViewById(R.id.setting_list_server);

    /** Documents */
    mPrivateDriveCbx = (CheckBox) findViewById(R.id.setting_private_drive_ckb);
    mPrivateDriveCbx.setText(mResources.getString(R.string.DocumentShowPrivateDrive))
                    .setChecked(mSharedPerf.getBoolean(mSetting.documentKey, true), false);

    /** eXo */
    mStartCloudSignUpBtn = (Button) findViewById(R.id.setting_start_cloud_signup_btn);
    mStartCloudSignUpBtn.setOnClickListener(this);

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
    if (!usesEnglish()) onChangeLanguage(SettingUtils.getPrefsLanguage(this));
  }

  private boolean usesEnglish() {
    return mSharedPerf.getString(ExoConstants.EXO_PRF_LOCALIZE,
        ExoConstants.ENGLISH_LOCALIZATION).equalsIgnoreCase(ExoConstants.ENGLISH_LOCALIZATION);
  }

  private void onChangeLanguage(String language) {
    SettingUtils.setLocale(this, language);

    ((TextView) findViewById(R.id.setting_login_title_txt)).setText(mResources.getString(R.string.LoginTitle));
    mRememberMeCbx.setText(mResources.getString(R.string.RememberMe));
    mAutoLoginCbx.setText(mResources.getString(R.string.Autologin));

    ((TextView) findViewById(R.id.setting_language_title)).setText(mResources.getString(R.string.Language));
    mEnCbx.setText(mResources.getString(R.string.English));
    mFrCbx.setText(mResources.getString(R.string.French));

    ((TextView) findViewById(R.id.setting_social_title)).setText(mResources.getString(R.string.SocialSettingTitle));
    ((TextView) findViewById(R.id.setting_server_title)).setText(mResources.getString(R.string.Server));

    ((TextView) findViewById(R.id.setting_document_title)).setText(mResources.getString(R.string.DocumentSettingTitle));

    ((TextView) findViewById(R.id.setting_app_info_title)).setText(mResources.getString(R.string.ApplicationInformation));
    ((TextView) findViewById(R.id.setting_server_edition_txt)).setText(mResources.getString(R.string.ServerEdition));
    ((TextView) findViewById(R.id.setting_server_version_txt)).setText(mResources.getString(R.string.ServerVersion));
    ((TextView) findViewById(R.id.setting_app_version_txt)).setText(mResources.getString(R.string.ApplicationVersion));

    mAddServerBtn.setText(mResources.getString(R.string.AddAServer));
    mStartCloudSignUpBtn.setText(mResources.getString(R.string.StartCloudSignUpAssistant));
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

    if (view.equals(mAddServerBtn)) {
      Intent next = new Intent(this, ServerEditionActivity.class);
      next.putExtra(ExoConstants.SETTING_ADDING_SERVER, true);
      next.putExtra(ExoConstants.SETTING_TYPE, mSettingType);
      startActivity(next);
    }

    /** Users start cloud sign up */
    if (view.equals(mStartCloudSignUpBtn)) {
      //mSetting.setCurrentServer(null);
      //mSetting.setDomainIndex("-1");
      //SettingUtils.modifySharedPerf(this);

      /* do not allow to come back to setting */
      Intent next = new Intent(this, WelcomeActivity.class);
      //next.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
      ServerSettingHelper.getInstance().getServerInfoList(), ExoConstants.EXO_SERVER_SETTING_FILE, "");
  }

  @Override
  public void onClickCheckBox(CheckBox checkBox, boolean isChecked) {

    /** Change language */
    if (checkBox.equals(mEnCbx)) {
      mFrCbx.setChecked(false, false);
      onChangeLanguage(ExoConstants.ENGLISH_LOCALIZATION);
    }
    else if (checkBox.equals(mFrCbx)) {
      mEnCbx.setChecked(false, false);
      onChangeLanguage(ExoConstants.FRENCH_LOCALIZATION);
    }
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
