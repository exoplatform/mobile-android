package org.exoplatform.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import greendroid.widget.ActionBarItem;

import org.exoplatform.R;
import org.exoplatform.controller.setting.SettingController;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Represents the setting screen<br/>
 *
 * Requires setting_type
 */
public class SettingActivity extends MyActionBar implements OnClickListener {

  /** launch setting without logging in */
  public static final int       GLOBAL_TYPE           = 0;

  /** launch setting while logging in */
  public static final int       PERSONAL_TYPE         = 1;

  private int                   mSettingType;

  private View                  vEngLish, vFrench;

  private TextView              txtvEnglish, txtvFrench;

  private ImageView             imgViewECheckMark, imgViewFCheckMark;

  private TextView              txtvLanguage;                                   // Language

  private TextView              txtvServer;                                     // Server

  /* Login settings */
  private RelativeLayout        mLoginLayout;

  private ImageView             mRememberMeImg;

  private ImageView             mAutoLoginImg;
                                                                                 // label
  private TextView              mRememberMeTxt;

  private TextView              mAutoLoginTxt;

  private TextView              mLoginTitleTxt;

  /*
   * Social settings
   */

  private TextView              socialTitleView, socialContentView;

  private RelativeLayout        socialLayout, socialTitleLayout;

  private ImageView             socialCheckedView;

  /*
   * Document settings
   */
  private TextView              documentTitleView, documentContentView;

  private RelativeLayout        documentLayout, documentTitleLayout;

  private ImageView             documentCheckedView;

  /*
   * Server list setting
   */

  private LinearLayout          listServerWrap;

  private Button                addServerBtn;

  private TextView              settingAppInfoTitle;

  private TextView              applicationInfoText;

  private TextView              serverInfoText;

  private TextView              serverEditionText;

  private SettingController     mSettingController;

  private String                errorMessage;

  private String                titleString;

  private String                okString;

  private Button                mStartCloudSignUpBtn;

  private TextView              mExoTitleTxt;

  private AccountSetting        mSetting;

  private ServerSettingHelper   mServerSetting = ServerSettingHelper.getInstance();

  private SharedPreferences     mSharedPerf;

  private static final String TAG = "eXoSettingActivity";

  // TODO: highly susceptible of memory leak
  public static SettingActivity settingActivity;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.exosetting);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    mSharedPerf  = getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    mSettingType = getIntent().getIntExtra(ExoConstants.SETTING_TYPE, GLOBAL_TYPE);
    mSetting = AccountSetting.getInstance();
    settingActivity = this;

    if (savedInstanceState != null) {
      ServerSettingHelper helper = savedInstanceState.
          getParcelable(ExoConstants.SERVER_SETTING_HELPER);
      ServerSettingHelper.getInstance().setInstance(helper);
      mSetting = savedInstanceState.getParcelable(ExoConstants.ACCOUNT_SETTING);
      if( mSetting==null) mSetting = AccountSetting.getInstance();
    }

    init();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(ExoConstants.SERVER_SETTING_HELPER, ServerSettingHelper.getInstance());
    outState.putParcelable(ExoConstants.ACCOUNT_SETTING, mSetting);
  }

  private void init() {
    /* Login */
    mLoginLayout   = (RelativeLayout) findViewById(R.id.setting_login_layout);
    mRememberMeImg = (ImageView) mLoginLayout.findViewById(R.id.setting_remember_me_check_img);
    mAutoLoginImg  = (ImageView) mLoginLayout.findViewById(R.id.setting_autologin_check_img);
    mRememberMeTxt = (TextView)  mLoginLayout.findViewById(R.id.setting_remember_me_txt);
    mAutoLoginTxt  = (TextView)  mLoginLayout.findViewById(R.id.setting_autologin_txt);
    mLoginTitleTxt = (TextView)  findViewById(R.id.setting_login_title_txt);

    txtvLanguage = (TextView) findViewById(R.id.TextView_Language);
    txtvServer = (TextView) findViewById(R.id.TextView_Server_List);

    vEngLish = findViewById(R.id.View_English);
    vEngLish.setOnClickListener(this);
    txtvEnglish = (TextView) findViewById(R.id.TextView_English);
    imgViewECheckMark = (ImageView) findViewById(R.id.ImageView_CheckMark_EN);

    vFrench  = findViewById(R.id.View_French);
    vFrench.setOnClickListener(this);
    txtvFrench = (TextView) findViewById(R.id.TextView_French);
    imgViewFCheckMark = (ImageView) findViewById(R.id.ImageView_CheckMark_FR);

    socialTitleView = (TextView) findViewById(R.id.social_store_setting_title);
    socialContentView = (TextView) findViewById(R.id.social_store_setting_content);
    socialCheckedView = (ImageView) findViewById(R.id.social_store_setting_checked);
    socialTitleLayout = (RelativeLayout) findViewById(R.id.social_store_setting_title_layout);
    socialLayout = (RelativeLayout) findViewById(R.id.social_store_setting_layout);
    socialLayout.setOnClickListener(this);
    documentTitleView = (TextView) findViewById(R.id.document_setting_title);
    documentContentView = (TextView) findViewById(R.id.document_store_setting_content);
    documentCheckedView = (ImageView) findViewById(R.id.document_store_setting_checked);
    documentTitleLayout = (RelativeLayout) findViewById(R.id.document_setting_title_header_layout);
    documentLayout = (RelativeLayout) findViewById(R.id.document_store_setting_layout);
    documentLayout.setOnClickListener(this);
    addServerBtn = (Button) findViewById(R.id.modify_server_btn);
    addServerBtn.setOnClickListener(this);

    mExoTitleTxt = (TextView) findViewById(R.id.setting_exo_title_txt);

    mStartCloudSignUpBtn = (Button) findViewById(R.id.setting_start_cloud_signup_btn);
    mStartCloudSignUpBtn.setOnClickListener(this);

    /* replacing old list server with new one */
    listServerWrap = (LinearLayout) findViewById(R.id.listview_server_wrap);
    mSettingController = new SettingController(this, listServerWrap);

    mSettingController.initLocation(imgViewECheckMark, imgViewFCheckMark);
    if (mSettingType == GLOBAL_TYPE) {
      socialTitleLayout.setVisibility(View.GONE);
      socialLayout.setVisibility(View.GONE);
      documentTitleLayout.setVisibility(View.GONE);
      documentLayout.setVisibility(View.GONE);
      hideLoginSetting();
    } else {
      mSettingController.initSocialFilter(socialCheckedView);
      mSettingController.initDocumentHiddenFile(documentCheckedView);
      mSettingController.initLoginSetting(mRememberMeImg, mAutoLoginImg);
    }
    if(SocialActivityUtil.getPlatformVersion() >= 4.0f) {
      documentTitleLayout.setVisibility(View.GONE);
      documentLayout.setVisibility(View.GONE);
    }

    settingAppInfoTitle = (TextView) findViewById(R.id.setting_application_info_title);
    serverInfoText = (TextView) findViewById(R.id.setting_server_info_title_text);
    String serverVersion = mServerSetting.getServerVersion();
    TextView severValueText = (TextView) findViewById(R.id.setting_server_info_value_text);
    severValueText.setText(serverVersion);

    serverEditionText = (TextView) findViewById(R.id.setting_server_edition_info_title_text);
    String serverEdition = mServerSetting.getServerEdition();
    TextView severEditionValueText = (TextView) findViewById(R.id.setting_server_edition_info_value_text);
    severEditionValueText.setText(serverEdition);

    applicationInfoText = (TextView) findViewById(R.id.setting_app_info_title_text);
    TextView appValueText = (TextView) findViewById(R.id.setting_app_info_value_text);
    appValueText.setText(mServerSetting.getApplicationVersion());

    changeLanguage();
  }

  private void hideLoginSetting() {
    mLoginLayout.setVisibility(View.GONE);
    findViewById(R.id.setting_login_title_txt).setVisibility(View.GONE);
    findViewById(R.id.setting_login_separator_img).setVisibility(View.GONE);
    findViewById(R.id.setting_login_separator_bottom_img).setVisibility(View.GONE);
  }

  /**
   * Update app language
   * @param localize
   */
  private void updateLocation(String localize) {
    if (mSettingController.updateLocallize(localize)) {
      changeLanguage();
    }
  }

  private void changeLanguage() {
    Resources resource = getResources();
    String strLanguageTittle = resource.getString(R.string.Language);
    String strServerTittle = resource.getString(R.string.Server);
    String strEnglish = resource.getString(R.string.English);
    String strFrench = resource.getString(R.string.French);
    String socialTitle = resource.getString(R.string.SocialSettingTitle);
    String socialContent = resource.getString(R.string.SocialSettingContent);
    String documentTitle = resource.getString(R.string.DocumentSettingTitle);
    String documentContent = resource.getString(R.string.DocumentShowPrivateDrive);
    String applicationInfos = resource.getString(R.string.ApplicationInformation);
    String serverVersion = resource.getString(R.string.ServerVersion);
    String serverEdition = resource.getString(R.string.ServerEdition);
    String appVersion = resource.getString(R.string.ApplicationVersion);
    String settings = resource.getString(R.string.Settings);
    setTitle(settings);
    String addNewServer = resource.getString(R.string.AddAServer);
    String eXo = resource.getString(R.string.EXO);

    addServerBtn.setText(addNewServer);
    txtvEnglish.setText(strEnglish);
    txtvFrench.setText(strFrench);
    txtvServer.setText(strServerTittle);
    txtvLanguage.setText(strLanguageTittle);
    socialTitleView.setText(socialTitle);
    socialContentView.setText(socialContent);
    documentTitleView.setText(documentTitle);
    documentContentView.setText(documentContent);
    settingAppInfoTitle.setText(applicationInfos);
    applicationInfoText.setText(appVersion);
    serverInfoText.setText(serverVersion);
    serverEditionText.setText(serverEdition);
    errorMessage = resource.getString(R.string.DoNotHavePermision);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);

    mExoTitleTxt.setText(eXo);

    /* Login */
    String rememberMe   = resource.getString(R.string.RememberMe);
    String autoLogin    = resource.getString(R.string.Autologin);
    String loginTitle   = resource.getString(R.string.LoginTitle);

    mRememberMeTxt.setText(rememberMe);
    mAutoLoginTxt.setText(autoLogin);
    mLoginTitleTxt.setText(loginTitle);
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
    if (view.equals(vEngLish)) {
      mSettingController.setEnglishLocation(imgViewECheckMark, imgViewFCheckMark);
      updateLocation(ExoConstants.ENGLISH_LOCALIZATION);
    }
    if (view.equals(vFrench)) {
      mSettingController.setFrenchLocation(imgViewECheckMark, imgViewFCheckMark);
      updateLocation(ExoConstants.FRENCH_LOCALIZATION);
    }
    if (view.equals(socialLayout)) {
      mSettingController.setSocialFilter(socialCheckedView);
    }

    if (view.equals(documentLayout)) {
      mSettingController.setDocumentShowPrivateDrive(documentCheckedView);
    }
    if (view.equals(addServerBtn)) {
      Intent next = new Intent(this, ServerEditionActivity.class);
      next.putExtra(ExoConstants.SETTING_ADDING_SERVER, true);
      next.putExtra(ExoConstants.SETTING_TYPE, mSettingType);
      SettingController.useInstance(mSettingController);
      startActivity(next);
    }
    /**
     * Users start cloud sign up
     */
    if (view.equals(mStartCloudSignUpBtn)) {
      mSetting.setCurrentServer(null);
      mSetting.setDomainIndex("-1");
      SettingUtils.modifySharedPerf(this);

      /* do not allow to come back to setting */
      Intent next = new Intent(this, WelcomeActivity.class);
      next.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(next);
      finish();
    }

  }

  @Override
  protected void onPause(){
    Log.i(TAG, "onPause");
    super.onPause();

    if (mSettingType == GLOBAL_TYPE) return ;
    SharedPreferences.Editor editor = mSharedPerf.edit();
    if (mSettingController.mIsSocialFilterEnabled)
      editor.putInt(mSetting.socialKeyIndex, 0);
    else
      editor.putBoolean(mSetting.socialKey, mSettingController.mIsSocialFilterEnabled);
    editor.putBoolean(mSetting.documentKey, mSettingController.mIsShowHidden);
    editor.commit();

    ServerConfigurationUtils.generateXmlFileWithServerList(this,
      ServerSettingHelper.getInstance().getServerInfoList(), ExoConstants.EXO_SERVER_SETTING_FILE, "");
  }
}
