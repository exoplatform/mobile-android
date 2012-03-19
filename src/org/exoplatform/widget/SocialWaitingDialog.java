package org.exoplatform.widget;

import org.exoplatform.controller.social.SocialController;

import android.content.Context;

public class SocialWaitingDialog extends WaitingDialog {
  private SocialController socialController;

  public SocialWaitingDialog(Context context,
                             SocialController controller,
                             String titleString,
                             String contentString) {
    super(context, titleString, contentString);
    socialController = controller;
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    socialController.onCancelLoad();
  }

}
