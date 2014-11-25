/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
