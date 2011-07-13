package exo.exoplatform.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import exo.exoplatform.R;
import exo.exoplatform.chat.ExoChatListController;
import exo.exoplatform.dashboard.ExoGadgetViewController;
import exo.exoplatform.document.ExoFile;
import exo.exoplatform.document.ExoFilesController;
import exo.exoplatform.model.GateInDbItem;
import exo.exoplatform.model.ExoApp;

//Main app view controller
public class ExoApplicationsController extends Activity {
  ListView                         _lstvApps;                        // eXo

  // Apps
  // view

  ListView                         _lstvGadgets;                     // Gadgets

  // view

  TextView                         txtVieweXoAppsTittle;             // eXo app

  // title

  TextView                         txtVieweXoGadgetsTittle;          // Gadgets

  // title

  Button                           _btnSignOut;                      // Signout

  // button

  Button                           _btnLanguageHelp;                 // Setting

  public static short              webViewMode;                      // 0: view

  // gadget,
  // 1: View
  // file,
  // 2: view
  // help;

  String                           fileTittle;                       // File

  // app
  // name

  String                           chatTittle;                       // Chat

  // app
  // name

  private static ExoApp            exoapp;                           // eXo app

  private static eXoAppsAdapter    exoAppsAdapter;                   // eXo app

  // adapter

  ProgressDialog                   _progressDialog;                  // Progress

  // dialog

  static ExoApplicationsController eXoApplicationsControllerInstance; // Instance

  public static List<GateInDbItem> arrGadgets;                       // Gadgets

  // array

  public static GateInDbItem       gadgetTab;                        // Dashboard

  // tab

  Thread                           thread;

  // Localization strings
  TextView                         textViewFileDescription;

  TextView                         textViewChatDescription;

  TextView                         labelFileName;

  TextView                         labelChatName;

  String                           strCannotBackToPreviousPage;

  String                           strChatServer;

  // Constructor
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.appsview);

    eXoApplicationsControllerInstance = this;

    txtVieweXoAppsTittle = (TextView) findViewById(R.id.TextView_AppsTitle);
    txtVieweXoGadgetsTittle = (TextView) findViewById(R.id.TextView_GadgetsTitle);

    // Sign Out button
    _btnSignOut = (Button) findViewById(R.id.Button_SignOut);
    _btnSignOut.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        if (ExoChatListController.conn != null && ExoChatListController.conn.isAuthenticated()) {
          ExoChatListController.conn.getRoster()
                                    .removeRosterListener(ExoChatListController.rosterListener);
          ExoChatListController.conn.disconnect();
        }

        Intent next = new Intent(ExoApplicationsController.this, AppController.class);
        ExoApplicationsController.this.startActivity(next);
      }
    });

    _btnLanguageHelp = (Button) findViewById(R.id.Button_Language_Help);
    _btnLanguageHelp.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {

        // eXoLanguageSettingDialog customizeDialog = new
        // eXoLanguageSettingDialog(eXoApplicationsController.this, 1,
        // eXoApplicationsControllerInstance);
        // customizeDialog.show();
      }
    });

    changeLanguage(AppController.bundle);

    _lstvApps = (ListView) findViewById(R.id.ListView_Apps);
    //        
    createAdapter();

    // Gadgets List
    _lstvGadgets = (ListView) findViewById(R.id.ListView_Gadgets);
    if (arrGadgets != null) {
      GadgetsAdapter gadgetsAdapter = new GadgetsAdapter(arrGadgets);
      _lstvGadgets.setAdapter(gadgetsAdapter);
      _lstvGadgets.setOnItemClickListener(gadgetsAdapter);
      _lstvGadgets.setAdapter(gadgetsAdapter);
      _lstvGadgets.setOnItemClickListener(gadgetsAdapter);
    }

    AppController._progressDialog.dismiss();

  }

  // Kewdown listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      Toast.makeText(ExoApplicationsController.this, strCannotBackToPreviousPage, Toast.LENGTH_LONG)
           .show();

    }
    return false;
  }

  // Create adapter for eXo app
  public void createAdapter() {
    List<ExoApp> exoapps = new ArrayList<ExoApp>(2);
    exoapps.add(new ExoApp(chatTittle, ""));
    exoapps.add(new ExoApp(fileTittle, ""));
    exoAppsAdapter = new eXoAppsAdapter(exoapps);
    _lstvApps.setAdapter(exoAppsAdapter);
    _lstvApps.setOnItemClickListener(exoAppsAdapter);
  }

  // eXoAppsAdapter - it was used to be Data Source for Applications List View
  class eXoAppsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private final List<ExoApp> _arreXoApps;

    public eXoAppsAdapter(List<ExoApp> exoapps) {
      _arreXoApps = exoapps;
    }

    public int getCount() {
      return _arreXoApps.size();
    }

    public Object getItem(int position) {
      return position;
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = getLayoutInflater();
      View rowView = inflater.inflate(R.layout.gadgettabitem, parent, false);
      bindView(rowView, _arreXoApps.get(position));
      return (rowView);
    }

    private void bindView(View view, ExoApp app) {
      ImageView icon = (ImageView) view.findViewById(R.id.icon);
      if (app._streXoAppName.equalsIgnoreCase(fileTittle)) {

        labelFileName = (TextView) view.findViewById(R.id.label);
        labelFileName.setText(app._streXoAppName);

        icon.setImageResource(R.drawable.files_app_icn);
      } else if (app._streXoAppName.equalsIgnoreCase(chatTittle)) {
        labelChatName = (TextView) view.findViewById(R.id.label);
        labelChatName.setText(app._streXoAppName);

        if (ExoChatListController.conn == null || !ExoChatListController.conn.isConnected()
            || !ExoChatListController.conn.isAuthenticated())
          icon.setImageResource(R.drawable.offlineicon);
        else
          icon.setImageResource(R.drawable.onlineicon);
      }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      exoapp = _arreXoApps.get(position);
      launchApp();
    }

    private Runnable dismissProgressDialog = new Runnable() {

                                             public void run() {

                                               _progressDialog.dismiss();

                                               thread.stop();
                                               thread = null;
                                             }
                                           };

    public void launchApp() {

      Runnable loadingDataRunnable = new Runnable() {
        public void run() {

          if (exoapp._streXoAppName == fileTittle) {
            launchFilesApp();
          } else if (exoapp._streXoAppName == chatTittle) {
            launchMessengerApp();
          }

          runOnUiThread(dismissProgressDialog);
        }
      };

      String strLoadingDataFromServer = "";
      try {

        strLoadingDataFromServer = new String(AppController.bundle.getString("LoadingDataFromServer")
                                                                  .getBytes("ISO-8859-1"),
                                              "UTF-8");

      } catch (Exception e) {

        strLoadingDataFromServer = "";
      }

      _progressDialog = ProgressDialog.show(eXoApplicationsControllerInstance,
                                            null,
                                            strLoadingDataFromServer);

      thread = new Thread(loadingDataRunnable, "LoadDingData");
      thread.start();

    }

    public void launchFilesApp() {
      String userName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME,
                                                                 "exo_prf_username");
      String domain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN,
                                                               "exo_prf_domain");

      if (ExoFilesController.myFile == null) {
        ExoFilesController.myFile = new ExoFile();
        ExoFilesController.myFile.fileName = userName;
        ExoFilesController.myFile.urlStr = domain
            + "/rest/private/jcr/repository/collaboration/Users/" + userName;
        ExoFilesController._rootUrl = ExoFilesController.myFile.urlStr;
      }

      ExoFilesController.arrFiles = ExoFilesController.getPersonalDriveContent(ExoFilesController.myFile.urlStr);
      // eXoFilesController._delegate = eXoApplicationsControllerInstance;
      Intent next = new Intent(ExoApplicationsController.this, ExoFilesController.class);
      ExoApplicationsController.this.startActivity(next);

    }

    public void launchMessengerApp() {

      SharedPreferences sharedPreference = getSharedPreferences(AppController.EXO_PREFERENCE, 0);
      String urlStr = sharedPreference.getString(AppController.EXO_PRF_DOMAIN, "exo_prf_domain");

      URI url = null;

      try {
        url = new URI(urlStr);
      } catch (Exception e) {

      }

      String userName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME,
                                                                 "exo_prf_username");
      String password = AppController.sharedPreference.getString(AppController.EXO_PRF_PASSWORD,
                                                                 "exo_prf_password");

      connectToChatServer(url.getHost(), 5222, userName, password);
      if (ExoChatListController.conn == null || !ExoChatListController.conn.isConnected())
        return;

      // eXoChatListController._delegate = eXoApplicationsControllerInstance;
      Intent next = new Intent(ExoApplicationsController.this, ExoChatListController.class);
      ExoApplicationsController.this.startActivity(next);
    }
  }

  // Connect to Openfile server
  private void connectToChatServer(String host, int port, String userName, String password) {
    if (ExoChatListController.conn != null && ExoChatListController.conn.isConnected())
      return;

    ConnectionConfiguration config = new ConnectionConfiguration(host, port, "Work");
    ExoChatListController.conn = new XMPPConnection(config);

    try {
      ExoChatListController.conn.connect();
      ExoChatListController.conn.login(userName, password);

      runOnUiThread(new Runnable() {

        public void run() {

          // icon.setBackgroundResource(R.drawable.onlineicon);
          List<ExoApp> exoapps = new ArrayList<ExoApp>(2);
          exoapps.add(new ExoApp(fileTittle, ""));
          exoapps.add(new ExoApp(chatTittle, ""));
          exoAppsAdapter = new eXoAppsAdapter(exoapps);
          _lstvApps.setAdapter(exoAppsAdapter);
          _lstvApps.setOnItemClickListener(exoAppsAdapter);

        }
      });

      // exoAppsAdapter.notifyDataSetChanged();

    } catch (XMPPException e) {

      String str = e.toString();
      String msg = e.getMessage();
      Log.e(str, msg);
      // e.printStackTrace();
      Toast.makeText(ExoApplicationsController.this, strChatServer, Toast.LENGTH_SHORT).show();
    }

  }

  // Gadget Adapter - it was used to be Data Source for Gadgets List View
  class GadgetsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private final List<GateInDbItem> _arrGadgets;

    public GadgetsAdapter(List<GateInDbItem> gadgets) {
      _arrGadgets = gadgets;
    }

    public int getCount() {
      if (_arrGadgets == null)
        return 0;
      return _arrGadgets.size();
    }

    public Object getItem(int position) {
      return position;
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = getLayoutInflater();
      View rowView = inflater.inflate(R.layout.gadgettabitem, parent, false);
      bindView(rowView, _arrGadgets.get(position));
      return (rowView);
    }

    private void bindView(View view, GateInDbItem gadgetTab) {

      TextView label = (TextView) view.findViewById(R.id.label);

      label.setText(gadgetTab._strDbItemName);

      ImageView icon = (ImageView) view.findViewById(R.id.icon);
      icon.setImageResource(R.drawable.dashboard);

    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      // launchGadget(_arrGadgets.get(position));
      gadgetTab = _arrGadgets.get(position);
      launchGadget();
    }

    public void launchGadget() {
      ExoGadgetViewController._delegate = eXoApplicationsControllerInstance;
      Intent next = new Intent(ExoApplicationsController.this, ExoGadgetViewController.class);
      ExoApplicationsController.this.startActivity(next);

    }
  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {

    String strSignOut = "";
    String strTxtVieweXoAppsTittle = "";
    String strtxtVieweXoGadgetsTittle = "";
    String strfileTittle = "";
    String strchatTittle = "";

    try {
      strSignOut = new String(resourceBundle.getString("SignOutButton").getBytes("ISO-8859-1"),
                              "UTF-8");
      strTxtVieweXoAppsTittle = new String(resourceBundle.getString("NativeApplicationsHeader")
                                                         .getBytes("ISO-8859-1"), "UTF-8");
      strtxtVieweXoGadgetsTittle = new String(resourceBundle.getString("GadgetsHeader")
                                                            .getBytes("ISO-8859-1"), "UTF-8");
      strfileTittle = new String(resourceBundle.getString("FilesApplication")
                                               .getBytes("ISO-8859-1"), "UTF-8");
      strchatTittle = new String(resourceBundle.getString("ChatApplication").getBytes("ISO-8859-1"),
                                 "UTF-8");
      strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage")
                                                             .getBytes("ISO-8859-1"), "UTF-8");
      strChatServer = new String(resourceBundle.getString("ChatServer").getBytes("ISO-8859-1"),
                                 "UTF-8");
    } catch (Exception e) {

    }

    _btnSignOut.setText(strSignOut);
    txtVieweXoAppsTittle.setText(strTxtVieweXoAppsTittle);
    txtVieweXoGadgetsTittle.setText(strtxtVieweXoGadgetsTittle);

    fileTittle = strfileTittle;
    chatTittle = strchatTittle;

  }

}
