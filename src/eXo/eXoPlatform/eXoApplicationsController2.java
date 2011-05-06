package eXo.eXoPlatform;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class eXoApplicationsController2 extends Activity implements OnTouchListener {

  // App item object
  class AppItem {
    Bitmap  _icon; // feature's icon
    int _badgeCount; //  number of notification
    String  _name; // feature's name
    
    public AppItem()
    {
      
    }
    
    public AppItem(Bitmap bm, String name)
    {
      _icon = bm;
      _name = name;
    }
  }
  
  
  static eXoApplicationsController2 eXoApplicationsController2Instance;

  Button            btnHome;
  Button            btnAdd;
  Button            btnDone;
  
  Timer             timer;
  Handler           handler;
  
  GridView gridview;
  View myView;
  TranslateAnimation anim;
  int timerCounter = 0;
  int itemMoveIndex = -1;
  boolean isDeleteItem = false;

  private BaseAdapter adapter;
  
  String strChatServer;
  
  // Standalone gadget content
  private String                            _strContentForStandaloneURL;
  ArrayList<AppItem> array = new ArrayList<AppItem>();
  

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.appsview2);
    
    eXoApplicationsController2Instance = this;
    
    btnHome = (Button)findViewById(R.id.Button_SignOut);
    btnHome.setOnClickListener(new View.OnClickListener() {
      
      public void onClick(View v) {

        finish(); 
      }
    });

    btnAdd = (Button)findViewById(R.id.Button_Add);
    btnAdd.setOnClickListener(new View.OnClickListener() {
      
      public void onClick(View v) {
        
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.onlinechat);
        AppItem item = new AppItem(bm, "New");
        array.add(item);
        
        adapter.notifyDataSetChanged();
//        createAdapter();
      }
      
    });
    
    btnDone = (Button)findViewById(R.id.Button_Done);
    btnDone.setVisibility(View.INVISIBLE);
    btnDone.setOnClickListener(new View.OnClickListener() {
      
      public void onClick(View v) {
        
        btnDone.setVisibility(View.INVISIBLE);
        
        for(int i = 0; i < array.size(); i++)
        {
          
          View view = adapter.getView(i, null, gridview);
          view.clearAnimation();
          ImageView ivIcon = (ImageView)view.findViewById(R.id.icon_image);
          ivIcon.setOnClickListener(null);
          
          isDeleteItem = false;
          timerCounter = 0;
        }
        adapter.notifyDataSetChanged();
      }
      
    });
    
    gridview = (GridView)findViewById(R.id.gridView1);   
    
    Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.files_app_icn);
    AppItem fileApp = new AppItem(bm, "Files");
    array.add(fileApp);
    
    bm = BitmapFactory.decodeResource(getResources(), R.drawable.onlinechat);
    AppItem chatApp = new AppItem(bm, "Chats");
    array.add(chatApp);
    
    bm = BitmapFactory.decodeResource(getResources(), R.drawable.dashboard);
    AppItem dashBoardApp = new AppItem(bm, "Dashboard");
    array.add(dashBoardApp);
    
    createAdapter();
    
  }

//  Create GridView Apdapter
  private void createAdapter()
  {
    adapter = new BaseAdapter()
    {
        public View getView(int position, View convertView, ViewGroup parent) {

          View v;
          final int pos = position;
          
          AppItem item = array.get(position);
          LayoutInflater li = getLayoutInflater();
          v = li.inflate(R.layout.appitem, null);
          v.setOnTouchListener(eXoApplicationsController2Instance);
          TextView tv = (TextView)v.findViewById(R.id.icon_text);
          tv.setText(item._name);
          ImageView iv = (ImageView)v.findViewById(R.id.icon_image);
          iv.setImageBitmap(item._icon);
          
          ImageView ivDelete = (ImageView)v.findViewById(R.id.icon_delete);
          if(isDeleteItem)
          {
            ivDelete.setVisibility(View.VISIBLE);
            v.startAnimation(anim);
          }
          else
          {
            ivDelete.setVisibility(View.INVISIBLE);
            v.clearAnimation();
          }
            
          ivDelete.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
             
              array.remove(pos);
              adapter.notifyDataSetChanged();
              
            }
          });
          
          return v;
        }
        
        public long getItemId(int position) {

          return 0;
        }
        
        public Object getItem(int position) {
          return null;
        }
        
        public int getCount() {
          
          return array.size();
        }
      };
      
      gridview.setAdapter(adapter);
  }
  
  // Create Setting Menu
  public boolean onCreateOptionsMenu(Menu menu) {

    menu.add(0, 1, 0, "Reorder Menu").setIcon(R.drawable.server);
    menu.add(0, 2, 0, "Add item").setIcon(R.drawable.server);

    return true;

  }

  // Menu action
  public boolean onOptionsItemSelected(MenuItem item) {

    int selectedItemIndex = item.getItemId();
//  Reorder menu
    if (selectedItemIndex == 1) {
      
      Intent next = new Intent(eXoApplicationsController2.this, BasicItemActivity.class);
      startActivity(next);
    }
//    Add item
    else
    {
      
    }

    return false;
  }

  // Change language
  private void updateLocallize(String localize) {
    try {
      SharedPreferences.Editor editor = AppController.sharedPreference.edit();
      editor.putString(AppController.EXO_PRF_LOCALIZE, localize);
      editor.commit();

      AppController.bundle = new PropertyResourceBundle(this.getAssets().open(localize));
      changeLanguage(AppController.bundle);

     
    } catch (Exception e) {

    }

  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {
    String strLanguageTittle = "";
    String strServerTittle = "";
    String strmyOptionEnglish = "";
    String strmyOptionFrench = "";
    String strCloseModifyServerLisrButton = "";
    String strUserGuideButton = "";

    try {
      strLanguageTittle = new String(resourceBundle.getString("Language").getBytes("ISO-8859-1"),
                                     "UTF-8");
      strServerTittle = new String(resourceBundle.getString("Server").getBytes("ISO-8859-1"),
                                   "UTF-8");
      strmyOptionEnglish = new String(resourceBundle.getString("English").getBytes("ISO-8859-1"),
                                      "UTF-8");
      strmyOptionFrench = new String(resourceBundle.getString("French").getBytes("ISO-8859-1"),
                                     "UTF-8");
      strCloseModifyServerLisrButton = new String(resourceBundle.getString("ModifyServerList")
                                                                .getBytes("ISO-8859-1"), "UTF-8");
      strUserGuideButton = new String(resourceBundle.getString("UserGuide").getBytes("ISO-8859-1"),
                                      "UTF-8");
      strChatServer = new String(resourceBundle.getString("ChatServer").getBytes("ISO-8859-1"),
      "UTF-8");
      
      
      
    } catch (Exception e) {

    }
  }

  public boolean onTouch(View v, MotionEvent event) {

    myView = v;
    int eventaction = event.getAction();
      if(timer == null)
        timer = new Timer();
      if(handler == null)
        handler = new Handler();
      
      switch (eventaction) {
          case MotionEvent.ACTION_DOWN: 
          {
              // finger touches the screen
            TimerTask timeTask = new TimerTask() {
              @Override
              public void run() {
                
                handler.post(mUpdateTimeTask);
              }
            };
            
            timer.schedule(timeTask, 0, 1000);
              break;
          }
          case MotionEvent.ACTION_MOVE:
          {
              // finger moves on the screen
            timer.cancel();
            handler.removeCallbacks(mUpdateTimeTask);
              break;
          }
          case MotionEvent.ACTION_UP:
          {
              // finger leaves the screen
            timer.cancel();
            handler.removeCallbacks(mUpdateTimeTask);
            
            if(timerCounter <= 3)
            {
              for(int i = 0; i < array.size(); i++)
              {
                  View view = gridview.getChildAt(i);
                  if(view == v)
                  {
                    AppItem item = array.get(i);
                    if(item._name.equalsIgnoreCase("files"))
                    {
                      launchFilesApp();
                    }
                    else if(item._name.equalsIgnoreCase("dashboard"))
                    {
                      launchDashboardApp();
                    }
                    else
                    {
                      launchMessengerApp(); 
                    }
                  }
              }
              
            }
            
            timer = null;
            handler = null;
              break;
          }
      }
      
      return true; 
    }

  Runnable mUpdateTimeTask = new Runnable() {
    
    public void run() {

      timerCounter++;
      Log.i("hehe", "" + timerCounter);
      if(timerCounter > 3)
      {
        timer.cancel();
        handler.removeCallbacks(mUpdateTimeTask);
        
        timer = null;
        handler = null;
        
       btnDone.setVisibility(View.VISIBLE);
       // Start animating the image
      for(int i = 0; i < array.size(); i++)
      {
        final int pos = i;
        View view = gridview.getChildAt(i);
        view.setOnTouchListener(null);
        final ImageView ivIcon = (ImageView)view.findViewById(R.id.icon_image);
        ivIcon.setOnClickListener(new View.OnClickListener() {
          
          public void onClick(View v) {
            
            if(itemMoveIndex == -1)
            {
              itemMoveIndex = pos;
              ivIcon.setBackgroundResource(R.drawable.imageborder); 
            }
            else
            {
              for(int j = 0; j < array.size(); j++)
              {
                View view2 = gridview.getChildAt(j);
                view2.findViewById(R.id.icon_image).setBackgroundDrawable(null);
                  
              }
              
              AppItem item = array.get(pos);
              array.set(pos, array.get(itemMoveIndex));
              array.set(itemMoveIndex, item);
              
              itemMoveIndex = -1;
              
              adapter.notifyDataSetChanged();
              
            }
            
          }
        });
        
        ImageView iv = (ImageView)view.findViewById(R.id.icon_delete);
        iv.setVisibility(View.VISIBLE);
        isDeleteItem = true;
        
        if(anim == null)
        {
          anim = new TranslateAnimation(-1f, 0f, 1f, 0f);
//          anim.setInterpolator(new BounceInterpolator());
//          AccelerateDecelerateInterpolator, AccelerateInterpolator, AnticipateInterpolator, AnticipateOvershootInterpolator, BounceInterpolator, CycleInterpolator, DecelerateInterpolator, LinearInterpolator, OvershootInterpolator 
          anim.setRepeatCount(Animation.INFINITE);
          anim.setRepeatMode(Animation.REVERSE);
          anim.setDuration(100); 
        }
        
        view.startAnimation(anim);
      }
       
      } 
    
    }
  
  };

  public void launchFilesApp() {
    String userName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME,
                                                               "exo_prf_username");
    String domain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN,
                                                             "exo_prf_domain");

    if (eXoFilesController.myFile == null) {
      eXoFilesController.myFile = new eXoFile();
      eXoFilesController.myFile.fileName = userName;
      eXoFilesController.myFile.urlStr = domain
          + "/rest/private/jcr/repository/collaboration/Users/" + userName;
      eXoFilesController._rootUrl = eXoFilesController.myFile.urlStr;
    }

    eXoFilesController.arrFiles = eXoFilesController.getPersonalDriveContent(eXoFilesController.myFile.urlStr);
    eXoFilesController._delegate = eXoApplicationsController2Instance;
    Intent next = new Intent(eXoApplicationsController2.this, eXoFilesController.class);
    startActivity(next);

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
    if (eXoChatListController.conn == null || !eXoChatListController.conn.isConnected())
      return;

    eXoChatListController._delegate = eXoApplicationsController2Instance;
    Intent next = new Intent(eXoApplicationsController2.this, eXoChatListController.class);
    startActivity(next);
  }

  public void launchDashboardApp() 
  {
    
  }
  
//Connect to Openfile server
  private void connectToChatServer(String host, int port, String userName, String password) {
    if (eXoChatListController.conn != null && eXoChatListController.conn.isConnected())
      return;

    ConnectionConfiguration config = new ConnectionConfiguration(host, port, "Work");
    eXoChatListController.conn = new XMPPConnection(config);

    try {
      eXoChatListController.conn.connect();
      eXoChatListController.conn.login(userName, password);

//      runOnUiThread(new Runnable() {
//
//        public void run() {
//
//          // icon.setBackgroundResource(R.drawable.onlineicon);
//          List<eXoApp> exoapps = new ArrayList<eXoApp>(2);
//          exoapps.add(new eXoApp(fileTittle, ""));
//          exoapps.add(new eXoApp(chatTittle, ""));
//          exoAppsAdapter = new eXoAppsAdapter(exoapps);
//          _lstvApps.setAdapter(exoAppsAdapter);
//          _lstvApps.setOnItemClickListener(exoAppsAdapter);
//
//        }
//      });

      // exoAppsAdapter.notifyDataSetChanged();

    } catch (XMPPException e) {

      String str = e.toString();
      String msg = e.getMessage();
      Log.e(str, msg);
      // e.printStackTrace();
      Toast.makeText(eXoApplicationsController2.this, strChatServer, Toast.LENGTH_SHORT).show();
    }

  }

  // Get gadget list
  public List<eXoGadget> getGadgetsList() {
    List<eXoGadget> arrGadgets = new ArrayList<eXoGadget>();
    String _strDomain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN,
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
    String _strDomain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN,
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

  
}
