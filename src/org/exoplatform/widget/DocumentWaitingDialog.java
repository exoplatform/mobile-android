package org.exoplatform.widget;

import org.exoplatform.ui.DocumentActivity;

import android.content.Context;

public class DocumentWaitingDialog extends WaitingDialog {

  public DocumentWaitingDialog(Context context, String titleString, String contentString) {
    super(context, titleString, contentString);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    DocumentActivity._documentActivityInstance.onCancelLoad();
  }

}
