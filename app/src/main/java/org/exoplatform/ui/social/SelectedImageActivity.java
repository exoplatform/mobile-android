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
package org.exoplatform.ui.social;

import java.io.File;

import org.exoplatform.R;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.WaitingDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class SelectedImageActivity extends Activity implements OnClickListener, OnRequestPermissionsResultCallback {

  private static final int SCALE_WIDTH  = 1024;

  private static final int SCALE_HEIGHT = 860;

  private static final int SELECT_MODE  = 2;

  private static final int EDIT_MODE    = 1;

  private ImageView        imageView;

  private Button           okButton;

  private Button           removeButton;

  private String           filePath     = null;

  private File             file;

  private String           okText;

  private String           removeText;

  private String           cancelText;

  private String           loadingData;

  private String           title;

  private int              modeId;

  private DisplayImageTask mLoadTask;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.social_selected_image_layout);
    if (savedInstanceState != null)
      finish();
    else {
      modeId = getIntent().getIntExtra(ExoConstants.SELECTED_IMAGE_MODE, 0);
      init();
      onLoad(modeId);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    init();
    onLoad(modeId);
  }

  private void init() {
    onChangeLanguage();
    setTitle(title);
    okButton = (Button) findViewById(R.id.social_selected_image_ok_button);
    okButton.setText(okText);
    okButton.setOnClickListener(this);
    imageView = (ImageView) findViewById(R.id.social_selected_image);
    removeButton = (Button) findViewById(R.id.social_selected_image_remove_button);
    if (modeId == EDIT_MODE) {
      removeButton.setText(removeText);
    } else {
      removeButton.setText(cancelText);
    }

    removeButton.setOnClickListener(this);

  }

  @SuppressLint("Override")
  @Override
  public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] results) {
    if (reqCode == ExoConstants.REQUEST_PICK_IMAGE_FROM_GALLERY) {
      if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
        // permission granted
        onLoad(modeId);
      } else {
        // permission denied
        if (ExoDocumentUtils.shouldDisplayExplanation(this, ExoConstants.REQUEST_PICK_IMAGE_FROM_GALLERY)) {
          PhotoUtils.alertNeedStoragePermission(this);
        } else {
          Toast.makeText(this, R.string.PermissionStorageDeniedToast, Toast.LENGTH_LONG).show();
        }
        finish();
      }
    }
  }

  private void onLoad(int id) {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      boolean hasPermission = !ExoDocumentUtils.didRequestPermission(this, ExoConstants.REQUEST_PICK_IMAGE_FROM_GALLERY);
      if (hasPermission && (mLoadTask == null || mLoadTask.getStatus() == DisplayImageTask.Status.FINISHED)) {
        mLoadTask = (DisplayImageTask) new DisplayImageTask().execute(id);
      }
    } else {
      new ConnectionErrorDialog(this).show();
    }
  }

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == DisplayImageTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  @Override
  public void onBackPressed() {
    onCancelLoad();
    if (modeId == SELECT_MODE) {
      backToGallery();
    }
    finish();
  }

  public void onClick(View view) {
    if (view.equals(okButton)) {
      if (filePath != null) {
        if (DocumentActivity._documentActivityInstance != null) {
          DocumentActivity._documentActivityInstance._sdcard_temp_dir = filePath;
          DocumentActivity._documentActivityInstance.uploadFile();
        } else
          ComposeMessageActivity.addImageToMessage(file);
      }

    }

    if (view.equals(removeButton)) {
      if (modeId == EDIT_MODE) {
        ComposeMessageActivity.removeImageFromMessage();
      }
      if (modeId == SELECT_MODE) {
        backToGallery();
      }
    }
    finish();

  }

  private void backToGallery() {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType(ExoConstants.PHOTO_ALBUM_IMAGE_TYPE);
    if (DocumentActivity._documentActivityInstance != null) {
      DocumentActivity._documentActivityInstance.startActivityForResult(intent, ExoConstants.REQUEST_PICK_IMAGE_FROM_GALLERY);
    } else if (ComposeMessageActivity.composeMessageActivity != null) {
      ComposeMessageActivity.composeMessageActivity.startActivityForResult(intent, ExoConstants.REQUEST_PICK_IMAGE_FROM_GALLERY);
    }
  }

  private void onChangeLanguage() {
    Resources resource = getResources();
    okText = resource.getString(R.string.OK);
    cancelText = resource.getString(R.string.Cancel);
    removeText = resource.getString(R.string.Remove);
    loadingData = resource.getString(R.string.LoadingImage);
    title = resource.getString(R.string.SelectedPhoto);

  }

  private class DisplayImageTask extends AsyncTask<Integer, Void, Bitmap> {
    private SelectImageWaitingDialog _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = new SelectImageWaitingDialog(SelectedImageActivity.this, null, loadingData);
      _progressDialog.show();
    }

    @Override
    public Bitmap doInBackground(Integer... params) {
      int modeId = params[0];
      if (modeId == SELECT_MODE) {
        Uri imageUri = getIntent().getData();
        String host = imageUri.getHost();
        String scheme = imageUri.getScheme();
        // check if the image is stored on the device (host = media)
        // or if it's available with a content provider (scheme =
        // content)
        if ("media".equals(host) || "content".equals(scheme)) {
          filePath = PhotoUtils.getFileFromUri(imageUri, SelectedImageActivity.this);
        } else {
          // get the full image url and download it to sdcard
          String path = imageUri.getPath();
          StringBuffer buffer = new StringBuffer(scheme);
          buffer.append("://");
          buffer.append(host);
          buffer.append(path);
          Log.i("PHOTO_PICKER", "Image imageUri getHost(): " + buffer.toString());
          String fileName = imageUri.getLastPathSegment();
          filePath = PhotoUtils.downloadFile(buffer.toString(), fileName);
        }

      } else {
        filePath = getIntent().getStringExtra(ExoConstants.SELECTED_IMAGE_EXTRA);
      }

      if (filePath != null) {
        Log.i("PHOTO_PICKER", "Image File Path: " + filePath);
        file = new File(filePath);
        Bitmap bm = PhotoUtils.shrinkBitmap(filePath, SCALE_WIDTH, SCALE_HEIGHT);
        return ExoDocumentUtils.rotateBitmapToNormal(filePath, bm);
      } else {
        return null;
      }
    }

    @Override
    public void onPostExecute(Bitmap result) {
      if (result != null) {
        imageView.setImageBitmap(result);
      } else {
        okButton.setClickable(false);
      }
      _progressDialog.dismiss();
    }

    @Override
    protected void onCancelled(Bitmap result) {
      if (_progressDialog != null)
        _progressDialog.dismiss();
      onCancelled();
    }

  }

  private class SelectImageWaitingDialog extends WaitingDialog {

    public SelectImageWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      onCancelLoad();
    }

  }

}
