/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.widget;

import org.exoplatform.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Apr
 * 10, 2012
 */
public class ConnTimeOutDialog extends Dialog implements android.view.View.OnClickListener {

  private TextView titleView;

  private TextView contentView;

  private Button   okButton;

  public ConnTimeOutDialog(Context context, String titleString, String okString) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.warning_dialog_layout);
    titleView = (TextView) findViewById(R.id.warning_dialog_title_view);
    titleView.setText(titleString);
    ImageView imageView = (ImageView) findViewById(R.id.warning_image);
    imageView.setImageResource(R.drawable.warning_icon);
    contentView = (TextView) findViewById(R.id.warning_content);
    contentView.setText(context.getString(R.string.NetworkTimeout));
    okButton = (Button) findViewById(R.id.warning_ok_button);
    okButton.setText(okString);
    okButton.setOnClickListener(this);
  }

  public void onClick(View view) {
    if (view.equals(okButton)) {
      dismiss();
    }
  }

}
