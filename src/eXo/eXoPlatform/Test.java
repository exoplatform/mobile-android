/*
 * Copyright (C) 2010 Cyril Mottier (http://www.cyrilmottier.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eXo.eXoPlatform;

import java.util.ArrayList;
import java.util.List;

import eXo.eXoPlatform.eXoApplicationsController2.AppItem;

import greendroid.app.GDActivity;
import greendroid.app.GDListActivity;
import greendroid.graphics.drawable.ActionBarDrawable;
import greendroid.widget.ActionBarItem;
import greendroid.widget.LoaderActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.item.TextItem;
import greendroid.widget.NormalActionBarItem;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Test extends GDActivity implements OnClickListener {

//App item object
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
  
  public List<GateInDbItem> arrGadgets;
  GridView gridview;
  Button btnDone;
  
  private BaseAdapter adapter;
  ArrayList<AppItem> array = new ArrayList<AppItem>();
  
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      
      
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setActionBarContentView(R.layout.appsview2);
        
        this.setTitle("312313");
        
//        addActionBarItem(getActionBar()
//                         .newActionBarItem(NormalActionBarItem.class)
//                         .setDrawable(new ActionBarDrawable(getResources(), R.drawable.ic_title_export)), R.id.action_bar_view_info);
        addActionBarItem(Type.Add, R.drawable.gd_action_bar_add);
        addActionBarItem(Type.SignOut, R.drawable.gd_action_bar_signout);
        
        
//        addActionBarItem(getActionBar()
//                .newActionBarItem(NormalActionBarItem.class)
//                .setDrawable(R.drawable.ic_title_export)
//                .setContentDescription(R.string.gd_export), R.id.action_bar_export);
//        addActionBarItem(Type.Locate, R.id.action_bar_locate);
        
        gridview = (GridView)findViewById(R.id.gridView1); 
        
        btnDone = (Button)findViewById(R.id.Button_Done);
        btnDone.setOnClickListener(this);
        
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

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

        switch (item.getItemId()) {
            case R.id.action_bar_locate:
//                startActivity(new Intent(this, TabbedActionBarActivity.class));
                break;

            case R.id.action_bar_refresh:
                final LoaderActionBarItem loaderItem = (LoaderActionBarItem) item;
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        loaderItem.setLoading(false);
                    }
                }, 2000);
                Toast.makeText(this, R.string.refresh_pressed, Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_bar_export:
                Toast.makeText(this, R.string.custom_drawable, Toast.LENGTH_SHORT).show();
                break;

            default:
                return super.onHandleActionBarItemClick(item, position);
        }

        return true;
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
//            v.setOnTouchListener(eXoApplicationsController2Instance);
            TextView tv = (TextView)v.findViewById(R.id.icon_text);
            tv.setText(item._name);
            ImageView iv = (ImageView)v.findViewById(R.id.icon_image);
            iv.setImageBitmap(item._icon);
            
            ImageView ivDelete = (ImageView)v.findViewById(R.id.icon_delete);
            if(true)
            {
//              ivDelete.setVisibility(View.VISIBLE);
              ivDelete.setVisibility(View.INVISIBLE);
//              v.startAnimation(anim);
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

public void onClick(View v) {
  // TODO Auto-generated method stub
  if(v == btnDone)
  {
    
//    final TextItem textItem = (TextItem) l.getAdapter().getItem(position);
    GDActivity.TYPE = 1;
    
    Intent intent = new Intent(Test.this, BasicItemActivity.class);
    intent.putExtra("123", "23123");
    startActivity(intent);
    
  }
  
}
    
}
