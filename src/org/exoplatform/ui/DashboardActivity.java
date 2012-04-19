package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.dashboard.DashboardItemAdapter;
import org.exoplatform.controller.dashboard.DashboardLoadTask;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.DashboardWaitingDialog;
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

public class DashboardActivity extends MyActionBar {
  private static final String     ACCOUNT_SETTING = "account_setting";

  private static final String     COOKIESTORE     = "cookie_store";

  private DashboardWaitingDialog  _progressDialog;

  private ListView                listView;

  private View                    empty_stub;

  private String                  title;

  private String                  dashboardEmptyString;

  public static DashboardActivity dashboardActivity;

  private DashboardLoadTask       mLoadTask;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.dashboard_layout);
    changeLanguage();
    dashboardActivity = this;

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem();
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
      ArrayList<String> cookieList = bundle.getStringArrayList(COOKIESTORE);
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
    }

    onLoad();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
    outState.putStringArrayList(COOKIESTORE,
                                ExoConnectionUtils.getCookieList(ExoConnectionUtils.cookiesStore));
  }

  public void onLoad() {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      if (mLoadTask == null || mLoadTask.getStatus() == DashboardLoadTask.Status.FINISHED) {
        mLoadTask = (DashboardLoadTask) new DashboardLoadTask(this, _progressDialog).execute();
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
    case 0: {
      onLoad();
    }
      break;

    default:

    }
    return true;

  }

  @Override
  public void finish() {
    if (_progressDialog != null) {
      _progressDialog.dismiss();
    }
    super.finish();
  }

  @Override
  public void onBackPressed() {
    onCancelLoad();
    finish();
  }

  private void changeLanguage() {
    Resources resource = getResources();
    title = resource.getString(R.string.Dashboard);
    setTitle(title);
    dashboardEmptyString = resource.getString(R.string.EmptyDashboard);
  }

}
