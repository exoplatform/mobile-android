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
import org.exoplatform.utils.ExoConstants;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class AddPhotoDialog extends Dialog implements android.view.View.OnClickListener {

    private Button                   takePhotoButton;

    private Button                   libraryButton;

    private Button                   cancelButton;

    private Activity                 mContext;

    private ComposeMessageController messageController;

    private DocumentActionDialog     fileActionDialog;

    public AddPhotoDialog(Activity context, DocumentActionDialog activity) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_photo_dialog_layout);
        mContext = context;
        fileActionDialog = activity;

        init();

    }

    public AddPhotoDialog(Activity context, ComposeMessageController controller) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_photo_dialog_layout);
        mContext = context;
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
        if (view.equals(takePhotoButton)) {
            dismiss();
            if (messageController == null)
                fileActionDialog._documentActionAdapter.initCamera();
            else
                messageController.initCamera();
        }
        // Start the native album photo
        if (view.equals(libraryButton)) {
            dismiss();
            Intent intent = new Intent(Intent.ACTION_PICK,
                                       android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType(ExoConstants.PHOTO_ALBUM_IMAGE_TYPE);
            mContext.startActivityForResult(intent, ExoConstants.REQUEST_ADD_PHOTO);
        }
        if (view.equals(cancelButton)) {
            dismiss();
        }
    }

}
