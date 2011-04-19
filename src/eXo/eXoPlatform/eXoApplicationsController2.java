package eXo.eXoPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import eXo.eXoPlatform.AppController.ServerObj;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableLayout.LayoutParams;

public class eXoApplicationsController2 extends Activity {

  static eXoApplicationsController2 eXoApplicationsController2Instance;

  Button            btnHome;
  Button            btnAdd;
  
  TableLayout table;
  int counter = 0;

  ArrayList<RelativeLayout> array = new ArrayList<RelativeLayout>();
  

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.appsview2);
    
    eXoApplicationsController2Instance = this;
    
    table = (TableLayout) findViewById(R.id.TableLayout01);
    
    for(int i = 0; i < 3; i++)
    {
      RelativeLayout t = new RelativeLayout(eXoApplicationsController2Instance);
//      t.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
      
      Button btn = new Button(eXoApplicationsController2Instance);
//      btn.setBackgroundResource(R.drawable.server);
      
      LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
      lp.setMargins(100, 50, 20, 20);
      btn.setLayoutParams(lp);
      btn.invalidate();
      
      t.addView(btn);
      
      
        array.add(t);
    }
    
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
        // TODO Auto-generated method stub

//        ImageButton t = new ImageButton(eXoApplicationsController2Instance);
//        t.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//        array.add(t);
      
        // get a reference for the TableLayout
//        table.removeAllViewsInLayout();
        
        int count = array.size();
        int numberOfRow = count/3;
        int remaining = count % 3;
        if(remaining != 0)
          numberOfRow += 1;
        
        for(int i = 0; i < numberOfRow; i++)
        {
            // create a new TableRow
          TableRow row = new TableRow(eXoApplicationsController2Instance);
          for(int j = 0; j < 3; j++)
          {
            int index = i*3 + j;
            if(index < count)
            {
              RelativeLayout btn = array.get(index);
              row.addView(btn, j);
            }
            
            
          }
          
          table.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }
        
 /*
        // count the counter up by one
        counter++;
 
        // create a new Button
        Button t = new Button(eXoApplicationsController2Instance);
        
        // set the text to "text xx"
        t.setBackgroundResource(R.drawable.server);
 
        Button t1 = new Button(eXoApplicationsController2Instance);
        // set the text to "text xx"
        t.setBackgroundResource(R.drawable.server);
        
        Button t2 = new Button(eXoApplicationsController2Instance);
        // set the text to "text xx"
        t.setBackgroundResource(R.drawable.server);
        
        // add the TextView and the CheckBox to the new TableRow
//        row.addView(t, 0);
//        row.addView(t1, 1);
//        row.addView(t2, 2);
        
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
//        layoutParams.setMargins(10, 20, 10, 10);
//
//        Button myBtn = new Button(eXoApplicationsController2Instance);
//        myBtn.setLayoutParams(layoutParams);
//        myBtn.setBackgroundResource(R.drawable.server);
//        row.addView(myBtn, 0);
        
     // add the TableRow to the TableLayout
//        table.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));        
//        table.addView(row);
 * */
 
      }
      
    });
    
  }

  // Create server list adapter
  public void createServersAdapter(List<ServerObj> serverObjs) {

    final List<ServerObj> serverObjsTmp = serverObjs;

    BaseAdapter serverAdapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = eXoApplicationsController2Instance.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.serverlistitem, parent, false);

        ServerObj serverObj = serverObjsTmp.get(position);

        TextView serverName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
        serverName.setText(serverObj._strServerName);

        TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
        txtvUrl.setText(serverObj._strServerUrl);
//        RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams) txtvUrl.getLayoutParams();
//        layout.width = RelativeLayout.LayoutParams.FILL_PARENT;
//        txtvUrl.setLayoutParams(layout);

        ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        imgView.setVisibility(View.INVISIBLE);
        
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

//    listViewServer.setAdapter(serverAdapter);
    // _lstvFiles.setOnItemClickListener(test);
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
