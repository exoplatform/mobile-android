package org.exoplatform.setting;

import greendroid.widget.ActionBarItem;

import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.exoplatform.proxy.ServerObj;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.ServerItemLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;

public class ExoSetting extends MyActionBar {

  public static ExoSetting eXoSettingInstance;

  View                     vEngLish, vFrench;

  TextView                 txtvEnglish, txtvFrench;

  private TextView         tvModifyTheList;

  ImageView                imgViewECheckMark, imgViewFCheckMark;

  TextView                 txtvLanguage;                        // Language
                                                                 // label

  TextView                 txtvServer;                          // Server label

  private ListView         listViewServer;

  private LinearLayout     listServerWrap;

  private Button           modifyServerBtn;

  private int              style;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.exosetting);
    eXoSettingInstance = this;
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    txtvLanguage = (TextView) findViewById(R.id.TextView_Language);
    txtvServer = (TextView) findViewById(R.id.TextView_Server_List);

    vEngLish = (View) findViewById(R.id.View_English);
    txtvEnglish = (TextView) findViewById(R.id.TextView_English);
    imgViewECheckMark = (ImageView) findViewById(R.id.ImageView_CheckMark_EN);

    vFrench = (View) findViewById(R.id.View_French);
    txtvFrench = (TextView) findViewById(R.id.TextView_French);
    imgViewFCheckMark = (ImageView) findViewById(R.id.ImageView_CheckMark_FR);

    // listViewServer = (ListView) findViewById(R.id.ListView_Servers);
    // listViewServer.setDivider(null);
    // listViewServer.setDividerHeight(0);

    listServerWrap = (LinearLayout) findViewById(R.id.listview_server_wrap);

    modifyServerBtn = (Button) findViewById(R.id.modify_server_btn);
    modifyServerBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
          String msg = "You dont't have permission for this because SDCard is not available!";
          Toast.makeText(eXoSettingInstance, msg, Toast.LENGTH_LONG);
        } else {
          Intent next = new Intent(ExoSetting.this, ExoModifyServerList.class);
          startActivity(next);
          finish();
        }
      }
    });

    vEngLish.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        imgViewECheckMark.setVisibility(View.VISIBLE);
        imgViewFCheckMark.setVisibility(View.INVISIBLE);

        updateLocallize("LocalizeEN.properties");
      }
    });

    vFrench.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        imgViewECheckMark.setVisibility(View.INVISIBLE);
        imgViewFCheckMark.setVisibility(View.VISIBLE);

        updateLocallize("LocalizeFR.properties");
      }
    });

    String locallize = LocalizationHelper.getInstance().getLocation();
    if (locallize.equalsIgnoreCase("LocalizeFR.properties")) {
      imgViewFCheckMark.setVisibility(View.VISIBLE);
      imgViewECheckMark.setVisibility(View.INVISIBLE);
    } else {
      imgViewECheckMark.setVisibility(View.VISIBLE);
      imgViewFCheckMark.setVisibility(View.INVISIBLE);
    }

    setServerList(ServerSettingHelper.getInstance().getServerInfoList());

    changeLanguage();

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

  public void setServerList(List<ServerObj> serverList) {
    listServerWrap.removeAllViews();
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    for (int i = 0; i < serverList.size(); i++) {
      ServerObj serverObj = serverList.get(i);
      ServerItemLayout serverItem = new ServerItemLayout(this);
      serverItem.serverName.setText(serverObj._strServerName);
      serverItem.serverUrl.setText(serverObj._strServerUrl);
      if (AccountSetting.getInstance().getDomainIndex() == i)
        serverItem.serverImageView.setVisibility(View.VISIBLE);
      else
        serverItem.serverImageView.setVisibility(View.INVISIBLE);
      listServerWrap.addView(serverItem, params);

    }

  }

  // Change language
  private void updateLocallize(String localize) {
    try {
      SharedPreferences.Editor editor = LocalizationHelper.getInstance().getSharePrefs().edit();
      editor.putString(ExoConstants.EXO_PRF_LOCALIZE, localize);
      editor.commit();
      LocalizationHelper.getInstance().setLocation(localize);
      ResourceBundle bundle = new PropertyResourceBundle(this.getAssets().open(localize));
      LocalizationHelper.getInstance().setResourceBundle(bundle);
      changeLanguage();
      setServerList(ServerSettingHelper.getInstance().getServerInfoList());

    } catch (Exception e) {

    }

  }

  // Set language
  public void changeLanguage() {
    LocalizationHelper local = LocalizationHelper.getInstance();
    String strLanguageTittle = "";
    String strServerTittle = "";
    String strEnglish = "";
    String strFrench = "";
    String strCloseModifyServerLisrButton = "";
    String modifyListText = "";

    try {
      strLanguageTittle = local.getString("Language");
      strServerTittle = local.getString("Server");
      strEnglish = local.getString("English");
      strFrench = local.getString("French");
      strCloseModifyServerLisrButton = local.getString("ModifyServerList");
      
    } catch (Exception e) {

    }
    String settings = local.getString("Settings");
    setTitle(settings);
    modifyListText = local.getString("ModifyServerList");
    modifyServerBtn.setText(modifyListText);
    txtvEnglish.setText(strEnglish);
    txtvFrench.setText(strFrench);

    txtvServer.setText(strServerTittle);
    txtvLanguage.setText(strLanguageTittle);

  }

  
}
