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
import android.view.Window;
import android.widget.TextView;

public class WaitingDialog extends Dialog {
  private TextView contentView;

  public WaitingDialog(Context context, String titleString, String contentString) {
    super(context);
    if (titleString == null) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
    } else {
      setTitle(titleString);
    }
    setContentView(R.layout.waiting_dialog_layout);
    contentView = (TextView) findViewById(R.id.waiting_content);
    contentView.setText(contentString);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    dismiss();
  }

  public void setMessage(String message) {
    contentView.setText(message);
  }
}
