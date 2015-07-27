/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ui;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.dashboard.DashboardItemAdapter;
import org.exoplatform.controller.dashboard.DashboardLoadTask;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.MyActionBar;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.LoaderActionBarItem;

public class DashboardActivity extends MyActionBar {
  private static final String     ACCOUNT_SETTING = "account_setting";

  private ListView                listView;

  private View                    empty_stub;

  private String                  title;

  private String                  dashboardEmptyString;

  public static DashboardActivity dashboardActivity;

  private DashboardLoadTask       mLoadTask;

  private LoaderActionBarItem     loaderItem;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.dashboard_layout);
    changeLanguage();
    dashboardActivity = this;

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem(Type.Refresh);
    getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);

    listView = (ListView) findViewById(R.id.dashboard_listview);
    listView.setCacheColorHint(Color.TRANSPARENT);
    listView.setFadingEdgeLength(0);
    listView.setScrollbarFadingEnabled(true);
    listView.setDivider(null);
    // listView.setDividerHeight(-1);
    if (bundle != null) {
      AccountSetting accountSetting = bundle.getParcelable(ACCOUNT_SETTING);
      AccountSetting.getInstance().setInstance(accountSetting);
      ArrayList<String> cookieList = AccountSetting.getInstance().cookiesList;
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
    }
    loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);

    onLoad(loaderItem);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
  }

  public void onLoad(LoaderActionBarItem loader) {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      if (mLoadTask == null || mLoadTask.getStatus() == DashboardLoadTask.Status.FINISHED) {
        mLoadTask = (DashboardLoadTask) new DashboardLoadTask(this, loader).execute();
      }
    } else {
      new ConnectionErrorDialog(this).show();
    }
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == DashboardLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  public void setAdapter(ArrayList<GadgetInfo> result) {
    listView.setAdapter(new DashboardItemAdapter(this, result));
  }

  public void setEmptyView(int status) {
    if (empty_stub == null) {
      initStubView();
    }
    empty_stub.setVisibility(status);

  }

  private void initStubView() {
    empty_stub = ((ViewStub) findViewById(R.id.dashboard_empty_stub)).inflate();
    ImageView emptyImage = (ImageView) empty_stub.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_no_gadgets);
    TextView emptyStatus = (TextView) empty_stub.findViewById(R.id.empty_status);
    emptyStatus.setText(dashboardEmptyString);
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      finish();
      break;
    case 0:
      loaderItem = (LoaderActionBarItem) item;
      onLoad(loaderItem);

      break;

    default:

    }
    return true;

  }

  @Override
  protected void onPause() {
    onCancelLoad();
    super.onPause();
  }

  private void changeLanguage() {
    Resources resource = getResources();
    title = resource.getString(R.string.Dashboard);
    setTitle(title);
    dashboardEmptyString = resource.getString(R.string.EmptyDashboard);
  }

}
