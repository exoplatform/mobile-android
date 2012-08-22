package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import org.exoplatform.controller.setting.SettingController;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.WarningDialog;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SettingActivity extends MyActionBar implements OnClickListener {

  private static final String   SERVER_SETTING_HELPER = "SERVER_SETTING_HELPER";

  private static final String   ACCOUNT_SETTING       = "account_setting";

  private View                  vEngLish, vFrench;

  private TextView              txtvEnglish, txtvFrench;

  private ImageView             imgViewECheckMark, imgViewFCheckMark;

  private TextView              txtvLanguage;                                   // Language

  private TextView              txtvServer;                                     // Server
                                                                                 // label

  /*
   * Social settings
   */

  private TextView              socialTitleView, socialContentView;

  private RelativeLayout        socialLayout;

  private ImageView             socialCheckedView;

  /*
   * Document settings
   */
  private TextView              documentTitleView, documentContentView;

  private RelativeLayout        documentLayout;

  private ImageView             documentCheckedView;

  /*
   * Server list setting
   */

  private LinearLayout          listServerWrap;

  private Button                modifyServerBtn;

  private TextView              settingAppInfoTitle;

  private TextView              applicationInfoText;

  private TextView              serverInfoText;

  private TextView              serverEditionText;

  private SettingController     setttingController;

  private String                errorMessage;

  private String                titleString;

  private String                okString;

  public static SettingActivity settingActivity;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.exosetting);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    settingActivity = this;
    if (savedInstanceState != null) {
      ServerSettingHelper helper = savedInstanceState.getParcelable(SERVER_SETTING_HELPER);
      ServerSettingHelper.getInstance().setInstance(helper);
      AccountSetting accountSetting = savedInstanceState.getParcelable(ACCOUNT_SETTING);
      AccountSetting.getInstance().setInstance(accountSetting);
    }
    init();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(SERVER_SETTING_HELPER, ServerSettingHelper.getInstance());
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
  }

  private void init() {
    txtvLanguage = (TextView) findViewById(R.id.TextView_Language);
    txtvServer = (TextView) findViewById(R.id.TextView_Server_List);

    vEngLish = (View) findViewById(R.id.View_English);
    vEngLish.setOnClickListener(this);
    txtvEnglish = (TextView) findViewById(R.id.TextView_English);
    imgViewECheckMark = (ImageView) findViewById(R.id.ImageView_CheckMark_EN);

    vFrench = (View) findViewById(R.id.View_French);
    vFrench.setOnClickListener(this);
    txtvFrench = (TextView) findViewById(R.id.TextView_French);
    imgViewFCheckMark = (ImageView) findViewById(R.id.ImageView_CheckMark_FR);

    socialTitleView = (TextView) findViewById(R.id.social_store_setting_title);
    socialContentView = (TextView) findViewById(R.id.social_store_setting_content);
    socialCheckedView = (ImageView) findViewById(R.id.social_store_setting_checked);
    socialLayout = (RelativeLayout) findViewById(R.id.social_store_setting_layout);
    socialLayout.setOnClickListener(this);

    documentTitleView = (TextView) findViewById(R.id.document_setting_title);
    documentContentView = (TextView) findViewById(R.id.document_store_setting_content);
    documentCheckedView = (ImageView) findViewById(R.id.document_store_setting_checked);
    documentLayout = (RelativeLayout) findViewById(R.id.document_store_setting_layout);
    documentLayout.setOnClickListener(this);

    listServerWrap = (LinearLayout) findViewById(R.id.listview_server_wrap);
    modifyServerBtn = (Button) findViewById(R.id.modify_server_btn);
    modifyServerBtn.setOnClickListener(this);

    setttingController = new SettingController(this, imgViewECheckMark, imgViewFCheckMark);
    setttingController.initLocation();
    setttingController.initSocialFilter(socialCheckedView);
    setttingController.initDocumentHiddenFile(documentCheckedView);

    settingAppInfoTitle = (TextView) findViewById(R.id.setting_application_info_title);
    serverInfoText = (TextView) findViewById(R.id.setting_server_info_title_text);
    String serverVersion = ServerSettingHelper.getInstance().getServerVersion();
    TextView severValueText = (TextView) findViewById(R.id.setting_server_info_value_text);
    severValueText.setText(serverVersion);

    serverEditionText = (TextView) findViewById(R.id.setting_server_edition_info_title_text);
    String serverEdition = ServerSettingHelper.getInstance().getServerEdition();
    TextView severEditionValueText = (TextView) findViewById(R.id.setting_server_edition_info_value_text);
    severEditionValueText.setText(serverEdition);

    applicationInfoText = (TextView) findViewById(R.id.setting_app_info_title_text);
    TextView appValueText = (TextView) findViewById(R.id.setting_app_info_value_text);
    String appVersion = ServerSettingHelper.getInstance().getApplicationVersion();
    appValueText.setText(appVersion);
    changeLanguage();

    setttingController.setServerList(listServerWrap);
  }

  private void updateLocation(String localize) {
    if (setttingController.updateLocallize(localize) == true) {
      changeLanguage();
      setttingController.setServerList(listServerWrap);
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
    String documentContent = resource.getString(R.string.DocumentShowHiddenFile);
    String applicationInfos = resource.getString(R.string.ApplicationInformation);
    String serverVersion = resource.getString(R.string.ServerVersion);
    String serverEdition = resource.getString(R.string.ServerEdition);
    String appVersion = resource.getString(R.string.ApplicationVersion);
    String settings = resource.getString(R.string.Settings);
    setTitle(settings);
    String modifyListText = resource.getString(R.string.ModifyServerList);
    modifyServerBtn.setText(modifyListText);
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

  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    finish();
    return true;
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  // @Override
  public void onClick(View view) {
    if (view.equals(vEngLish)) {
      setttingController.setEnglishLocation();
      updateLocation(ExoConstants.ENGLISH_LOCALIZATION);
    }
    if (view.equals(vFrench)) {
      setttingController.setFrenchLocation();
      updateLocation(ExoConstants.FRENCH_LOCALIZATION);
    }
    if (view.equals(socialLayout)) {
      setttingController.setSocialFilter(socialCheckedView);
    }

    if (view.equals(documentLayout)) {
      setttingController.setDocumentHiddenFile(documentCheckedView);
    }
    if (view.equals(modifyServerBtn)) {
      if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
        WarningDialog dialog = new WarningDialog(SettingActivity.this,
                                                 titleString,
                                                 errorMessage,
                                                 okString);
        dialog.show();
      } else {
        Intent next = new Intent(SettingActivity.this, SettingServerListEditionActivity.class);
        startActivity(next);
        finish();
      }
    }

  }

}
