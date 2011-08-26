package org.exoplatform.social.image;

import greendroid.widget.ActionBarItem;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.exoplatform.R;
import org.exoplatform.controller.AppController;
import org.exoplatform.social.ComposeMessageActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.MyActionBar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class SocialImageLibrary extends MyActionBar {

  // private String title;
  private ArrayList<String>        listFiles;

  private String                   loadingData;

  private LoadImageTask            mLoadTask;

  private Bitmap                   bitmap;

  public static SocialImageLibrary socialImageLibrary;

  private PhotoInfo                photoInfo;
  
  private CoverFlow coverFlow;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
//    setActionBarContentView(R.layout.social_image_library_layout);
    photoInfo = SocialPhotoAlbums.photoInfoSelected;
    setTitle(photoInfo.getAlbumsName());
    onChangeLanguage(AppController.bundle);
    socialImageLibrary = this;

    onLoad();
  }

  private void onLoad() {
    if (mLoadTask == null || mLoadTask.getStatus() == LoadImageTask.Status.FINISHED) {
      mLoadTask = (LoadImageTask) new LoadImageTask().execute();
    }
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

    switch (position) {
    case -1:
      finish();
      SocialPhotoAlbums.socialPhotoAlbums.finish();
      ComposeMessageActivity.composeMessageActivity.finish();

      break;

    case 0:
      break;
    }
    return true;
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }

  private void setAdapter(ArrayList<String> list) {
    ImageAdapter adapter = new ImageAdapter(this, list);
    
    coverFlow = new CoverFlow(this);

    coverFlow.setSpacing(-25);
    coverFlow.setSelection(4, true);
    coverFlow.setAnimationDuration(1000);
    coverFlow.setAdapter(adapter);
    coverFlow.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
        String filePath = listFiles.get(pos);
        Intent intent = new Intent(getApplicationContext(), SelectedImageActivity.class);
        intent.putExtra(ExoConstants.SELECTED_IMAGE_EXTRA, filePath);
        startActivity(intent);
      }
    });
    setActionBarContentView(coverFlow);
  }

  private void onChangeLanguage(ResourceBundle resourceBundle) {
    loadingData = resourceBundle.getString("LoadingData");

  }

  
  private class ImageAdapter extends BaseAdapter {
    int                       mGalleryItemBackground;

    private Context           mContext;

    private ArrayList<String> list;

    public ImageAdapter(Context c, ArrayList<String> l) {
      mContext = c;
      TypedArray attr = mContext.obtainStyledAttributes(R.styleable.HelloGallery);
      mGalleryItemBackground = attr.getResourceId(R.styleable.HelloGallery_android_galleryItemBackground,
                                                  0);
      attr.recycle();
      list = l;
    }

    public int getCount() {
      return list.size();
    }

    public Object getItem(int position) {
      return position;
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ImageView imageView = new ImageView(mContext);
      if (list.size() > 0) {
        String filePath = list.get(position);
        bitmap = PhotoUltils.shrinkBitmap(filePath, 200, 200);
        if(bitmap!=null){
          bitmap =PhotoUltils.reflectionPhoto(bitmap);
        }
       
      }
      imageView.setImageBitmap(bitmap);
      imageView.setLayoutParams(new Gallery.LayoutParams(280, 240));
      imageView.setScaleType(ImageView.ScaleType.FIT_XY);
      imageView.setBackgroundResource(mGalleryItemBackground);
      bitmap = null;
      return imageView;
    }
  }

  private class LoadImageTask extends UserTask<Void, Void, ArrayList<String>> {
    private ProgressDialog _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = ProgressDialog.show(SocialImageLibrary.this, null, loadingData);
    }

    @Override
    public ArrayList<String> doInBackground(Void... params) {
//      ArrayList<String> listResult = new ArrayList<String>();
      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        // PhotoUltils.getAllImages(Environment.getExternalStorageDirectory(),
        // listResult);

        return photoInfo.getImageList();
      } else
        return null;
    }

    @Override
    public void onPostExecute(ArrayList<String> result) {
      if (result != null) {
        setAdapter(result);
        listFiles = result;
      }
      _progressDialog.dismiss();

    }
  }

}
