package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;

import java.util.ArrayList;
import java.util.HashMap;

import org.exoplatform.R;
import org.exoplatform.model.SocialPhotoInfo;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.CoverFlow;
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

//  private Bitmap                   bitmap;

  public static SocialImageLibrary socialImageLibrary;

  private SocialPhotoInfo          photoInfo;

  private CoverFlow                coverFlow;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    // setActionBarContentView(R.layout.social_image_library_layout);
    photoInfo = SocialPhotoAlbums.photoInfoSelected;
    setTitle(photoInfo.getAlbumsName());
    onChangeLanguage();
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
      if (SocialPhotoAlbums.socialPhotoAlbums != null)
        SocialPhotoAlbums.socialPhotoAlbums.finish();
      if (ComposeMessageActivity.composeMessageActivity != null)
        ComposeMessageActivity.composeMessageActivity.finish();
      if (SocialActivity.socialActivity != null)
        SocialActivity.socialActivity.finish();

      break;

    case 0:
      break;
    }
    return true;
  }

  @Override
  public void onBackPressed() {
    finish();
  }
  
  @Override
  public void onLowMemory() {
    super.onLowMemory();
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

  private void onChangeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    loadingData = bundle.getString("LoadingData");

  }
  
  

  private class ImageAdapter extends BaseAdapter {
    int                       mGalleryItemBackground;

    private Context           mContext;

    private ArrayList<String> list;
    
    private HashMap<String, ImageView> imgViews;

    public ImageAdapter(Context c, ArrayList<String> l) {
      mContext = c;
      TypedArray attr = mContext.obtainStyledAttributes(R.styleable.HelloGallery);
      mGalleryItemBackground = attr.getResourceId(R.styleable.HelloGallery_android_galleryItemBackground,
                                                  0);
      attr.recycle();
      list = l;
      imgViews = new HashMap<String, ImageView>();
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
      ImageView imageView = null;
      if (list.size() > 0) {
        String filePath = list.get(position);
        
        imageView = imgViews.get(list.get(position));
        
        if(imageView == null ) {
          
         Bitmap bitmap = PhotoUltils.shrinkBitmap(filePath, 160, 120);
//          if (bitmap != null) {
//            bitmap = PhotoUltils.reflectionPhoto(bitmap);
            
          imageView = new ImageView(mContext);
          imageView.setImageBitmap(bitmap);
          imageView.setLayoutParams(new Gallery.LayoutParams(280, 240));
          imageView.setScaleType(ImageView.ScaleType.FIT_XY);
          imageView.setBackgroundResource(mGalleryItemBackground);
         
          imgViews.put(list.get(position), imageView);
        }
        
      }
      
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
      // ArrayList<String> listResult = new ArrayList<String>();
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
