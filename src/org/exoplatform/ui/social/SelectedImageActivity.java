package org.exoplatform.ui.social;

import android.support.v7.app.ActionBarActivity;
//import greendroid.widget.ActionBarItem;

import java.io.File;

import org.exoplatform.poc.tabletversion.R;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
//import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.WaitingDialog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Represents the screen to select or edit an image<br/>
 *
 * Can be started from document screen for selecting an image<br/>
 */
public class SelectedImageActivity extends ActionBarActivity implements OnClickListener {
  //extends MyActionBar

  private static final int SCALE_WIDTH  = 1024;

  private static final int SCALE_HEIGHT = 860;

  private ImageView        imageView;

  private Button           okButton;

  private Button           mCancelBtn;

  private String           filePath     = null;

  private File             file;

  private String           okText;

  private String           removeText;

  private String           cancelText;

  private String           loadingData;

  private String           title;

  /** works in 2 mode: selecting 0 or editing image 1 */
  private int              mMode;

  private static final int SELECT_MODE  = 2;      // Default mode

  private static final int EDIT_MODE    = 1;

  private Uri              mImageUri;

  private DisplayImageTask mLoadTask;

  private static final String TAG = "eXo____SelectedImageActivity____";


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //requestWindowFeature(Window.FEATURE_NO_TITLE);
    //setTheme(R.style.Theme_eXo);

    //setActionBarContentView(R.layout.social_selected_image_layout);
    setContentView(R.layout.social_selected_image_layout);

    if (savedInstanceState != null)
      finish();
    else {
      mMode = getIntent().getIntExtra(ExoConstants.SELECTED_IMAGE_MODE, 0);
      init();
      onLoad(mMode);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    //this.getContentView().removeAllViews();
    //this.setActionBarContentView(R.layout.social_selected_image_layout);

    setContentView(R.layout.social_selected_image_layout);
    init();
    onLoad(mMode);
  }

  private void init() {
    onChangeLanguage();
    setTitle(title);
    okButton = (Button) findViewById(R.id.social_selected_image_ok_button);
    okButton.setText(okText);
    okButton.setOnClickListener(this);
    imageView = (ImageView) findViewById(R.id.social_selected_image);

    mCancelBtn = (Button) findViewById(R.id.social_selected_image_remove_button);
    mCancelBtn.setText(mMode == EDIT_MODE ? getString(R.string.Remove) : getString(R.string.Cancel));
    mCancelBtn.setOnClickListener(this);
  }

  private void onLoad(int id) {
    if (!ExoConnectionUtils.isNetworkAvailableExt(this)) {
      new ConnectionErrorDialog(this).show();
      return ;
    }

    if (mLoadTask == null || mLoadTask.getStatus() == DisplayImageTask.Status.FINISHED) {
      mLoadTask = (DisplayImageTask) new DisplayImageTask().execute(id);
    }
  }

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == DisplayImageTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  /** TODO - replace
  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      finish();
      if (ComposeMessageActivity.composeMessageActivity != null)
        ComposeMessageActivity.composeMessageActivity.finish();
      if (SocialTabsActivity.instance!= null)
        SocialTabsActivity.instance.finish();
      if (DocumentActivity._documentActivityInstance != null) {
        DocumentActivity._documentActivityInstance.finish();
      }
      break;

    case 0:
      break;
    }
    return true;
  }
   **/


  @Override
  public void onBackPressed() {
    onCancelLoad();
    if (mMode == SELECT_MODE) {
      backToGallery();
    }
    finish();
  }

  public void onClick(View view) {

    if (view.equals(okButton)) {
      if (filePath != null) {
        if (DocumentActivity.instance != null) {
          DocumentActivity.instance._sdcard_temp_dir = filePath;
          DocumentActivity.instance.uploadFile();
        } else {
          ComposeMessageActivity.addImageToMessage(file);
        }
      }

    }

    /** Click on Cancel or Remove */
    if (view.equals(mCancelBtn)) {
      if (mMode == EDIT_MODE)   ComposeMessageActivity.removeImageFromMessage();
      if (mMode == SELECT_MODE) backToGallery();
    }

    finish();
  }

  private void backToGallery() {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType(ExoConstants.PHOTO_ALBUM_IMAGE_TYPE);

    if (DocumentActivity.instance != null) {
      DocumentActivity.instance.startActivityForResult(intent, ExoConstants.REQUEST_ADD_PHOTO);

    }
    else if (ComposeMessageActivity.composeMessageActivity != null) {
      ComposeMessageActivity.composeMessageActivity.startActivityForResult(intent, ExoConstants.REQUEST_ADD_PHOTO);
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
    private WaitingDialog mProgressDialog;

    @Override
    public void onPreExecute() {
      mProgressDialog = new WaitingDialog(SelectedImageActivity.this, null, loadingData) {

        @Override
        public void onBackPressed() {
          super.onBackPressed();
          onCancelLoad();
        }

      };
      mProgressDialog.show();
    }

    @Override
    public Bitmap doInBackground(Integer... params) {
      int modeId = params[0];
      if (modeId == SELECT_MODE) {
        mImageUri = getIntent().getData();
        String host = mImageUri.getHost();
        // check if the image is store in memory or not
        if (host.equals("media")) {
          filePath = PhotoUtils.getFileFromUri(mImageUri, SelectedImageActivity.this);
        } else {
          // get the full image url and download it to sdcard
          StringBuffer buffer = new StringBuffer(mImageUri.getScheme()).append("://")
              .append(host).append(mImageUri.getPath());
          Log.i("PHOTO_PICKER", "Image mImageUri getHost(): " + buffer.toString());
          String fileName = mImageUri.getLastPathSegment();
          filePath = PhotoUtils.downloadFile(buffer.toString(), fileName);
        }

      } else {
        filePath = getIntent().getStringExtra(ExoConstants.SELECTED_IMAGE_EXTRA);
      }

      try {
        Log.i("PHOTO_PICKER", "Image File Path: " + filePath);
        file = new File(filePath);
        return PhotoUtils.shrinkBitmap(filePath, SCALE_WIDTH, SCALE_HEIGHT);
      } catch (NullPointerException e) {
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
      mProgressDialog.dismiss();
    }

  }

}
