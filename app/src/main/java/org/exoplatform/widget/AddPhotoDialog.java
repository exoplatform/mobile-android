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
import org.exoplatform.controller.social.ComposeMessageController;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.utils.PhotoUtils;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class AddPhotoDialog extends Dialog implements android.view.View.OnClickListener {

  private Button                   takePhotoButton;

  private Button                   libraryButton;

  private Button                   cancelButton;

  private Activity                 mActivity;

  private ComposeMessageController messageController;

  private DocumentActionDialog     fileActionDialog;

  public AddPhotoDialog(Activity callingActivity, DocumentActionDialog dialog) {
    super(callingActivity);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.add_photo_dialog_layout);
    mActivity = callingActivity;
    fileActionDialog = dialog;

    init();

  }

  public AddPhotoDialog(Activity callingActivity, ComposeMessageController controller) {
    super(callingActivity);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.add_photo_dialog_layout);
    mActivity = callingActivity;
    messageController = controller;

    init();
  }

  private void init() {

    takePhotoButton = (Button) findViewById(R.id.add_photo_take_button);
    takePhotoButton.setOnClickListener(this);
    libraryButton = (Button) findViewById(R.id.add_photo_library_button);
    libraryButton.setOnClickListener(this);
    cancelButton = (Button) findViewById(R.id.add_photo_cancel_button);
    cancelButton.setOnClickListener(this);

  }

  public void onClick(View view) {
    if (view.equals(cancelButton)) {
      dismiss();
    } else {
      if (view.equals(takePhotoButton)) {
        // take photo
        dismiss();
        startCapture();
      } else if (view.equals(libraryButton)) {
        // pick photo from gallery
        dismiss();
        pickPhoto();
      }
    }
  }

  private void startCapture() {
    if (messageController == null)
      fileActionDialog._documentActionAdapter.initCamera();
    else
      messageController.initCamera();
  }

  private void pickPhoto() {
    PhotoUtils.pickPhotoForActivity(mActivity);
  }
}
