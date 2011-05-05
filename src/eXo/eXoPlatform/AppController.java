package eXo.eXoPlatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

//Login page
public class AppController extends Activity implements OnTouchListener {
  // Server info object
  class ServerObj {
    String  _strServerName; // Name of server

    String  _strServerUrl; // URL of server

    boolean _bSystemServer; // Is default server
  }

  // Actions with server configuration files
  class Configuration {

    ArrayList<ServerObj> _arrUserServerList;

    ArrayList<ServerObj> _arrDefaulServerList;

    ArrayList<ServerObj> _arrDeletedServerList;

    ArrayList<ServerObj> _arrServerList;

    String               version;

    public Configuration() {
      _arrServerList = new ArrayList<ServerObj>();
    }

    // Constructor

    public boolean createLocalFileDirectory(String path, boolean isFolder) {
      boolean returnValue = false;

      File f = new File(path);
      try {
        if (!f.exists()) {
          if (isFolder)
            returnValue = f.mkdir();
          else
            returnValue = f.createNewFile();
        }
      } catch (Exception e) {

      }

      return returnValue;
    }

    // Get app's current version, create DefaultServerList.xml if needed
    public String getAppVersion() {
      if (!(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED))) {
        return "0";
      } else {
        String filePath = "/sdcard/eXo/DefaultServerList.xml";
        createLocalFileDirectory("/sdcard/eXo", true);
        File file = new File(filePath);
        if (!file.exists()) {
          createXmlDataWithServerList(getDefaultServerList(), "DefaultServerList.xml", "0");
          return "0";
        }

        InputStream obj_is = null;
        Document obj_doc = null;
        DocumentBuilderFactory doc_build_fact = null;
        DocumentBuilder doc_builder = null;
        try {
          obj_is = new FileInputStream(filePath);
          doc_build_fact = DocumentBuilderFactory.newInstance();
          doc_builder = doc_build_fact.newDocumentBuilder();

          obj_doc = doc_builder.parse(obj_is);

          NodeList obj_nod_list = null;
          if (null != obj_doc) {
            org.w3c.dom.Element feed = obj_doc.getDocumentElement();
            obj_nod_list = feed.getElementsByTagName("version");

            for (int i = 0; i < obj_nod_list.getLength(); i++) {
              Node itemNode = obj_nod_list.item(i);
              if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) itemNode;
                return itemElement.getAttribute("number");
              }
            }
          }

        } catch (Exception e) {

        }
      }

      return "0";
    }

    // Get default server list
    public ArrayList<ServerObj> getDefaultServerList() {

      ArrayList<ServerObj> arrServerList = getServerListWithFileName("");
      XmlResourceParser parser = getResources().getXml(R.xml.defaultconfiguaration);

      try {
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
          String name = null;

          switch (eventType) {
          case XmlPullParser.START_TAG:
            name = parser.getName().toLowerCase();

            if (name.equalsIgnoreCase("server")) {
              ServerObj serverObj = new ServerObj();
              for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attribute = parser.getAttributeName(i).toLowerCase();
                if (attribute.equalsIgnoreCase("name")) {
                  serverObj._strServerName = parser.getAttributeValue(i);
                } else if (attribute.equalsIgnoreCase("serverURL")) {
                  serverObj._strServerUrl = parser.getAttributeValue(i);
                }
              }
              serverObj._bSystemServer = true;
              arrServerList.add(serverObj);
            }

            break;
          case XmlPullParser.END_TAG:
            name = parser.getName();
            break;
          }

          try {
            eventType = parser.next();
          } catch (Exception e) {

            eventType = 0;
          }

        }
      } catch (XmlPullParserException e) {
        throw new RuntimeException("Cannot parse XML");
      }

      String filePath = "/sdcard/eXo/DefaultServerList.xml";
      File file = new File(filePath);
      if (!file.exists()) {
        try {

          this.createXmlDataWithServerList(arrServerList, "DefaultServerList.xml", "0");

        } catch (Exception e) {

        }

      }

      return arrServerList;

    }

    // Get added/deleted servers
    public ArrayList<ServerObj> getServerListWithFileName(String name) {

      ArrayList<ServerObj> arrServerList = new ArrayList<ServerObj>();

      String filePath = "/sdcard/eXo/" + name;
      File file = new File(filePath);
      if (!file.exists()) {
        return arrServerList;
      }

      InputStream obj_is = null;
      Document obj_doc = null;
      DocumentBuilderFactory doc_build_fact = null;
      DocumentBuilder doc_builder = null;
      try {
        obj_is = new FileInputStream(filePath);
        doc_build_fact = DocumentBuilderFactory.newInstance();
        doc_builder = doc_build_fact.newDocumentBuilder();

        obj_doc = doc_builder.parse(obj_is);

        NodeList obj_nod_list = null;
        if (null != obj_doc) {
          org.w3c.dom.Element feed = obj_doc.getDocumentElement();
          obj_nod_list = feed.getElementsByTagName("server");

          for (int i = 0; i < obj_nod_list.getLength(); i++) {
            Node itemNode = obj_nod_list.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
              Element itemElement = (Element) itemNode;

              ServerObj serverObj = new ServerObj();
              serverObj._strServerName = itemElement.getAttribute("name");
              serverObj._strServerUrl = itemElement.getAttribute("serverURL");
              serverObj._bSystemServer = false;
              if (name.equalsIgnoreCase("DefaultServerList.xml"))
                serverObj._bSystemServer = true;

              arrServerList.add(serverObj);
            }
          }

        }

      } catch (Exception e) {

      }

      return arrServerList;

    }

    // Create user configuration file: deleted & added servers
    public boolean createXmlDataWithServerList(ArrayList<ServerObj> objList,
                                               String fileName,
                                               String appVersion) {
      boolean returnValue = false;
      String path = Environment.getExternalStorageDirectory() + "/eXo/" + fileName;
      File newxmlfile = new File(path);
      try {
        newxmlfile.createNewFile();

      } catch (IOException e) {

        Log.e("IOException", "exception in createNewFile() method");
        return returnValue;
      }

      // we have to bind the new file with a FileOutputStream
      FileOutputStream fileos = null;

      try {
        fileos = new FileOutputStream(newxmlfile);

      } catch (FileNotFoundException e) {

        Log.e("FileNotFoundException", "can't create FileOutputStream");
      }

      // we create a XmlSerializer in order to write xml data
      XmlSerializer serializer = Xml.newSerializer();
      try {
        // we set the FileOutputStream as output for the serializer, using UTF-8
        // encoding
        serializer.setOutput(fileos, "UTF-8");

        // Write <?xml declaration with encoding (if encoding not null) and
        // standalone flag (if standalone not null)
        serializer.startDocument(null, Boolean.valueOf(true));

        // set indentation option
        // serializer.setFeature(name,
        // state)("http://xmlpull.org/v1/doc/features.htmlindent-output", true);

        // start a tag called "root"
        serializer.startTag(null, "xml");
        if (fileName.equalsIgnoreCase("DefaultServerList.xml")) {
          serializer.startTag(null, "version");
          serializer.attribute(null, "number", appVersion);
          serializer.endTag(null, "version");
        }

        serializer.startTag(null, "Servers");

        // i indent code just to have a view similar to xml-tree
        for (int i = 0; i < objList.size(); i++) {
          ServerObj serverObj = objList.get(i);
          serializer.startTag(null, "server");
          serializer.attribute(null, "name", serverObj._strServerName);
          serializer.attribute(null, "serverURL", serverObj._strServerUrl);
          serializer.endTag(null, "server");
        }

        serializer.endTag(null, "Servers");
        serializer.endTag(null, "xml");

        serializer.endDocument();

        // write xml data into the FileOutputStream
        serializer.flush();
        // finally we close the file stream

        fileos.close();

      } catch (Exception e) {

        Log.e("Exception", "error occurred while creating xml file");
      }

      return returnValue;
    }

  }

  /** Called when the activity is first created. */
  // References keys
  public static final String                EXO_PREFERENCE       = "exo_preference";

  public static final String                EXO_PRF_DOMAIN       = "exo_prf_domain";

  public static final String                EXO_PRF_DOMAIN_INDEX = "exo_prf_domain_index";

  public static final String                EXO_PRF_USERNAME     = "exo_prf_username";

  public static final String                EXO_PRF_PASSWORD     = "exo_prf_password";

  public static final String                EXO_PRF_LANGUAGE     = "exo_prf_language";

  public static final String                EXO_PRF_LOCALIZE     = "exo_prf_localize";

  // Authentication
  public static AuthScope                   auth                 = null;

  public static UsernamePasswordCredentials credential           = null;

  // Preferences
  public static SharedPreferences           sharedPreference;

  // Localization
  public static ResourceBundle              bundle;

  private Runnable                          viewOrders;

  // Host
  String                                    _strDomain           = "";

  // Active host index
  String                                    _strDomainIndex      = "";

  int                                       _intDomainIndex;

  // Username
  String                                    _strUserName         = "";

  // Password
  String                                    _strPassword         = "";

  // Standalone gadget content
  private String                            _strContentForStandaloneURL;

  // Connect to server
  public static eXoConnection               _eXoConnection       = new eXoConnection();

  // UI component
  Button                                    _btnAccount;

  Button                                    _btnServer;

  Button                                    _btnLogIn;

  TextView                                  _txtViewUserName;

  TextView                                  _txtViewPassword;

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

  // Point to itself
  public static AppController               appControllerInstance;

  public static Configuration               configurationInstance;

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

    RelativeLayout layout = (RelativeLayout)findViewById(R.id.RelativeLayout_Login);
    layout.setOnClickListener(new View.OnClickListener() {
      
      public void onClick(View v) {
        // TODO Auto-generated method stub
        Log.e("", "");
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_edtxUserName.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(_edtxPassword.getWindowToken(), 0);
        
      }
    });
    configurationInstance = new Configuration();

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
    
    if (sharedPreference == null)
      sharedPreference = getSharedPreferences(EXO_PREFERENCE, 0);

    strLocalize = sharedPreference.getString(EXO_PRF_LOCALIZE, "exo_prf_localize");
    if (strLocalize == null || strLocalize.equalsIgnoreCase("exo_prf_localize"))
      strLocalize = "LocalizeEN.properties";

    try {

      bundle = new PropertyResourceBundle(getAssets().open(strLocalize));

    } catch (Exception e) {

    }

    _txtViewUserName = (TextView) findViewById(R.id.TextView_UserName);
    _txtViewPassword = (TextView) findViewById(R.id.TextView_Password);

    _edtxUserName = (EditText) findViewById(R.id.EditText_UserName);
    _edtxPassword = (EditText) findViewById(R.id.EditText_Password);
    _edtxPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
    _edtxPassword.setImeActionLabel("Connect", EditorInfo.IME_ACTION_DONE);

    _btnAccount = (Button) findViewById(R.id.Button_Account);
    _btnServer = (Button) findViewById(R.id.Button_Server);
    _btnLogIn = (Button) findViewById(R.id.Button_Login);

    _listViewServer = (ListView) findViewById(R.id.ListView_Servers);
    _listViewServer.setVisibility(View.INVISIBLE);

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
        
        _txtViewUserName.setVisibility(View.VISIBLE);
        _edtxUserName.setVisibility(View.VISIBLE);

        _txtViewPassword.setVisibility(View.VISIBLE);
        _edtxPassword.setVisibility(View.VISIBLE);

        _btnLogIn.setVisibility(View.VISIBLE);

        _listViewServer.setVisibility(View.INVISIBLE);
      }
    });

    _btnServer.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_edtxUserName.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(_edtxPassword.getWindowToken(), 0);
        
        _txtViewUserName.setVisibility(View.INVISIBLE);
        _edtxUserName.setVisibility(View.INVISIBLE);

        _txtViewPassword.setVisibility(View.INVISIBLE);
        _edtxPassword.setVisibility(View.INVISIBLE);

        _btnLogIn.setVisibility(View.INVISIBLE);

        _listViewServer.setVisibility(View.VISIBLE);
      }
    });

    _btnLogIn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
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

  public boolean onTouch(View v, MotionEvent event)
  {
   int action = event.getAction();
   if (action == MotionEvent.ACTION_DOWN)
   {
    
   }
   else if (action == MotionEvent.ACTION_MOVE)
   {
    // movement: cancel the touch press
    
   }
   else if (action == MotionEvent.ACTION_UP)
   {
    
   }

   return true;
  }

  // Key down listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      Toast.makeText(AppController.this, strCannotBackToPreviousPage, Toast.LENGTH_LONG).show();
    }

    return false;
  }

  // Create Setting Menu
  public boolean onCreateOptionsMenu(Menu menu) {

    menu.add(0, 1, 0, "Setting");

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

      Intent next = new Intent(AppController.this, eXoSetting.class);
      startActivity(next);
    }

    return false;
  }

  // Create server list adapter
  public void createServersAdapter(ArrayList<ServerObj> serverObjs) {

    final List<ServerObj> serverObjsTmp = serverObjs;

    final BaseAdapter serverAdapter = new BaseAdapter() {
      
      public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;

        LayoutInflater inflater = appControllerInstance.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.serverlistitem, parent, false);

        final ServerObj serverObj = serverObjsTmp.get(position);

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

        if (_intDomainIndex == position)
          imgView.setVisibility(View.VISIBLE);
        else
          imgView.setVisibility(View.INVISIBLE);

        // txtvUrl.setLayoutParams(layout);

        rowView.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            if(_intDomainIndex == -1)
              _intDomainIndex = pos;
            
            View rowView =  getView(_intDomainIndex, null, _listViewServer); 
              
            ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
            imgView.setVisibility(View.INVISIBLE);
            
            _strDomainIndex = String.valueOf(pos);
            _intDomainIndex = pos;
            _strDomain = serverObj._strServerUrl;
            
            rowView = getView(_intDomainIndex, null, _listViewServer); 
            imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
            imgView.setVisibility(View.VISIBLE);
            
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
                                                      listOfGadgets();
                                                      Intent next = new Intent(AppController.this,
                                                                               eXoApplicationsController2.class);
                                                      
                                                      startActivity(next);

                                                      thread.stop();

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

                                                    }
                                                  };

  // Login progress
  public void signInProgress() {
    try {

      if (_strDomain.indexOf("http://") == -1) {
        _strDomain = "http://" + _strDomain;
      }

      URL url = new URL(_strDomain);
      // HttpURLConnection con = (HttpURLConnection) url.openConnection();
      // int code = con.getResponseCode();

      _strUserName = _edtxUserName.getText().toString();
      _strPassword = _edtxPassword.getText().toString();

      // String strResult = "NO";
      String strResult = _eXoConnection.sendAuthentication(_strDomain, _strUserName, _strPassword);
      if (strResult.equalsIgnoreCase("YES")) {

        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(EXO_PRF_DOMAIN, _strDomain);
        editor.putString(EXO_PRF_DOMAIN_INDEX, _strDomainIndex);
        editor.putString(EXO_PRF_USERNAME, _strUserName);
        editor.putString(EXO_PRF_PASSWORD, _strPassword);
        editor.commit();

        createAuthorization(url.getHost(), url.getPort());

        runOnUiThread(returnRes);
      } else if (strResult.equalsIgnoreCase("NO")) {
        runOnUiThread(returnResFaileUserNamePassword);
      } else {
        runOnUiThread(returnResFaileConnection);
      }

    } catch (Exception e) {

      // String msg = e.getMessage();
      // String str = e.toString();
      // Log.v(str, msg);
      runOnUiThread(returnResFaileConnection);
    }
  }

  // Get gadget list
  public List<eXoGadget> getGadgetsList() {
    List<eXoGadget> arrGadgets = new ArrayList<eXoGadget>();
    _strDomain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN,
                                                          "exo_prf_domain");
    String strHomeUrl = _strDomain + "/portal/private/classic";
    String strContent = AppController._eXoConnection.sendRequestAndReturnString(strHomeUrl);

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
      bmp = BitmapFactory.decodeStream(AppController._eXoConnection.sendRequest(bmpUrl));

      eXoGadget tempGadget = new eXoGadget(title, description, url, bmp, null);
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
  public List<eXoGadget> listOfGadgetsWithURL(String url) {
    List<eXoGadget> arrTmpGadgets = new ArrayList<eXoGadget>();

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
      strContent = AppController._eXoConnection.sendRequestToGetGadget(url, userName, password);
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
        imgGadgetIcon = BitmapFactory.decodeStream(AppController._eXoConnection.sendRequest(gadgetIconUrl));
        if (imgGadgetIcon == null) {
          try {
            imgGadgetIcon = BitmapFactory.decodeStream(getAssets().open("portletsicon.png"));
          } catch (Exception e2) {

            imgGadgetIcon = null;
          }
        }

      } catch (Exception e) {

        try {
          imgGadgetIcon = BitmapFactory.decodeStream(getAssets().open("portletsicon.png"));
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

      eXoGadget gadget = new eXoGadget(strGadgetName,
                                       strGadgetDescription,
                                       gadgetUrl,
                                       imgGadgetIcon,
                                       gadgetID);

      arrTmpGadgets.add(gadget);

      strContent = strContent.substring(index2 + 35);
      index1 = strContent.indexOf("eXo.gadget.UIGadget.createGadget");

    } while (index1 > 0);

    return arrTmpGadgets;
  }

  // Get gadget tab list
  public List<GateInDbItem> listOfGadgets() {
    _strDomain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN,
                                                          "exo_prf_domain");
    // List<GateInDbItem> arrTmpGadgets = new ArrayList<GateInDbItem>();

    eXoApplicationsController.arrGadgets = new ArrayList<GateInDbItem>();

    String strContent = AppController._eXoConnection.getFirstLoginContent();

    int index1;
    int index2;
    int index3;

    index1 = strContent.indexOf("DashboardIcon TBIcon");

    if (index1 < 0)
      return null;

    strContent = strContent.substring(index1 + 20);
    index1 = strContent.indexOf("TBIcon");

    if (index1 < 0)
      return null;

    strContent = strContent.substring(0, index1);

    do {
      index1 = strContent.indexOf("ItemIcon DefaultPageIcon\" href=\"");
      index2 = strContent.indexOf("\" >");
      if (index1 < 0 && index2 < 0)
        return null;
      String gadgetTabUrlStr = strContent.substring(index1 + 32, index2);

      strContent = strContent.substring(index2 + 3);
      index3 = strContent.indexOf("</a>");
      if (index3 < 0)
        return null;
      String gadgetTabName = strContent.substring(0, index3);
      List<eXoGadget> arrTmpGadgetsInItem = listOfGadgetsWithURL(_strDomain + gadgetTabUrlStr);

      HashMap<String, String> mapOfURLs = listOfStandaloneGadgetsURL();

      if (arrTmpGadgetsInItem != null) {
        for (int i = 0; i < arrTmpGadgetsInItem.size(); i++) {
          eXoGadget tmpGadget = arrTmpGadgetsInItem.get(i);

          String urlStandalone = mapOfURLs.get(tmpGadget._strGadgetID);

          if (urlStandalone != null) {
            tmpGadget._strGadgetUrl = urlStandalone;
          }
        }

        GateInDbItem tmpGateInDbItem = new GateInDbItem(gadgetTabName,
                                                        gadgetTabUrlStr,
                                                        arrTmpGadgetsInItem);
        // arrTmpGadgets.add(tmpGateInDbItem);
        eXoApplicationsController.arrGadgets.add(tmpGateInDbItem);

        strContent = strContent.substring(index3);
        index1 = strContent.indexOf("ItemIcon DefaultPageIcon\" href=\"");
      }

    } while (index1 > 0);

    return null;
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
    eXoApplicationsController.webViewMode = 2;
    Intent next = new Intent(appControllerInstance, eXoWebViewController.class);
    startActivity(next);
  }

  // Generate authntication
  private void createAuthorization(String url, int port) {
    auth = new AuthScope(url, port);
    String userName = sharedPreference.getString(EXO_PRF_USERNAME, "");
    String password = sharedPreference.getString(EXO_PRF_PASSWORD, "");
    credential = new UsernamePasswordCredentials(userName, password);
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

    } catch (Exception e) {

    }

    _btnLogIn.setText(strSignIn);
    _txtViewUserName.setText(strUserName);
    _txtViewPassword.setText(strPassword);
  }

}
