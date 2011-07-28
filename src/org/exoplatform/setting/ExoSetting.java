package org.exoplatform.setting;

import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.exoplatform.chat.ExoChatController;
import org.exoplatform.chat.ExoChatListController;
import org.exoplatform.controller.AppController;
import org.exoplatform.controller.ExoApplicationsController2;
import org.exoplatform.dashboard.ExoGadgetViewController;
import org.exoplatform.dashboard.ExoWebViewController;
import org.exoplatform.document.ExoFilesController;
import org.exoplatform.proxy.ServerObj;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;

import com.cyrilmottier.android.greendroid.R;

import greendroid.widget.ActionBarItem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ExoSetting extends MyActionBar {

  public static ExoSetting eXoSettingInstance;

  Button                   btnUserGuide;

  View                     vEngLish, vFrench;

  TextView                 txtvEnglish, txtvFrench;

  private TextView         tvModifyTheList;

  ImageView                imgViewECheckMark, imgViewFCheckMark;

  TextView                 txtvLanguage;                        // Language
                                                                 // label

  TextView                 txtvServer;                          // Server label

  static ListView          listViewServer;

  private String           modifyListText;

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

    btnUserGuide = (Button) findViewById(R.id.Button_User_Guide);

    vEngLish = (View) findViewById(R.id.View_English);
    txtvEnglish = (TextView) findViewById(R.id.TextView_English);
    imgViewECheckMark = (ImageView) findViewById(R.id.ImageView_CheckMark_EN);

    vFrench = (View) findViewById(R.id.View_French);
    txtvFrench = (TextView) findViewById(R.id.TextView_French);
    imgViewFCheckMark = (ImageView) findViewById(R.id.ImageView_CheckMark_FR);

    listViewServer = (ListView) findViewById(R.id.ListView_Servers);
    listViewServer.setDivider(null);
    listViewServer.setDividerHeight(0);

    btnUserGuide.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        ExoApplicationsController2.webViewMode = 2;
        Intent next = new Intent(ExoSetting.this, ExoWebViewController.class);
        startActivity(next);
        finish();
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

    String locallize = AppController.sharedPreference.getString(AppController.EXO_PRF_LOCALIZE,
                                                                "exo_prf_localize");
    if (locallize.equalsIgnoreCase("LocalizeFR.properties")) {
      imgViewFCheckMark.setVisibility(View.VISIBLE);
      imgViewECheckMark.setVisibility(View.INVISIBLE);
    } else {
      imgViewECheckMark.setVisibility(View.VISIBLE);
      imgViewFCheckMark.setVisibility(View.INVISIBLE);
    }

    createServersAdapter(AppController.configurationInstance._arrServerList);

    changeLanguage(AppController.bundle);

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

  // Create server list adapter
  public void createServersAdapter(List<ServerObj> serverObjs) {

    final List<ServerObj> serverObjsTmp = serverObjs;

    BaseAdapter serverAdapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = eXoSettingInstance.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.serverlistitemforsetting, parent, false);

        tvModifyTheList = (TextView) rowView.findViewById(R.id.TextView_Modify_The_List);
        tvModifyTheList.setText(modifyListText);
        TextView serverName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
        TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
        ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);

        if (position < serverObjsTmp.size()) {
          ServerObj serverObj = serverObjsTmp.get(position);

          serverName.setText(serverObj._strServerName);
          txtvUrl.setText(serverObj._strServerUrl);

          if (AppController._intDomainIndex == position)
            imgView.setVisibility(View.VISIBLE);
          else
            imgView.setVisibility(View.INVISIBLE);

        }

        if (position == serverObjsTmp.size()) {

          tvModifyTheList.setOnClickListener(new View.OnClickListener() {

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

          tvModifyTheList.setVisibility(View.VISIBLE);
          serverName.setVisibility(View.INVISIBLE);
          imgView.setVisibility(View.INVISIBLE);
          txtvUrl.setVisibility(View.INVISIBLE);
        }

        return (rowView);

      }

      public long getItemId(int position) {

        return position;
      }

      public Object getItem(int position) {

        return position;
      }

      public int getCount() {
        return serverObjsTmp.size() + 1;
      }
    };

    listViewServer.setAdapter(serverAdapter);
  }

  // Change language
  private void updateLocallize(String localize) {
    try {
      SharedPreferences.Editor editor = AppController.sharedPreference.edit();
      editor.putString(AppController.EXO_PRF_LOCALIZE, localize);
      editor.commit();

      AppController.bundle = new PropertyResourceBundle(this.getAssets().open(localize));
      changeLanguage(AppController.bundle);
      createServersAdapter(AppController.configurationInstance._arrServerList);

    } catch (Exception e) {

    }

  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {
    String strLanguageTittle = "";
    String strServerTittle = "";
    String strEnglish = "";
    String strFrench = "";
    String strCloseModifyServerLisrButton = "";
    String strUserGuideButton = "";

    try {
      strLanguageTittle = new String(resourceBundle.getString("Language").getBytes("ISO-8859-1"),
                                     "UTF-8");
      strServerTittle = new String(resourceBundle.getString("Server").getBytes("ISO-8859-1"),
                                   "UTF-8");
      strEnglish = new String(resourceBundle.getString("English").getBytes("ISO-8859-1"), "UTF-8");
      strFrench = new String(resourceBundle.getString("French").getBytes("ISO-8859-1"), "UTF-8");
      strCloseModifyServerLisrButton = new String(resourceBundle.getString("ModifyServerList")
                                                                .getBytes("ISO-8859-1"), "UTF-8");
      strUserGuideButton = new String(resourceBundle.getString("UserGuide").getBytes("ISO-8859-1"),
                                      "UTF-8");
    } catch (Exception e) {

    }
    String settings = resourceBundle.getString("Settings");
    setTitle(settings);
    modifyListText = resourceBundle.getString("ModifyServerList");

    txtvEnglish.setText(strEnglish);
    txtvFrench.setText(strFrench);

    btnUserGuide.setText(strUserGuideButton);

    txtvServer.setText(strServerTittle);
    txtvLanguage.setText(strLanguageTittle);

  }
}
