package eXo.eXoPlatform;

import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class eXoApplicationsController2 extends Activity {

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
  
  GridView gridview;
  
  int counter = 0;

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
        // TODO Auto-generated method stub
        finish(); 
      }
    });

    btnAdd = (Button)findViewById(R.id.Button_Add);
    btnAdd.setOnClickListener(new View.OnClickListener() {
      
      public void onClick(View v) {
        
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.onlinechat);
        AppItem item = new AppItem(bm, "New");
        array.add(item);
        
        createAdapter();
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
    BaseAdapter adapter = new BaseAdapter()
    {
        public View getView(int position, View convertView, ViewGroup parent) {
          // TODO Auto-generated method stub
          View v;
          if(convertView == null)
          {
            AppItem item = array.get(position);
            LayoutInflater li = getLayoutInflater();
            v = li.inflate(R.layout.appitem, null);
            TextView tv = (TextView)v.findViewById(R.id.icon_text);
            tv.setText(item._name);
            ImageView iv = (ImageView)v.findViewById(R.id.icon_image);
//            iv.setImageResource(R.drawable.server);
            iv.setImageBitmap(item._icon);
          }
          else
          {
            v = convertView;
          }
          return v;
        }
        
        public long getItemId(int position) {
          // TODO Auto-generated method stub
          return 0;
        }
        
        public Object getItem(int position) {
          // TODO Auto-generated method stub
          return null;
        }
        
        public int getCount() {
          // TODO Auto-generated method stub
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
    } catch (Exception e) {

    }
  }
}
