package org.exoplatform.ui;

import org.exoplatform.R;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

public class SettingServerEditionDialog extends Dialog {

  public SettingServerEditionDialog(Context context) {
    super(context);
    /** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    /** Design the dialog in main.xml file */
    setContentView(R.layout.exolanguagesetting);
  }

}
