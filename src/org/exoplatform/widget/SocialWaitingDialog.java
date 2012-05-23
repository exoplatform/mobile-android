package org.exoplatform.widget;

import org.exoplatform.controller.home.HomeController;

import android.content.Context;

public class SocialWaitingDialog extends WaitingDialog {
  private HomeController homeController;

  public SocialWaitingDialog(Context context,
                             HomeController controller,
                             String titleString,
                             String contentString) {
    super(context, titleString, contentString);
    homeController = controller;
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    homeController.finishService();
  }

}
