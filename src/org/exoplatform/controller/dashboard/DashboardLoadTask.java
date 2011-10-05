package org.exoplatform.controller.dashboard;

import java.util.ArrayList;

import org.exoplatform.model.DashBoardItem;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.model.GateInDbItem;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.view.View;

public class DashboardLoadTask extends UserTask<Void, Void, ArrayList<GateInDbItem>> {
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
  public ArrayList<GateInDbItem> doInBackground(Void... params) {
    return dashboardController.listOfGadgets();
  }

  @Override
  public void onPostExecute(ArrayList<GateInDbItem> result) {
    if (result != null) {
      if (result.size() == 0) {
        dashboardActivity.setEmptyView(View.VISIBLE);
      } else {
        ArrayList<DashBoardItem> items = new ArrayList<DashBoardItem>();
        for (int i = 0; i < result.size(); i++) {
          GateInDbItem gadgetTab = result.get(i);
          items.add(new DashBoardItem(gadgetTab._strDbItemName, null));
          for (int j = 0; j < gadgetTab._arrGadgetsInItem.size(); j++) {
            GadgetInfo gadget = gadgetTab._arrGadgetsInItem.get(j);
            items.add(new DashBoardItem(null, gadget));
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
      dashboardActivity.setEmptyView(View.VISIBLE);
    }
    _progressDialog.dismiss();
  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    loadingData = location.getString("LoadingData");
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    contentString = location.getString("ConnectionError");
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
