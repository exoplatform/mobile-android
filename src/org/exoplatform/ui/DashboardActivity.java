package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import org.exoplatform.controller.dashboard.DashboardController;
import org.exoplatform.widget.MyListActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

public class DashboardActivity extends MyListActivity {

  private DashboardController controller;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setTitle("Dashboard");

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    ListView listView = getListView();
    listView.setBackgroundColor(Color.rgb(8, 56, 123));
    listView.setCacheColorHint(Color.TRANSPARENT);
    listView.setFadingEdgeLength(0);
    
    controller = new DashboardController(this);
    controller.onLoad();
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      finish();
      break;
    case 0:

      break;

    default:

    }
    return true;

  }

  @Override
  public void onBackPressed() {
    controller.onCancelLoad();
    finish();
  }

}
