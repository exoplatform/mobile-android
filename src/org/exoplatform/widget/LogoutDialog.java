package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.ui.HomeActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LogoutDialog extends Dialog implements OnClickListener {
  private TextView       contentView;

  private Button         okButton;

  private Button         cancelButton;

  private HomeActivity homeActivity;

  private Context        mContext;

  public LogoutDialog(Context context, HomeActivity homeAct) {
    super(context);
    setContentView(R.layout.logout_dialog_layout);
    mContext = context;
    homeActivity = homeAct;
    contentView = (TextView) findViewById(R.id.logout_content);

    okButton = (Button) findViewById(R.id.logout_ok_button);
    okButton.setOnClickListener(this);

    cancelButton = (Button) findViewById(R.id.logout_cancel_button);
    cancelButton.setOnClickListener(this);
    changeLanguage();
  }

  // @Override
  public void onClick(View view) {
    if (view.equals(okButton)) {
//      homeController.onFinish();
      dismiss();
    }

    if (view.equals(cancelButton)) {
      dismiss();
    }
  }

  private void changeLanguage() {
    Resources res = mContext.getResources();
    String titleString = res.getString(R.string.LogoutTitle);
    setTitle(titleString);
    String contentString = res.getString(R.string.LogoutContent);
    contentView.setText(contentString);
    String okString = res.getString(R.string.OK);
    okButton.setText(okString);
    String cancelString = res.getString(R.string.Cancel);
    cancelButton.setText(cancelString);
  }
}
