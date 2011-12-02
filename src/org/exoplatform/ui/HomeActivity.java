package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import org.exoplatform.R;
import org.exoplatform.controller.home.HomeActionListenner;
import org.exoplatform.controller.home.HomeAdapter;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.GridView;

public class HomeActivity extends MyActionBar {
  private GridView            gridView;

  private HomeController      homeController;

  private HomeActionListenner homeActionListenner;

  private String              settingText;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.home_layout);
    super.getActionBar().setType(greendroid.widget.ActionBar.Type.Dashboard);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.action_bar_logout_button);
    // addActionBarItem(R.drawable.action_bar_logout_button);
    // super.setTitle("eXo");
    init();
    changeLanguage();
  }

  @Override
  protected void onResume() {
    super.onResume();
    init();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    menu.add(0, 1, 0, settingText).setIcon(R.drawable.optionsettingsbutton);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    int selectedItemIndex = item.getItemId();

    if (selectedItemIndex == 1) {
      Intent next = new Intent(HomeActivity.this, SettingActivity.class);
      next.putExtra(ExoConstants.SETTING_TYPE, 0);
      startActivity(next);
    }
    return false;
  }

  @Override
  public void onBackPressed() {
    if (homeActionListenner != null) {
      homeActionListenner.onCancelLoadNewsService();
    }
    homeController.onFinish();
    // new LogoutDialog(HomeActivity.this, homeController).show();
  }

  private void init() {
    homeController = new HomeController(this);
    homeController.initScreen();
    createAdapter();
  }

  private void createAdapter() {
    gridView = (GridView) findViewById(R.id.gridView1);
    gridView.setAdapter(new HomeAdapter(this));
    homeActionListenner = new HomeActionListenner(this);
    gridView.setOnItemClickListener(homeActionListenner);
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      break;
    case 0:
      // new LogoutDialog(HomeActivity.this, homeController).show();
      if (homeActionListenner != null) {
        homeActionListenner.onCancelLoadNewsService();
      }
      homeController.onFinish();
      break;
    case 1:
      break;
    }
    return true;

  }

  public void changeLanguage() {
    LocalizationHelper local = LocalizationHelper.getInstance();
    settingText = local.getString("Settings");
  }

}
