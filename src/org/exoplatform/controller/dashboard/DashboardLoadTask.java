package org.exoplatform.controller.dashboard;

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
import org.exoplatform.widget.DashboardWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.View;

import com.cyrilmottier.android.greendroid.R;

public class DashboardLoadTask extends AsyncTask<Void, Void, ArrayList<DashboardItem>> {
  private DashboardController    dashboardController;

  private DashboardActivity      dashboardActivity;

  private String                 loadingData;

  private String                 okString;

  private String                 titleString;

  private String                 contentString;

  private DashboardWaitingDialog _progressDialog;

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
  public ArrayList<DashboardItem> doInBackground(Void... params) {
    try {
      HttpResponse response;
      String urlForDahboards = AccountSetting.getInstance().getDomainName()
          + ExoConstants.DASHBOARD_PATH;

      response = ExoConnectionUtils.getRequestResponse(urlForDahboards);
      /*
       * Checking the session status each time we retrieve dashboard items. If
       * time out, re logging in
       */
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return dashboardController.getDashboards(response);
      } else {
        // Re logging in if connection session time out
        ExoConnectionUtils.onReLogin();
        return dashboardController.getDashboards(response);
      }

    } catch (IOException e) {
      return null;
    }

  }

  @Override
  public void onPostExecute(ArrayList<DashboardItem> result) {
    if (result != null) {

      if (result.size() == 0) {
        dashboardActivity.setEmptyView(View.VISIBLE);
      } else {
        ArrayList<GadgetInfo> items = new ArrayList<GadgetInfo>();
        for (int i = 0; i < result.size(); i++) {
          DashboardItem gadgetTab = result.get(i);
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
            e.getMessage();
          }

        }
        if (items.size() > 0) {
          dashboardActivity.setAdapter(items);
          dashboardActivity.setEmptyView(View.GONE);
        } else
          dashboardActivity.setEmptyView(View.VISIBLE);
      }

    } else {
      dashboardActivity.setEmptyView(View.VISIBLE);
      WarningDialog dialog = new WarningDialog(dashboardActivity,
                                               titleString,
                                               contentString,
                                               okString);
      dialog.show();
    }
    _progressDialog.dismiss();

    String strGadgetsErrorList = dashboardController.getGadgetsErrorList();
    if (strGadgetsErrorList.length() > 0) {
      WarningDialog dialog = new WarningDialog(dashboardActivity, titleString, "Apps: "
          + strGadgetsErrorList + " " + contentString, okString);
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
