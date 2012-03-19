package org.exoplatform.widget;

import org.exoplatform.controller.social.ComposeMessageController;

import android.content.Context;

public class PostWaitingDialog extends WaitingDialog {

  private ComposeMessageController messageController;

  public PostWaitingDialog(Context context,
                           ComposeMessageController controller,
                           String titleString,
                           String contentString) {
    super(context, titleString, contentString);
    messageController = controller;
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    messageController.onCancelPostTask();
  }

}
