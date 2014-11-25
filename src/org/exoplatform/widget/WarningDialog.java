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

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class WarningDialog extends Dialog implements android.view.View.OnClickListener {
  private TextView titleView;

  private TextView contentView;

  protected Button   okButton;

  public WarningDialog(Context context, String titleString, String contentString, String okString) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.warning_dialog_layout);
    titleView = (TextView) findViewById(R.id.warning_dialog_title_view);
    titleView.setText(titleString);
    ImageView imageView = (ImageView) findViewById(R.id.warning_image);
    imageView.setImageResource(R.drawable.warning_icon);
    contentView = (TextView) findViewById(R.id.warning_content);
    contentView.setText(contentString);
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