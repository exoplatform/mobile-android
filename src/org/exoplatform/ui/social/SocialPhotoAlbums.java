package org.exoplatform.ui.social;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.exoplatform.model.SocialPhotoInfo;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.MyActionBar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SocialPhotoAlbums extends MyActionBar {

  private String                  loadingData;

  public static SocialPhotoInfo photoInfoSelected;

  private LinearLayout            albumLayout;

  private LoadImageTask           mLoadTask;
  
  public static SocialPhotoAlbums socialPhotoAlbums;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.social_photo_albums_layout);
    socialPhotoAlbums = this;
    init();
    onChangeLanguage();
    onLoad();
  }

  private void init() {
    albumLayout = (LinearLayout) findViewById(R.id.social_photo_albums);
  }

  private void setListAdapter(ArrayList<SocialPhotoInfo> photoList) {
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, 1);
    albumLayout.removeAllViews();
    for (int i = 0; i < photoList.size(); i++) {
      final SocialPhotoInfo photoInfo = photoList.get(i);
      if (photoInfo.getImageNumber() > 0) {
        PhotoAlbumsItem item = new PhotoAlbumsItem(this, photoInfo);
        item.setOnClickListener(new OnClickListener() {

          public void onClick(View v) {
            photoInfoSelected = photoInfo;
            Intent intent = new Intent(SocialPhotoAlbums.this, SocialImageLibrary.class);
            startActivity(intent);
//            photoInfoSelected = photoInfo;
//            Intent intent = new Intent(SocialPhotoAlbums.this, CoverFlowExample.class);
//            startActivity(intent);
          }
        });
        
        albumLayout.addView(item, params);
      }
    }
  }
  private void onChangeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    String title = bundle.getString("PhotoLibrary");
    setTitle(title);
    loadingData = bundle.getString("LoadingData");

  }

  private void onLoad() {
    if (mLoadTask == null || mLoadTask.getStatus() == LoadImageTask.Status.FINISHED) {
      mLoadTask = (LoadImageTask) new LoadImageTask().execute();
    }
  }

  private class LoadImageTask extends UserTask<Void, Void, ArrayList<SocialPhotoInfo>> {
    private ProgressDialog _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = ProgressDialog.show(SocialPhotoAlbums.this, null, loadingData);
    }

    @Override
    public ArrayList<SocialPhotoInfo> doInBackground(Void... params) {
      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        Resources resources = getResources();
        String[] imageTypes = resources.getStringArray(R.array.image_small);
        FilenameFilter[] filter = new FilenameFilter[imageTypes.length];
        int i = 0;
        for (final String type : imageTypes) {
          filter[i] = new FilenameFilter() {
            public boolean accept(File dir, String name) {
              int index = name.lastIndexOf(".");
              String typeOfFile = name.substring(index + 1);
//              return name.endsWith("." + type) || name.endsWith("." + typeUpcase);
              return type.equalsIgnoreCase(typeOfFile);
              
//              return name.endsWith("." + type);
            }
          };
          i++;
        }
        return PhotoUltils.listFileToArray(Environment.getExternalStorageDirectory(), filter, -1);
      } else
        return null;
    }

    @Override
    public void onPostExecute(ArrayList<SocialPhotoInfo> result) {
      if (result != null) {
        setListAdapter(result);
      }
      _progressDialog.dismiss();

    }
  }

  private class PhotoAlbumsItem extends LinearLayout {
    private ImageView albumsAvatar;

    private TextView  albumsName;

    private TextView  albumsNumber;

    public PhotoAlbumsItem(Context context, SocialPhotoInfo info) {
      super(context);
      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.social_photo_albums_item_layout, this);
      albumsAvatar = (ImageView) view.findViewById(R.id.albums_image);
      File file = new File(info.getAlbumsAvatar());
      FileInputStream fis;
      try {
        fis = new FileInputStream(file);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
        albumsAvatar.setImageBitmap(bitmap);
      } catch (FileNotFoundException e) {
      }

      albumsName = (TextView) view.findViewById(R.id.albums_name);
      albumsName.setText(info.getAlbumsName());
      albumsNumber = (TextView) view.findViewById(R.id.albums_number);
      albumsNumber.setText("(" + info.getImageNumber()+")");
    }

  }

}
