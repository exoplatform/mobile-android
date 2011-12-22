package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.singleton.LocalizationHelper;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LogoutDialog extends Dialog implements OnClickListener {
  private TextView       contentView;

  private Button         okButton;

  private Button         cancelButton;

  private HomeController homeController;

  public LogoutDialog(Context context, HomeController controller) {
    super(context);
    setContentView(R.layout.logout_dialog_layout);
    homeController = controller;
    contentView = (TextView) findViewById(R.id.logout_content);

    okButton = (Button) findViewById(R.id.logout_ok_button);
    okButton.setOnClickListener(this);

    cancelButton = (Button) findViewById(R.id.logout_cancel_button);
    cancelButton.setOnClickListener(this);
    changeLanguage();
  }

//  @Override
  public void onClick(View view) {
    if (view.equals(okButton)) {
      homeController.onFinish();
      dismiss();
    }

    if (view.equals(cancelButton)) {
      dismiss();
    }
  }

  private void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    String titleString = bundle.getString("LogoutTitle");
    setTitle(titleString);
    String contentString = bundle.getString("LogoutContent");
    contentView.setText(contentString);
    String okString = bundle.getString("OK");
    okButton.setText(okString);
    String cancelString = bundle.getString("Cancel");
    cancelButton.setText(cancelString);
  }

}
