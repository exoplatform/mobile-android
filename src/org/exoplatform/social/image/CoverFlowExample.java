/*
 * Copyright (C) 2010 Neil Davies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code is base on the Android Gallery widget and was Created 
 * by Neil Davies neild001 'at' gmail dot com to be a Coverflow widget
 * 
 * @author Neil Davies
 */
package org.exoplatform.social.image;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.MyActionBar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

public class CoverFlowExample extends MyActionBar {
    /** Called when the activity is first created. */
  
  private PhotoInfo                photoInfo;
  private LoadImageTask           mLoadTask;
  
  private ArrayList<String> mImageStrs;
  
  private ArrayList<Bitmap> mBitmaps;
  
  CoverFlow coverFlow;

  
    @Override
    public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     requestWindowFeature(Window.FEATURE_NO_TITLE);
     setTheme(R.style.Theme_eXo);
//     setActionBarContentView(R.layout.social_photo_albums_layout);
     
     photoInfo = SocialPhotoAlbums.photoInfoSelected;
     setTitle(photoInfo.getAlbumsName());
     
     
     
     mImageStrs = photoInfo.getImageList();
     mBitmaps = new ArrayList<Bitmap>();
     
     onLoad();
     
    }
    
    private void onLoad() {
      if (mLoadTask == null || mLoadTask.getStatus() == LoadImageTask.Status.FINISHED) {
        mLoadTask = (LoadImageTask) new LoadImageTask().execute();
      }
    }
    
    private void setListAdapter(ArrayList<Bitmap> bitmapList) {
      
      coverFlow = new CoverFlow(this);

      coverFlow.setSpacing(-25);
      coverFlow.setSelection(4, true);
      coverFlow.setAnimationDuration(1000);
      
      
      ImageAdapter coverImageAdapter =  new ImageAdapter(this);
      
      coverFlow.setAdapter(coverImageAdapter);

      setActionBarContentView(coverFlow);
    }
    
 public class ImageAdapter extends BaseAdapter{
     int mGalleryItemBackground;
     private Context mContext;

     //     private ImageView[] mImages;
     
     public ImageAdapter(Context c) {
      mContext = c;
//      mImages = new ImageView[mImageStrs.size()];
      
     }
     
     public boolean createReflectedImages() {
          //The gap we want between the reflection and the original image
          final int reflectionGap = 4;
          
          int index = 0;
          for (String imageStr : mImageStrs) 
          {
            
           Bitmap originalImage = PhotoUltils.shrinkBitmap(imageStr, 300, 300);
           mBitmaps.add(originalImage);
           int width = originalImage.getWidth();
           int height = originalImage.getHeight();
     
           //This will not scale but will flip on the Y axis
           Matrix matrix = new Matrix();
           matrix.preScale(1, -1);
           
           //Create a Bitmap with the flip matrix applied to it.
           //We only want the bottom half of the image
           Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height/2, width, height/2, matrix, false);
           
               
           //Create a new bitmap with same width but taller to fit reflection
           Bitmap bitmapWithReflection = Bitmap.createBitmap(width 
             , (height + height/2), Config.ARGB_8888);
         
          //Create a new Canvas with the bitmap that's big enough for
          //the image plus gap plus reflection
          Canvas canvas = new Canvas(bitmapWithReflection);
          //Draw in the original image
          canvas.drawBitmap(originalImage, 0, 0, null);
          //Draw in the gap
          Paint deafaultPaint = new Paint();
          canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);
          //Draw in the reflection
          canvas.drawBitmap(reflectionImage,0, height + reflectionGap, null);
          
          //Create a shader that is a linear gradient that covers the reflection
          Paint paint = new Paint(); 
          LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0, 
            bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, 
            TileMode.CLAMP); 
          //Set the paint to use this shader (linear gradient)
          paint.setShader(shader); 
          //Set the Transfer mode to be porter duff and destination in
          paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
          //Draw a rectangle using the paint with our linear gradient
          canvas.drawRect(0, height, width, 
            bitmapWithReflection.getHeight() + reflectionGap, paint); 
          
          ImageView imageView = new ImageView(mContext);
          imageView.setImageBitmap(bitmapWithReflection);
          imageView.setLayoutParams(new CoverFlow.LayoutParams(120, 120));
          imageView.setScaleType(ScaleType.MATRIX);
//          mImages[index++] = imageView;
          
          }
       return true;
  }

     public int getCount() {
         return mImageStrs.size();
     }

     public Object getItem(int position) {
         return position;
     }

     public long getItemId(int position) {
         return position;
     }

     public View getView(int position, View convertView, ViewGroup parent) {

      //Use this code if you want to load from resources
       
       ImageView imgView = new ImageView(mContext);
       imgView.setLayoutParams(new CoverFlow.LayoutParams(200, 200));
       imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
     
       if(position < mImageStrs.size())
       {
           imgView.setImageBitmap(mBitmaps.get(position));       
           BitmapDrawable drawable = (BitmapDrawable) imgView.getDrawable();
           drawable.setAntiAlias(true);
       }

         return imgView;
      
     }
   /** Returns the size (0.0f to 1.0f) of the views 
      * depending on the 'offset' to the center. */ 
      public float getScale(boolean focused, int offset) { 
        /* Formula: 1 / (2 ^ offset) */ 
          return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset))); 
      }

 }

 private class LoadImageTask extends UserTask<Void, Void, ArrayList<Bitmap>> {
   private ProgressDialog _progressDialog;

   @Override
   public void onPreExecute() {
     _progressDialog = ProgressDialog.show(CoverFlowExample.this, null, "Loading Data...");
   }

   @Override
   public ArrayList<Bitmap> doInBackground(Void... params) {
     
     for (String imageStr : mImageStrs) 
     {
       Bitmap originalImage = PhotoUltils.shrinkBitmap(imageStr, 300, 300);
       mBitmaps.add(originalImage);
     }
     
     return mBitmaps;
   }

   @Override
   public void onPostExecute(ArrayList<Bitmap> result) {
     if (result != null) {
       setListAdapter(result);
     }
     _progressDialog.dismiss();

   }
 }

}
