package org.exoplatform.social.image;

import greendroid.widget.ActionBarItem;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.exoplatform.controller.AppController;
import org.exoplatform.social.ComposeMessageActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.MyActionBar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cyrilmottier.android.greendroid.R;

public class SocialImageLibrary extends MyActionBar {

  // private String title;
  private ArrayList<String>        listFiles;

  private String                   loadingData;

  private GridView                 gridView;

  private LoadImageTask            mLoadTask;

  private Bitmap                   bitmap;

  public static SocialImageLibrary socialImageLibrary;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.social_image_library_layout);
    onChangeLanguage(AppController.bundle);
    gridView = (GridView) findViewById(R.id.social_image_gridview);
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
    gridView.setAdapter(adapter);
    gridView.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
        String filePath = listFiles.get(pos);
        Intent intent = new Intent(getApplicationContext(), SelectedImageActivity.class);
        intent.putExtra(ExoConstants.SELECTED_IMAGE_EXTRA, filePath);
        startActivity(intent);
      }
    });
  }

  private void onChangeLanguage(ResourceBundle resourceBundle) {
    String title = resourceBundle.getString("PhotoLibrary");
    setTitle(title);
    loadingData = resourceBundle.getString("LoadingData");

  }

  private class ImageAdapter extends BaseAdapter {

    private Context           mContext;

    private ArrayList<String> list;

    public ImageAdapter(Context context, ArrayList<String> l) {
      mContext = context;
      list = l;
    }

    @Override
    public int getCount() {
      return list.size();
    }

    @Override
    public Object getItem(int pos) {
      return pos;
    }

    @Override
    public long getItemId(int pos) {
      return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
      if (list.size() > 0) {
        try {
          String filePath = list.get(position);
          File file = new File(filePath);
          FileInputStream fis = new FileInputStream(file);
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inSampleSize = 8;
          bitmap = BitmapFactory.decodeStream(fis, null, options);
          fis.close();
          fis = null;
        } catch (Exception e) {
        }
      }

      ImageView imageView;
      if (convertView == null) { // if it's not recycled, initialize some
                                 // attributes
        imageView = new ImageView(mContext);
        imageView.setLayoutParams(new GridView.LayoutParams(115, 115));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 8, 8, 8);
      } else {
        imageView = (ImageView) convertView;
      }
      imageView.setImageBitmap(bitmap);
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
      ArrayList<String> listResult = new ArrayList<String>();
      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        PhotoUltils.getAllImageFiles(new File(Environment.getExternalStorageDirectory() + "/eXo/"),
                                     listResult);
        return listResult;
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
