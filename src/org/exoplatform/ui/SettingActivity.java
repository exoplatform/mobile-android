package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import org.exoplatform.controller.setting.SettingController;
import org.exoplatform.setting.ExoModifyServerList;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.widget.MyActionBar;

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
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;

public class SettingActivity extends MyActionBar implements OnClickListener {
  private Button            btnUserGuide;

  private View              vEngLish, vFrench;

  private TextView          txtvEnglish, txtvFrench;

  private ImageView         imgViewECheckMark, imgViewFCheckMark;

  private TextView          txtvLanguage;                        // Language

  private TextView          txtvServer;                          // Server
                                                                  // label

  private LinearLayout      listServerWrap;

  private Button            modifyServerBtn;

  private SettingController setttingController;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.exosetting);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    init();
  }

  private void init() {
    txtvLanguage = (TextView) findViewById(R.id.TextView_Language);
    txtvServer = (TextView) findViewById(R.id.TextView_Server_List);

    btnUserGuide = (Button) findViewById(R.id.Button_User_Guide);

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
    String strUserGuideButton = local.getString("UserGuide");
    String settings = local.getString("Settings");
    setTitle(settings);
    String modifyListText = local.getString("ModifyServerList");
    modifyServerBtn.setText(modifyListText);
    txtvEnglish.setText(strEnglish);
    txtvFrench.setText(strFrench);
    btnUserGuide.setText(strUserGuideButton);
    txtvServer.setText(strServerTittle);
    txtvLanguage.setText(strLanguageTittle);

  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    finish();
    return true;
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }

  @Override
  public void onClick(View view) {
    if (view == vEngLish) {
      setttingController.setEnglishLocation();
      updateLocation("LocalizeEN.properties");
    }
    if (view == vFrench) {
      setttingController.setFrenchLocation();
      updateLocation("LocalizeFR.properties");
    }
    if (view == modifyServerBtn) {
      if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
        String msg = "You dont't have permission for this because SDCard is not available!";
        Toast.makeText(this, msg, Toast.LENGTH_LONG);
      } else {
        Intent next = new Intent(SettingActivity.this, SettingServerListEditionActivity.class);
        startActivity(next);
        finish();
      }
    }

  }

}
