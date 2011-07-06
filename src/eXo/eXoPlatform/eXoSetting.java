package eXo.eXoPlatform;

import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.cyrilmottier.android.greendroid.R;

import eXo.eXoPlatform.AppController.ServerObj;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBarItem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class eXoSetting extends MyActionBar {

  public static eXoSetting eXoSettingInstance;

  Button                   btnUserGuide;

  View                     vEngLish, vFrench;

  TextView                 txtvEnglish, txtvFrench;

  ImageView                imgViewECheckMark, imgViewFCheckMark;

  int                      pageIDForChangeLanguage;             // 0:
                                                                 // AppController,
                                                                 // 1:

  // eXoApplicationController

  // 2: eXoFileController, 3: eXoChatList, 4: eXoChat
  // 5: eXoWebView

  TextView                 txtvLanguage;                        // Language
                                                                 // label

  TextView                 txtvServer;                          // Server label

  static ListView          listViewServer;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.exosetting);

    eXoSettingInstance = this;
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    
    setTitle("Setting");

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

        AppController.appControllerInstance.showUserGuide();
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

  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

    finish();
    return true;
  }
 
  // Key down listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      // Toast.makeText(AppController.this, strCannotBackToPreviousPage,
      // Toast.LENGTH_LONG).show();

    }

    return false;
  }

  public void finishMe() {
    Intent next;

    if (eXoApplicationsController2.eXoApplicationsController2Instance != null) {
      next = new Intent(eXoSetting.this, eXoApplicationsController2.class);
    } else {
      next = new Intent(eXoSetting.this, AppController.class);
    }

    startActivity(next);

    eXoSettingInstance = null;
//    GDActivity.TYPE = 0;
  }

  // Create server list adapter
  public void createServersAdapter(List<ServerObj> serverObjs) {

    final List<ServerObj> serverObjsTmp = serverObjs;

    BaseAdapter serverAdapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = eXoSettingInstance.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.serverlistitemforsetting, parent, false);

        TextView tvModifyTheList = (TextView) rowView.findViewById(R.id.TextView_Modify_The_List);
        TextView serverName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
        TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
        ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);

        if (position < serverObjsTmp.size()) {
          ServerObj serverObj = serverObjsTmp.get(position);

          serverName.setText(serverObj._strServerName);
          txtvUrl.setText(serverObj._strServerUrl);

          if (AppController.appControllerInstance._intDomainIndex == position)
            imgView.setVisibility(View.VISIBLE);
          else
            imgView.setVisibility(View.INVISIBLE);

        }

        if (position == serverObjsTmp.size()) {

          tvModifyTheList.setText("Modify the List");
          tvModifyTheList.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
              if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
                String msg = "You dont't have permission for this because SDCard is not available!";
                Toast.makeText(eXoSettingInstance, msg, Toast.LENGTH_LONG);
              } else {

//                GDActivity.TYPE = 1;

                Intent next = new Intent(eXoSetting.this, eXoModifyServerList.class);
                startActivity(next);
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

      if (pageIDForChangeLanguage == 0) {
        AppController controller = AppController.appControllerInstance;
        controller.changeLanguage(AppController.bundle);
      } else if (pageIDForChangeLanguage == 1) {
        eXoApplicationsController controller = eXoApplicationsController.eXoApplicationsControllerInstance;
        controller.changeLanguage(AppController.bundle);
        controller.createAdapter();
      } else if (pageIDForChangeLanguage == 2) {
        eXoFilesController controller = eXoFilesController.eXoFilesControllerInstance;
        controller.changeLanguage(AppController.bundle);
      } else if (pageIDForChangeLanguage == 3) {
        eXoChatListController controller = eXoChatListController.eXoChatListControllerInstance;
        controller.changeLanguage(AppController.bundle);
      } else if (pageIDForChangeLanguage == 4) {
        eXoChatController controller = eXoChatController.eXoChatControllerInstance;
        controller.changeLanguage(AppController.bundle);
      } else if (pageIDForChangeLanguage == 5) {
        eXoWebViewController controller = eXoWebViewController.eXoWebViewControllerInstance;
        controller.changeLanguage(AppController.bundle);
      } else if (pageIDForChangeLanguage == 6) {
        eXoGadgetViewController controller = eXoGadgetViewController.eXoGadgetViewControllerInstance;
        controller.changeLanguage(AppController.bundle);
      }

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

    txtvEnglish.setText(strEnglish);
    txtvFrench.setText(strFrench);

    btnUserGuide.setText(strUserGuideButton);

    txtvServer.setText(strServerTittle);
    txtvLanguage.setText(strLanguageTittle);

  }
}
