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
import org.exoplatform.ui.social.ComposeMessageActivity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class RemoveAttachedPhotoDialog extends Dialog implements android.view.View.OnClickListener {
  private Button removePhotoButton;

  public RemoveAttachedPhotoDialog(Context context) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.remove_photo_dialog_layout);
    setCanceledOnTouchOutside(true);
    removePhotoButton = (Button) findViewById(R.id.remove_photo_remove_button);
    removePhotoButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    if (view.equals(removePhotoButton)) {
      ComposeMessageActivity.removeImageFromMessage();
    }
    dismiss();
  }

}
