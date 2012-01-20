package org.exoplatform.controller.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.exoplatform.model.DashboardItem;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.utils.WebdavMethod;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.view.View;

public class DashboardLoadTask extends UserTask<Void, Void, ArrayList<DashboardItem>> {
  private DashboardController    dashboardController;

  private DashboardActivity      dashboardActivity;

  private String                 loadingData;

  private String                 okString;

  private String                 titleString;

  private String                 contentString;

  private DashboardWaitingDialog _progressDialog;

  public DashboardLoadTask(DashboardActivity context, DashboardController controller) {
    dashboardActivity = context;
    dashboardController = controller;
    changeLanguage();
  }

  @Override
  public void onPreExecute() {
    _progressDialog = new DashboardWaitingDialog(dashboardActivity, null, loadingData);
    _progressDialog.show();
  }

  @Override
  public ArrayList<DashboardItem> doInBackground(Void... params) {

    HttpResponse response;
    try {

      WebdavMethod copy = new WebdavMethod("HEAD", AccountSetting.getInstance().getDomainName());
      response = ExoConnectionUtils.httpClient.execute(copy);

      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return dashboardController.getDashboards();
      } else
        return null;

    } catch (Exception e) {
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

          List<GadgetInfo> gadgets = dashboardController.getGadgetInTab(gadgetTab.label,
                                                                        gadgetTab.link);
          if (gadgets != null && gadgets.size() > 0) {
            items.add(new GadgetInfo(gadgetTab.label));
            items.addAll(gadgets);
          }

        }

        dashboardController.setAdapter(items);
        dashboardActivity.setEmptyView(View.GONE);
      }

    } else {

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
    LocalizationHelper location = LocalizationHelper.getInstance();
    loadingData = location.getString("LoadingData");
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    contentString = location.getString("GadgetsCannotBeRetrieved");
  }

  private class DashboardWaitingDialog extends WaitingDialog {

    public DashboardWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      dashboardController.onCancelLoad();
    }
  }
}
