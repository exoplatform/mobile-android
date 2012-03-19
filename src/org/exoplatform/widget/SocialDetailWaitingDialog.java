package org.exoplatform.widget;

import org.exoplatform.controller.social.SocialDetailController;

import android.content.Context;

public class SocialDetailWaitingDialog extends WaitingDialog {
  private SocialDetailController detailController;

  public SocialDetailWaitingDialog(Context context,SocialDetailController controller, String titleString, String contentString) {
      super(context, titleString, contentString);
      detailController = controller;
    }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    detailController.onCancelLoad();
  }

}
