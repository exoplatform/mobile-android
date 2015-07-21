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
import org.exoplatform.utils.CompatibleFileOpen;
import org.exoplatform.utils.ExoDocumentUtils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CompatibleFileOpenDialog extends Dialog implements android.view.View.OnClickListener {

  private Button  okButton;

  private Button  cancelButton;

  private String  fileType;

  private String  filePath;

  private String  fileName;

  public CompatibleFileOpenDialog(Context context, String fType, String fPath, String fName) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.compatible_open_file_dialog_layout);
    TextView titleView = (TextView) findViewById(R.id.com_dialog_title_view);
    titleView.setText(fName);
    TextView contentView = (TextView) findViewById(R.id.com_warning_content);
    contentView.setText(context.getResources().getString(R.string.CompatibleFileSuggest));
    okButton = (Button) findViewById(R.id.com_ok_button);
    okButton.setOnClickListener(this);
    cancelButton = (Button) findViewById(R.id.com_cancel_button);
    cancelButton.setOnClickListener(this);
    ImageView iconView = (ImageView) findViewById(R.id.com_warning_image);
    iconView.setBackgroundResource(ExoDocumentUtils.getIconFromType(fType));
    fileType = fType;
    filePath = fPath;
    fileName = fName;
  }

  public void onClick(View view) {
    if (view.equals(cancelButton)) {
      dismiss();
    }
    if (view.equals(okButton)) {
      new CompatibleFileOpen(view.getContext(), fileType, filePath, fileName);
      dismiss();
    }
  }

}
