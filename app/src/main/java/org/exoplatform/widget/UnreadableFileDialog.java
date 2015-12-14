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
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UnreadableFileDialog extends Dialog implements android.view.View.OnClickListener {

  private Button okButton;

  public UnreadableFileDialog(Context context, String content) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.warning_dialog_layout);
    TextView titleView = (TextView) findViewById(R.id.warning_dialog_title_view);
    titleView.setText(context.getResources().getString(R.string.UnreachableFileTitle));
    // ImageView imageView = (ImageView) findViewById(R.id.warning_image);
    // imageView.setImageResource(R.drawable.icon_for_unreadable_file);
    TextView contentView = (TextView) findViewById(R.id.warning_content);
    if (content == null) {
      contentView.setText(context.getResources().getString(R.string.UnreachableFile));
    } else
      contentView.setText(content);

    Drawable icon = context.getResources().getDrawable(R.drawable.icon_for_unreadable_file);
    icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
    contentView.setCompoundDrawables(null, icon, null, null);

    okButton = (Button) findViewById(R.id.warning_ok_button);
    okButton.setOnClickListener(this);
  }

  public void onClick(View view) {
    if (view.equals(okButton)) {
      dismiss();
    }
  }

}
