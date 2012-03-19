package org.exoplatform.widget;

import org.exoplatform.ui.DashboardActivity;

public class DashboardWaitingDialog extends WaitingDialog {
  private DashboardActivity activity;

  public DashboardWaitingDialog(DashboardActivity context,
                                String titleString,
                                String contentString) {
    super(context, titleString, contentString);
    activity = context;
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    activity.onCancelLoad();
  }
}
