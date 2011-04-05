package eXo.eXoPlatform;

import java.util.List;

import eXo.eXoPlatform.AppController.ServerObj;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class eXoModifyServerList extends Activity {

  static eXoModifyServerList eXoModifyServerListInstance;

  Button                     btnHome;

  ListView                   listViewServer;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.exomodifyserverlist);

    eXoModifyServerListInstance = this;

    btnHome = (Button) findViewById(R.id.Button_Home);
    btnHome.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        finish();
      }
    });

    listViewServer = (ListView) findViewById(R.id.ListView_Server_List);
    createServersAdapter(AppController.configurationInstance._arrServerList);
  }

  // Create Setting Menu
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, 1, 0, "Add a Server");
    return true;
  }

  // Menu action, add new server
  public boolean onOptionsItemSelected(MenuItem item) {
    int selectedItemIndex = item.getItemId();

    if (selectedItemIndex == 1) {
      eXoLanguageSettingDialog.isNewServer = true;
      eXoLanguageSettingDialog customizeDialog = new eXoLanguageSettingDialog(eXoModifyServerList.this);
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
        View rowView = inflater.inflate(R.layout.serverlistitem, parent, false);

        ServerObj serverObj = serverObjsTmp.get(position);

        TextView serverName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
        serverName.setText(serverObj._strServerName);

        TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
        txtvUrl.setText(serverObj._strServerUrl);

        LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) txtvUrl.getLayoutParams();
        layout.width = 180;
        txtvUrl.setLayoutParams(layout);

        rowView.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            eXoLanguageSettingDialog.isNewServer = false;
            eXoLanguageSettingDialog.selectedServerIndex = pos;
            eXoLanguageSettingDialog customizeDialog = new eXoLanguageSettingDialog(eXoModifyServerList.this);
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
