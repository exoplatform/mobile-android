package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.home.HomeActionListenner;
import org.exoplatform.controller.home.HomeAdapter;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.GridView;

public class HomeActivity extends MyActionBar {
  private static final String SERVER_SETTING_HELPER = "SERVER_SETTING_HELPER";

  private static final String ACCOUNT_SETTING       = "account_setting";

  private static final String DOCUMENT_HELPER       = "document_helper";

  private static final String COOKIESTORE           = "cookie_store";

  private GridView            gridView;

  private HomeController      homeController;

  private HomeActionListenner homeActionListenner;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.home_layout);
    super.getActionBar().setType(greendroid.widget.ActionBar.Type.Dashboard);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.action_bar_logout_button);
    if (bundle != null) {
      DocumentHelper helper = bundle.getParcelable(DOCUMENT_HELPER);
      DocumentHelper.getInstance().setInstance(helper);
      AccountSetting accountSetting = bundle.getParcelable(ACCOUNT_SETTING);
      AccountSetting.getInstance().setInstance(accountSetting);
      ServerSettingHelper settingHelper = bundle.getParcelable(SERVER_SETTING_HELPER);
      ServerSettingHelper.getInstance().setInstance(settingHelper);
      ArrayList<String> cookieList = bundle.getStringArrayList(COOKIESTORE);
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
    }
    init();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(SERVER_SETTING_HELPER, ServerSettingHelper.getInstance());
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
    outState.putParcelable(DOCUMENT_HELPER, DocumentHelper.getInstance());
    outState.putStringArrayList(COOKIESTORE,
                                ExoConnectionUtils.getCookieList(ExoConnectionUtils.cookiesStore));

  }

  @Override
  protected void onResume() {
    super.onResume();
    init();
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    menu.add(0, 1, 0, getResources().getString(R.string.Settings))
        .setIcon(R.drawable.optionsettingsbutton);
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
      homeController.onFinish();
      break;
    case 1:
      break;
    }
    return true;

  }

}
