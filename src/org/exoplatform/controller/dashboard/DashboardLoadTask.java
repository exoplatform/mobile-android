package org.exoplatform.controller.dashboard;

import greendroid.util.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.exoplatform.model.DashboardItem;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ConnTimeOutDialog;
import org.exoplatform.widget.DashboardWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.cyrilmottier.android.greendroid.R;

public class DashboardLoadTask extends AsyncTask<Void, Void, Integer> {
  private static final int         RESULT_OK      = 1;

  private static final int         RESULT_ERROR   = 0;

  private static final int         RESULT_TIMEOUT = -1;

  private DashboardController      dashboardController;

  private DashboardActivity        dashboardActivity;

  private String                   loadingData;

  private String                   okString;

  private String                   titleString;

  private String                   contentString;

  private DashboardWaitingDialog   _progressDialog;

  private ArrayList<DashboardItem> dashboarList;

  public DashboardLoadTask(DashboardActivity context, DashboardWaitingDialog dialog) {
    dashboardActivity = context;
    dashboardController = new DashboardController();
    _progressDialog = dialog;
    changeLanguage();
  }

  @Override
  public void onPreExecute() {
    _progressDialog = new DashboardWaitingDialog(dashboardActivity, null, loadingData);
    _progressDialog.show();
  }

  @Override
  public Integer doInBackground(Void... params) {
    try {
      HttpResponse response;
      String urlForDahboards = AccountSetting.getInstance().getDomainName()
          + ExoConstants.DASHBOARD_PATH;
      /*
       * Checking the session status each time we retrieve dashboard item list.
       * If time out, re logging in
       */
      if (ExoConnectionUtils.getResponseCode(urlForDahboards) != 1) {
        if (!ExoConnectionUtils.onReLogin())
          return RESULT_TIMEOUT;
      }

      response = ExoConnectionUtils.getRequestResponse(urlForDahboards);
      dashboarList = dashboardController.getDashboards(response);
      return RESULT_OK;
    } catch (IOException e) {
      return RESULT_ERROR;
    }

  }

  @Override
  public void onPostExecute(Integer result) {
    if (result == RESULT_OK) {

      if (dashboarList.size() == 0) {
        dashboardActivity.setEmptyView(View.VISIBLE);
      } else {
        ArrayList<GadgetInfo> items = new ArrayList<GadgetInfo>();
        for (int i = 0; i < dashboarList.size(); i++) {
          DashboardItem gadgetTab = dashboarList.get(i);
          try {
            HttpResponse response = ExoConnectionUtils.getRequestResponse(gadgetTab.link);
            List<GadgetInfo> gadgets = dashboardController.getGadgetInTab(response,
                                                                          gadgetTab.label,
                                                                          gadgetTab.link);
            if (gadgets != null && gadgets.size() > 0) {
              items.add(new GadgetInfo(gadgetTab.label));
              items.addAll(gadgets);
            }
          } catch (IOException e) {
            if (Config.GD_ERROR_LOGS_ENABLED)
              Log.e("DashboardLoadTask", e.getMessage());
          }

        }
        if (items.size() > 0) {
          dashboardActivity.setAdapter(items);
          dashboardActivity.setEmptyView(View.GONE);
        } else
          dashboardActivity.setEmptyView(View.VISIBLE);
      }

    } else if (result == RESULT_ERROR) {
      dashboardActivity.setEmptyView(View.VISIBLE);
      WarningDialog dialog = new WarningDialog(dashboardActivity,
                                               titleString,
                                               contentString,
                                               okString);
      dialog.show();
    } else if (result == RESULT_TIMEOUT) {
      new ConnTimeOutDialog(dashboardActivity, titleString, okString).show();
    }
    _progressDialog.dismiss();

    String strGadgetsErrorList = dashboardController.getGadgetsErrorList();
    if (strGadgetsErrorList.length() > 0) {
      StringBuffer titleBuffer = new StringBuffer("Apps: ");
      titleBuffer.append(strGadgetsErrorList);
      titleBuffer.append(" ");
      titleBuffer.append(contentString);

      WarningDialog dialog = new WarningDialog(dashboardActivity,
                                               titleString,
                                               titleBuffer.toString(),
                                               okString);
      dialog.show();
    }

  }

  private void changeLanguage() {
    Resources resource = dashboardActivity.getResources();
    loadingData = resource.getString(R.string.LoadingData);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    contentString = resource.getString(R.string.GadgetsCannotBeRetrieved);
  }

}
