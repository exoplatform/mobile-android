package org.exoplatform.widget;

import android.content.Context;

public class SocialWaitingDialog extends WaitingDialog {
    
  
    public SocialWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      dismiss();
     // onCancelLoad();
    }
     
    
  }