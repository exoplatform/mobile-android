package org.exoplatform.setting;

import greendroid.widget.ActionBarItem;

import java.util.List;

import org.exoplatform.controller.AppController;
import org.exoplatform.proxy.ServerObj;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;


public class ExoModifyServerList extends MyActionBar {

  public static ExoModifyServerList eXoModifyServerListInstance;

  ListView                          listViewServer;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.exomodifyserverlist);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.gd_action_bar_add);
    String serverListText = AppController.bundle.getString("ServerList");
    setTitle(serverListText);

    eXoModifyServerListInstance = this;

    listViewServer = (ListView) findViewById(R.id.ListView_Server_List);
    listViewServer.setDivider(null);
    listViewServer.setDividerHeight(0);

    createServersAdapter(AppController.configurationInstance._arrServerList);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Intent intent = new Intent(this, ExoSetting.class);
    startActivity(intent);
    finish();
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

    switch (position) {

    case -1:
      finish();
      break;

    case 0:
      AddEditServerDialog.isNewServer = true;
      AddEditServerDialog customizeDialog = new AddEditServerDialog(ExoModifyServerList.this);
      customizeDialog.show();
      break;

    default:
      return super.onHandleActionBarItemClick(item, position);
    }

    return true;
  }

  // Create Setting Menu
  public boolean onCreateOptionsMenu(Menu menu) {
    String addServer = AppController.bundle.getString("AddAServer");
    menu.add(0, 1, 0, addServer);
    return true;
  }

  // Menu action, add new server
  public boolean onOptionsItemSelected(MenuItem item) {
    int selectedItemIndex = item.getItemId();

    if (selectedItemIndex == 1) {
      AddEditServerDialog.isNewServer = true;
      AddEditServerDialog customizeDialog = new AddEditServerDialog(ExoModifyServerList.this);
      customizeDialog.show();

    }

    return false;
  }

  // Create server list adapter
  public void createServersAdapter(List<ServerObj> serverObjs) {
    final List<ServerObj> serverObjsTmp = serverObjs;

    BaseAdapter serverAdapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;

        LayoutInflater inflater = eXoModifyServerListInstance.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.serverlistitemforsetting, parent, false);

        ServerObj serverObj = serverObjsTmp.get(position);

        TextView serverName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
        serverName.setText(serverObj._strServerName);

        TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
        txtvUrl.setText(serverObj._strServerUrl);

        ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        if (AppController._intDomainIndex == position)
          imgView.setVisibility(View.VISIBLE);
        else
          imgView.setVisibility(View.INVISIBLE);

        rowView.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            AddEditServerDialog.isNewServer = false;
            AddEditServerDialog.selectedServerIndex = pos;
            AddEditServerDialog customizeDialog = new AddEditServerDialog(ExoModifyServerList.this);
            customizeDialog.show();
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

    listViewServer.setAdapter(serverAdapter);
    // _lstvFiles.setOnItemClickListener(test);
  }

}
