package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import org.exoplatform.controller.setting.SettingController;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.WarningDialog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SettingActivity extends MyActionBar implements OnClickListener {

  private View              vEngLish, vFrench;

  private TextView          txtvEnglish, txtvFrench;

  private ImageView         imgViewECheckMark, imgViewFCheckMark;

  private TextView          txtvLanguage;                        // Language

  private TextView          txtvServer;                          // Server
                                                                  // label

  private LinearLayout      listServerWrap;

  private Button            modifyServerBtn;

  private SettingController setttingController;

  private String            errorMessage;

  private String            titleString;

  private String            okString;
  
  public static SettingActivity settingActivity;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.exosetting);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    settingActivity = this;
    init();
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

    listServerWrap = (LinearLayout) findViewById(R.id.listview_server_wrap);
    modifyServerBtn = (Button) findViewById(R.id.modify_server_btn);
    modifyServerBtn.setOnClickListener(this);
    setttingController = new SettingController(this, imgViewECheckMark, imgViewFCheckMark);
    setttingController.initLocation();
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
    LocalizationHelper local = LocalizationHelper.getInstance();
    String strLanguageTittle = local.getString("Language");
    String strServerTittle = local.getString("Server");
    String strEnglish = local.getString("English");
    String strFrench = local.getString("French");
    String settings = local.getString("Settings");
    setTitle(settings);
    String modifyListText = local.getString("ModifyServerList");
    modifyServerBtn.setText(modifyListText);
    txtvEnglish.setText(strEnglish);
    txtvFrench.setText(strFrench);
    txtvServer.setText(strServerTittle);
    txtvLanguage.setText(strLanguageTittle);
    errorMessage = local.getString("DoNotHavePermision");
    okString = local.getString("OK");
    titleString = local.getString("Warning");

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
    if (view == vEngLish) {
      setttingController.setEnglishLocation();
      updateLocation(ExoConstants.ENGLISH_LOCALIZATION);
    }
    if (view == vFrench) {
      setttingController.setFrenchLocation();
      updateLocation(ExoConstants.FRENCH_LOCALIZATION);
    }
    if (view == modifyServerBtn) {
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
