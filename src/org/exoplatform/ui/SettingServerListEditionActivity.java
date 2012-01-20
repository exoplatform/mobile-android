package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import org.exoplatform.controller.setting.ModifyServerAdapter;
import org.exoplatform.controller.setting.ModifyServerListenner;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

import com.cyrilmottier.android.greendroid.R;

public class SettingServerListEditionActivity extends MyActionBar {

  private ListView listViewServer;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.exomodifyserverlist);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.actionbaraddbutton);
    String serverListText = LocalizationHelper.getInstance().getString("ServerList");
    setTitle(serverListText);
    setServerAdapter();
  }

  @Override
  protected void onResume() {
    super.onResume();
    setServerAdapter();
  }

  private void setServerAdapter() {
    listViewServer = (ListView) findViewById(R.id.ListView_Server_List);
    listViewServer.setDivider(null);
    listViewServer.setDividerHeight(-1);
    listViewServer.setAdapter(new ModifyServerAdapter(this));
    listViewServer.setOnItemClickListener(new ModifyServerListenner(this, listViewServer));
  }

  // Create Setting Menu
  public boolean onCreateOptionsMenu(Menu menu) {
    String addServer = LocalizationHelper.getInstance().getString("AddAServer");
    menu.add(0, 1, 0, addServer);
    return true;
  }

  // Menu action, add new server
  public boolean onOptionsItemSelected(MenuItem item) {
    int selectedItemIndex = item.getItemId();

    if (selectedItemIndex == 1) {
      ServerSettingHelper.getInstance().setIsNewServer(true);
      new SettingServerEditionDialog(this, listViewServer).show();
    }

    return false;
  }

  @Override
  public void onBackPressed() {
    // super.onBackPressed();
    Intent intent = new Intent(this, SettingActivity.class);
    startActivity(intent);
    finish();
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

    switch (position) {

    case -1:
      if (SettingActivity.settingActivity != null)
        SettingActivity.settingActivity.finish();
      finish();
      break;

    case 0:
      ServerSettingHelper.getInstance().setIsNewServer(true);
      new SettingServerEditionDialog(this, listViewServer).show();
      break;

    }

    return true;
  }
}
