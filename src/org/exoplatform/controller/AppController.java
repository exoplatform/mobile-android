package org.exoplatform.controller;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.exoplatform.R;
import org.exoplatform.dashboard.ExoGadget;
import org.exoplatform.dashboard.ExoWebViewController;
import org.exoplatform.proxy.ExoServerConfiguration;
import org.exoplatform.proxy.ServerObj;
import org.exoplatform.setting.ExoSetting;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

//Login page
public class AppController extends Activity implements OnTouchListener {

  /** Called when the activity is first created. */
  // References keys
  public static final String                EXO_PREFERENCE       = "exo_preference";

  public static final String                EXO_PRF_DOMAIN       = "exo_prf_domain";

  public static final String                EXO_PRF_DOMAIN_INDEX = "exo_prf_domain_index";

  public static final String                EXO_PRF_USERNAME     = "exo_prf_username";

  public static final String                EXO_PRF_PASSWORD     = "exo_prf_password";

  public static final String                EXO_PRF_LANGUAGE     = "exo_prf_language";

  public static final String                EXO_PRF_LOCALIZE     = "exo_prf_localize";

  public static boolean                     isNewVersion;

  // Authentication
  public static AuthScope                   auth                 = null;

  public static UsernamePasswordCredentials credential           = null;

  // Preferences
  public static SharedPreferences           sharedPreference;

  // Localization
  public static ResourceBundle              bundle;

  private Runnable                          viewOrders;

  // Host
  public static String                      _strDomain           = "";

  // Active host index
  private static String                     _strDomainIndex      = "";

  public static int                         _intDomainIndex;

  // Username
  String                                    _strUserName         = "";

  // Password
  String                                    _strPassword         = "";

  // Standalone gadget content
  private String                            _strContentForStandaloneURL;

  // Connect to server
  // public static ExoConnection _eXoConnection = new ExoConnection();

  // UI component

  ImageView                                 _imageAccount;

  ImageView                                 _imageServer;

  RelativeLayout                            _imagePanelBackground;

  Button                                    _btnAccount;

  Button                                    _btnServer;

  Button                                    _btnLogIn;

  TextView                                  _tvLogIn;

  EditText                                  _edtxUserName;

  EditText                                  _edtxPassword;

  static ListView                           _listViewServer;

  // Connection status
  String                                    strWait;

  String                                    strSigning;

  String                                    strNetworkConnextionFailed;

  String                                    strUserNamePasswordFailed;

  String                                    strCannotBackToPreviousPage;

  String                                    strLoadingDataFromServer;

  private String                            settingText;

  // Point to itself
  public static AppController               appControllerInstance;

  public static ExoServerConfiguration      configurationInstance;

  // Get data thread
  Thread                                    thread;

  // Login progress dialog
  public static ProgressDialog              _progressDialog      = null;

  // Constructor
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    this.setContentView(R.layout.login);

    // String path = Environment.getExternalStorageDirectory() +
    // "/eXo/DefaultServerList.xml";
    // File file = new File(path);
    // boolean deleted = file.delete();

    RelativeLayout layout = (RelativeLayout) findViewById(R.id.RelativeLayout_Login);
    layout.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_edtxUserName.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(_edtxPassword.getWindowToken(), 0);

      }
    });
    configurationInstance = new ExoServerConfiguration(this);

    String appVer = "";
    String oldVer = configurationInstance.getAppVersion();
    try {
      appVer = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      // Log.v(tag, e.getMessage());
    }

    // If SDCard is not available
    if (!(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED))) {
      configurationInstance._arrDefaulServerList = configurationInstance.getDefaultServerList();
      if (configurationInstance._arrDefaulServerList.size() > 0)
        configurationInstance._arrServerList.addAll(configurationInstance._arrDefaulServerList);
    } else {
      ArrayList<ServerObj> defaultServerList = configurationInstance.getServerListWithFileName("DefaultServerList.xml");

      configurationInstance._arrDeletedServerList = configurationInstance.getServerListWithFileName("DeletedDefaultServerList.xml");
      if (appVer.compareToIgnoreCase(oldVer) > 0) {

        ArrayList<ServerObj> deletedDefaultServers = configurationInstance._arrDeletedServerList;

        ArrayList<ServerObj> tmp = new ArrayList<ServerObj>();
        if (deletedDefaultServers == null)
          tmp = defaultServerList;
        else {
          for (int i = 0; i < defaultServerList.size(); i++) {
            ServerObj newServerObj = defaultServerList.get(i);
            boolean isDeleted = false;
            for (int j = 0; j < deletedDefaultServers.size(); j++) {
              ServerObj deletedServerObj = deletedDefaultServers.get(i);
              if (newServerObj._strServerName.equalsIgnoreCase(deletedServerObj._strServerName)
                  && newServerObj._strServerUrl.equalsIgnoreCase(deletedServerObj._strServerUrl)) {
                isDeleted = true;
                break;
              }
            }
            if (!isDeleted)
              tmp.add(newServerObj);
          }
        }

        configurationInstance.createXmlDataWithServerList(tmp, "DefaultServerList.xml", appVer);
        configurationInstance.version = appVer;
      } else {

      }

      configurationInstance._arrUserServerList = configurationInstance.getServerListWithFileName("UserServerList.xml");
      configurationInstance._arrDefaulServerList = configurationInstance.getServerListWithFileName("DefaultServerList.xml");

      if (configurationInstance._arrDefaulServerList.size() > 0)
        configurationInstance._arrServerList.addAll(configurationInstance._arrDefaulServerList);
      if (configurationInstance._arrUserServerList.size() > 0)
        configurationInstance._arrServerList.addAll(configurationInstance._arrUserServerList);
    }

    String strLocalize;

    appControllerInstance = this;

    if (sharedPreference == null) {
      sharedPreference = getSharedPreferences(EXO_PREFERENCE, 0);
    }

    strLocalize = sharedPreference.getString(EXO_PRF_LOCALIZE, "exo_prf_localize");
    if (strLocalize == null || strLocalize.equalsIgnoreCase("exo_prf_localize"))
      strLocalize = "LocalizeEN.properties";

    try {

      bundle = new PropertyResourceBundle(getAssets().open(strLocalize));

    } catch (Exception e) {

    }

    _imageAccount = (ImageView) findViewById(R.id.Image_Account);
    _imageServer = (ImageView) findViewById(R.id.Image_Server);
    _imagePanelBackground = (RelativeLayout) findViewById(R.id.Image_Panel_Background);

    _edtxUserName = (EditText) findViewById(R.id.EditText_UserName);
    _edtxPassword = (EditText) findViewById(R.id.EditText_Password);
    _edtxPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
    _edtxPassword.setImeActionLabel("Connect", EditorInfo.IME_ACTION_DONE);

    _btnAccount = (Button) findViewById(R.id.Button_Account);
    _btnServer = (Button) findViewById(R.id.Button_Server);
    _btnLogIn = (Button) findViewById(R.id.Button_Login);
    // _tvLogIn = (TextView) findViewById(R.id.TextView_Login);

    _listViewServer = (ListView) findViewById(R.id.ListView_Servers);
    _listViewServer.setVisibility(View.INVISIBLE);
    _listViewServer.setDivider(null);
    _listViewServer.setDividerHeight(0);

    _strDomain = sharedPreference.getString(EXO_PRF_DOMAIN, "");
    _strDomainIndex = sharedPreference.getString(EXO_PRF_DOMAIN_INDEX, "");
    _strUserName = sharedPreference.getString(EXO_PRF_USERNAME, "");
    _strPassword = sharedPreference.getString(EXO_PRF_PASSWORD, "");

    _edtxUserName.setText(_strUserName);
    _edtxPassword.setText(_strPassword);

    _edtxUserName.setSingleLine(true);

    changeLanguage(bundle);

    _btnAccount.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        v.setBackgroundResource(R.drawable.authenticatepanelbuttonbgon);
        _imageAccount.setBackgroundResource(R.drawable.authenticatecredentialsiconiphoneon);

        _btnServer.setBackgroundResource(R.drawable.authenticatepanelbuttonbgoff);
        _imageServer.setBackgroundResource(R.drawable.authenticateserversiconiphoneoff);

        _edtxUserName.setVisibility(View.VISIBLE);
        _edtxPassword.setVisibility(View.VISIBLE);

        _btnLogIn.setVisibility(View.VISIBLE);
        // _tvLogIn.setVisibility(View.VISIBLE);

        _listViewServer.setVisibility(View.INVISIBLE);
      }
    });

    _btnServer.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_edtxUserName.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(_edtxPassword.getWindowToken(), 0);

        v.setBackgroundResource(R.drawable.authenticatepanelbuttonbgon);
        _imageServer.setBackgroundResource(R.drawable.authenticateserversiconiphoneon);

        _btnAccount.setBackgroundResource(R.drawable.authenticatepanelbuttonbgoff);
        _imageAccount.setBackgroundResource(R.drawable.authenticatecredentialsiconiphoneoff);

        _edtxUserName.setVisibility(View.INVISIBLE);

        _edtxPassword.setVisibility(View.INVISIBLE);

        _btnLogIn.setVisibility(View.INVISIBLE);
        // _tvLogIn.setVisibility(View.INVISIBLE);

        _listViewServer.setVisibility(View.VISIBLE);
      }
    });

    _btnLogIn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {

        _btnLogIn.setVisibility(View.INVISIBLE);

        viewOrders = new Runnable() {
          public void run() {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(_edtxUserName.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(_edtxPassword.getWindowToken(), 0);

            signInProgress();

          }

        };

        thread = new Thread(null, viewOrders, "SigningIn");
        thread.start();

        _progressDialog = ProgressDialog.show(AppController.this, strWait, strSigning);
        // _progressDialog.setIcon(R.drawable.wait);

      }
    });

    createServersAdapter(configurationInstance._arrServerList);
  }

  @Override
  protected void onResume() {
    super.onResume();
    changeLanguage(bundle);
  }

  public boolean onTouch(View v, MotionEvent event) {
    int action = event.getAction();
    if (action == MotionEvent.ACTION_DOWN) {

    } else if (action == MotionEvent.ACTION_MOVE) {
      // movement: cancel the touch press

    } else if (action == MotionEvent.ACTION_UP) {

    }

    return true;
  }

  // Key down listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    if (keyCode == KeyEvent.KEYCODE_BACK) {
      // Back to home application
      moveTaskToBack(true);
    }

    return false;
  }

  // Create Setting Menu
  public boolean onCreateOptionsMenu(Menu menu) {

    System.out.println("remove option item ");
    // menu.add(0, 1, 0, "Setting");
    menu.add(0, 1, 0, settingText).setIcon(R.drawable.optionsettingsbutton);
    // menu.add(0, 2, 0, "Delete Contact");
    // menu.add(0, 3, 0, "Exit");

    return true;

  }

  // Menu action
  public boolean onOptionsItemSelected(MenuItem item) {

    int selectedItemIndex = item.getItemId();

    if (selectedItemIndex == 1) {
      // eXoLanguageSettingDialog customizeDialog = new
      // eXoLanguageSettingDialog(AppController.this, 0, thisClass);
      // customizeDialog.show();

      // GDActivity.TYPE = 1;

      Intent next = new Intent(AppController.this, ExoSetting.class);
      next.putExtra(ExoConstants.SETTING_TYPE, 0);
      startActivity(next);

    }

    return false;
  }

  // Create server list adapter
  public static void createServersAdapter(ArrayList<ServerObj> serverObjs) {

    final List<ServerObj> serverObjsTmp = serverObjs;

    final BaseAdapter serverAdapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        LayoutInflater inflater = appControllerInstance.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.serverlistitem, parent, false);

        final ServerObj serverObj = serverObjsTmp.get(pos);

        TextView txtvServerName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
        txtvServerName.setText(serverObj._strServerName);

        TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
        txtvUrl.setText(serverObj._strServerUrl);

        ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        // RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams)
        // txtvUrl.getLayoutParams();
        _intDomainIndex = -1;
        try {
          _intDomainIndex = Integer.parseInt(_strDomainIndex);
        } catch (NumberFormatException e) {

        }

        if (_intDomainIndex == pos)
          imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneon);
        else
          imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneoff);

        // txtvUrl.setLayoutParams(layout);

        rowView.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            if (_intDomainIndex < 0)
              _intDomainIndex = pos;

            View rowView = getView(_intDomainIndex, null, _listViewServer);

            ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
            imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneoff);

            _strDomainIndex = String.valueOf(pos);
            _intDomainIndex = pos;
            _strDomain = serverObj._strServerUrl;

            rowView = getView(_intDomainIndex, null, _listViewServer);
            imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
            imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneon);

            notifyDataSetChanged();

          }
        });

        return (rowView);
      }

      public long getItemId(int position) {

        return position;
      }

      public Object getItem(int position) {

        return position;
      }

      public int getCount() {

        return serverObjsTmp.size();
      }
    };

    _listViewServer.setAdapter(serverAdapter);

    // _lstvFiles.setOnItemClickListener(test);
  }

  // Login successful
  private Runnable returnRes                      = new Runnable() {
                                                    public void run() {

                                                      _progressDialog.setMessage(strLoadingDataFromServer);

                                                      // eXoApplicationsController.arrGadgets
                                                      // = listOfGadgets();
                                                      // listOfGadgets();
                                                      Intent next = new Intent(AppController.this,
                                                                               ExoApplicationsController2.class);

                                                      startActivity(next);

                                                      // thread.stop();

                                                      _progressDialog.dismiss();
                                                      _btnLogIn.setVisibility(View.VISIBLE);
                                                      appControllerInstance = null;

                                                      finish();

                                                    }
                                                  };

  // Network connection is failed
  private Runnable returnResFaileConnection       = new Runnable() {
                                                    public void run() {
                                                      _progressDialog.dismiss();

                                                      AlertDialog.Builder builder = new AlertDialog.Builder(appControllerInstance);
                                                      builder.setMessage(strNetworkConnextionFailed);
                                                      builder.setCancelable(false);

                                                      builder.setPositiveButton("OK",
                                                                                new DialogInterface.OnClickListener() {
                                                                                  public void onClick(DialogInterface dialog,
                                                                                                      int id) {

                                                                                  }
                                                                                });

                                                      AlertDialog alert = builder.create();
                                                      alert.show();

                                                      thread.stop();
                                                      _btnLogIn.setVisibility(View.VISIBLE);

                                                    }
                                                  };

  // Invalid username/password
  private Runnable returnResFaileUserNamePassword = new Runnable() {
                                                    public void run() {
                                                      _progressDialog.dismiss();

                                                      AlertDialog.Builder builder = new AlertDialog.Builder(appControllerInstance);
                                                      builder.setMessage(strUserNamePasswordFailed);
                                                      builder.setCancelable(false);

                                                      builder.setPositiveButton("OK",
                                                                                new DialogInterface.OnClickListener() {
                                                                                  public void onClick(DialogInterface dialog,
                                                                                                      int id) {

                                                                                  }
                                                                                });

                                                      AlertDialog alert = builder.create();
                                                      alert.show();

                                                      thread.stop();
                                                      _btnLogIn.setVisibility(View.VISIBLE);

                                                    }
                                                  };

  // Login progress
  public void signInProgress() {
    try {

      if (_strDomain.indexOf("http://") == -1) {
        _strDomain = "http://" + _strDomain;
      }

      // URL url = new URL(_strDomain);
      URI uri = new URI(_strDomain);

      // HttpURLConnection con = (HttpURLConnection) url.openConnection();
      // int code = con.getResponseCode();

      _strUserName = _edtxUserName.getText().toString();
      _strPassword = _edtxPassword.getText().toString();

      // String strResult = "NO";
      String strResult = ExoConnectionUtils.sendAuthentication(_strDomain,
                                                               _strUserName,
                                                               _strPassword);
      if (strResult.equalsIgnoreCase("YES")) {

        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(EXO_PRF_DOMAIN, _strDomain);
        editor.putString(EXO_PRF_DOMAIN_INDEX, _strDomainIndex);
        editor.putString(EXO_PRF_USERNAME, _strUserName);
        editor.putString(EXO_PRF_PASSWORD, _strPassword);
        editor.commit();

        createAuthorization(uri.getHost(), uri.getPort());
        isNewVersion = checkNewPLF();
        runOnUiThread(returnRes);
      } else if (strResult.equalsIgnoreCase("NO")) {
        runOnUiThread(returnResFaileUserNamePassword);
      } else {
        runOnUiThread(returnResFaileConnection);
      }

    } catch (Exception e) {
      System.out.println("error login" + e.getMessage());

      // String msg = e.getMessage();
      // String str = e.toString();
      // Log.v(str, msg);
      runOnUiThread(returnResFaileConnection);
    }

  }

  // Get gadget list
  public List<ExoGadget> getGadgetsList() {
    List<ExoGadget> arrGadgets = new ArrayList<ExoGadget>();
    _strDomain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN,
                                                          "exo_prf_domain");
    String strHomeUrl = _strDomain + "/portal/private/classic";
    String strContent = ExoConnectionUtils.sendRequestAndReturnString(strHomeUrl);

    String strGadgetMark = "eXo.gadget.UIGadget.createGadget";
    String title;
    String url;
    String description;
    Bitmap bmp;

    int indexStart;
    int indexEnd;
    String tmpStr = strContent;
    indexStart = tmpStr.indexOf(strGadgetMark);

    while (indexStart >= 0) {
      tmpStr = tmpStr.substring(indexStart + 1);
      indexEnd = tmpStr.indexOf(strGadgetMark);
      String tmpStr2;

      if (indexEnd < 0)
        tmpStr2 = tmpStr;
      else
        tmpStr2 = tmpStr.substring(0, indexEnd);

      // Get title
      title = parseUrl(tmpStr2, "\"title\":\"", true, "\"");

      // Get description
      description = parseUrl(tmpStr2, "\"description\":\"", true, "\"");

      // Get url
      url = _strDomain + "/eXoGadgetServer/gadgets/ifr?container=default&mid=0&nocache=0";

      // Get country
      url += parseUrl(tmpStr2, "&country=", false, "&");

      // Get view
      url += parseUrl(tmpStr2, "&view=", false, "&");

      // Get language
      url += parseUrl(tmpStr2, "&lang=", false, "&");

      url += "&parent=" + _strDomain + "&st=";

      // Get token
      url += parseUrl(tmpStr2, "default:", false, "\"");

      // Get xml url
      url += parseUrl(tmpStr2, "&url=", false, "\"");

      // Get bitmap
      String bmpUrl = parseUrl(tmpStr2, "\"thumbnail\":\"", true, "\"");
      bmpUrl = bmpUrl.replace("localhost", _strDomain);
      // bmp =
      // BitmapFactory.decodeStream(AppController._eXoConnection.sendRequest(bmpUrl));

      ExoGadget tempGadget = new ExoGadget(title, description, url, bmpUrl, null, null);
      arrGadgets.add(tempGadget);

      indexStart = indexEnd;

    }

    return arrGadgets;
  }

  // Parser gadget string data
  public String getStringForGadget(String gadgetStr, String startStr, String endStr) {
    String returnValue = "";
    int index1;
    int index2;

    index1 = gadgetStr.indexOf(startStr);

    if (index1 > 0) {
      String tmpStr = gadgetStr.substring(index1 + startStr.length());
      index2 = tmpStr.indexOf(endStr);
      if (index2 > 0)
        returnValue = tmpStr.substring(0, index2);
    }

    return returnValue;
  }

  // Get gadget list with URL
  public List<ExoGadget> listOfGadgetsWithURL(String url) {
    List<ExoGadget> arrTmpGadgets = new ArrayList<ExoGadget>();

    String strGadgetName;
    String strGadgetDescription;
    Bitmap imgGadgetIcon = null;

    String domain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN,
                                                             "exo_prf_domain");
    String userName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME,
                                                               "exo_prf_domain");
    String password = AppController.sharedPreference.getString(AppController.EXO_PRF_PASSWORD,
                                                               "exo_prf_domain");

    String strContent = "";

    int indexOfSocial = domain.indexOf("social");
    if (indexOfSocial > 0) {
      // dataReply = [[_delegate getConnection]
      // sendRequestToSocialToGetGadget:[url absoluteString]];
    } else {
      strContent = ExoConnectionUtils.sendRequestToGetGadget(url, userName, password);
    }

    _strContentForStandaloneURL = new String(strContent);

    int index1;
    int index2;

    index1 = strContent.indexOf("eXo.gadget.UIGadget.createGadget");

    do {
      if (index1 < 0)
        return null;
      strContent = strContent.substring(index1 + 32);
      index2 = strContent.indexOf("'/eXoGadgetServer/gadgets',");
      if (index2 < 0)
        return null;
      String tmpStr = strContent.substring(0, index2 + 45);

      strGadgetName = getStringForGadget(tmpStr, "\"title\":\"", "\",");
      strGadgetDescription = getStringForGadget(tmpStr, "\"description\":\"", "\",");
      String gadgetIconUrl = getStringForGadget(tmpStr, "\"thumbnail\":\"", "\",");
      String gadgetID = getStringForGadget(tmpStr, "'content-", "'");

      gadgetIconUrl = gadgetIconUrl.replace("http://localhost:8080", domain);

      try {
        // imgGadgetIcon =
        // BitmapFactory.decodeStream(AppController._eXoConnection.sendRequest(gadgetIconUrl));
        if (imgGadgetIcon == null) {
          try {
            // imgGadgetIcon =
            // BitmapFactory.decodeStream(getAssets().open("portletsicon.png"));
          } catch (Exception e2) {

            imgGadgetIcon = null;
          }
        }

      } catch (Exception e) {

        try {
          // imgGadgetIcon =
          // BitmapFactory.decodeStream(getAssets().open("portletsicon.png"));
        } catch (Exception e2) {

          imgGadgetIcon = null;
        }

      }

      String gadgetUrl = domain;

      gadgetUrl += getStringForGadget(tmpStr, "'home', '", "',") + "/";
      gadgetUrl += "ifr?container=default&mid=1&nocache=0&lang="
          + getStringForGadget(tmpStr, "&lang=", "\",") + "&debug=1&st=default";

      String token = ":" + getStringForGadget(tmpStr, "\"default:", "\",");
      token = token.replace(":", "%3A");
      token = token.replace("/", "%2F");
      token = token.replace("+", "%2B");

      gadgetUrl += token + "&url=";

      String gadgetXmlFile = getStringForGadget(tmpStr, "\"url\":\"", "\",");
      gadgetXmlFile = gadgetXmlFile.replace(":", "%3A");
      gadgetXmlFile = gadgetXmlFile.replace("/", "%2F");

      gadgetUrl += gadgetXmlFile;

      ExoGadget gadget = new ExoGadget(strGadgetName,
                                       strGadgetDescription,
                                       gadgetUrl,
                                       gadgetIconUrl,
                                       null,
                                       gadgetID);

      arrTmpGadgets.add(gadget);

      strContent = strContent.substring(index2 + 35);
      index1 = strContent.indexOf("eXo.gadget.UIGadget.createGadget");

    } while (index1 > 0);

    return arrTmpGadgets;
  }

  // Get needed string
  private String parseUrl(String urlStr, String neededStr, boolean offset, String enddedStr) {
    String str;
    int idx = urlStr.indexOf(neededStr);
    String tmp = urlStr.substring(idx + neededStr.length());
    idx = tmp.indexOf(enddedStr);
    if (!offset)
      str = neededStr + tmp.substring(0, idx);
    else
      str = tmp.substring(0, idx);

    return str;
  }

  // Standalone gadgets
  private HashMap<String, String> listOfStandaloneGadgetsURL() {
    HashMap<String, String> mapOfURLs = new HashMap<String, String>();
    String strContent = _strContentForStandaloneURL;

    int index1;
    int index2;

    String[] arrParagraphs = strContent.split("<div class=\"UIGadget\" id=\"");

    for (int i = 1; i < arrParagraphs.length; i++) {
      String tmpStr1 = arrParagraphs[i];

      String idString = tmpStr1.substring(0, 36);

      if (this.isAGadgetIDString(idString)) {

        index1 = tmpStr1.indexOf("standalone");
        if (index1 >= 0) {
          index2 = tmpStr1.indexOf("<a style=\"display:none\" href=\"");
          String strStandaloneUrl = "";
          if (index2 >= 0) {
            int mark = 0;
            for (int j = index2 + 30; j < tmpStr1.length(); j++) {
              if (tmpStr1.charAt(j) == '"') {
                mark = j;
                break;
              }
            }
            strStandaloneUrl = tmpStr1.substring(index2 + 30, mark);
          }

          if (strStandaloneUrl.length() > 0) {
            mapOfURLs.put(idString, strStandaloneUrl);
          }
        }
      }
    }
    return mapOfURLs;
  }

  // Check if it is a standalone gadget
  private boolean isAGadgetIDString(String potentialIDString) {
    if ((potentialIDString.charAt(8) == '-') && (potentialIDString.charAt(13) == '-'))
      return true;
    return false;
  }

  // Show user guide
  public void showUserGuide() {
    ExoApplicationsController2.webViewMode = 2;
  }

  // Generate authntication
  private void createAuthorization(String url, int port) {
    auth = new AuthScope(url, port);
    String userName = sharedPreference.getString(EXO_PRF_USERNAME, "");
    String password = sharedPreference.getString(EXO_PRF_PASSWORD, "");
    credential = new UsernamePasswordCredentials(userName, password);
  }

  private static boolean checkNewPLF() {
    return ExoConnectionUtils.checkPLFVersion();

  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {
    String strSignIn = "";
    String strUserName = "";
    String strPassword = "";

    try {
      strSignIn = new String(resourceBundle.getString("SignInButton").getBytes("ISO-8859-1"),
                             "UTF-8");
      strUserName = new String(resourceBundle.getString("UserNameCellTitle").getBytes("ISO-8859-1"),
                               "UTF-8");
      strPassword = new String(resourceBundle.getString("PasswordCellTitle").getBytes("ISO-8859-1"),
                               "UTF-8");
      strWait = new String(resourceBundle.getString("PleaseWait").getBytes("ISO-8859-1"), "UTF-8");
      strSigning = new String(resourceBundle.getString("SigningIn").getBytes("ISO-8859-1"), "UTF-8");
      strNetworkConnextionFailed = new String(resourceBundle.getString("NetworkConnectionFailed")
                                                            .getBytes("ISO-8859-1"), "UTF-8");
      strUserNamePasswordFailed = new String(resourceBundle.getString("UserNamePasswordFailed")
                                                           .getBytes("ISO-8859-1"), "UTF-8");
      strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage")
                                                             .getBytes("ISO-8859-1"), "UTF-8");
      strLoadingDataFromServer = new String(resourceBundle.getString("LoadingDataFromServer")
                                                          .getBytes("ISO-8859-1"), "UTF-8");
      settingText = bundle.getString("Settings");

    } catch (Exception e) {

    }

    _btnLogIn.setText(strSignIn);
    // _txtViewUserName.setText(strUserName);
    // _txtViewPassword.setText(strPassword);
  }

}
