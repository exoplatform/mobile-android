/*
 * Copyright (C) 2010 Cyril Mottier (http://www.cyrilmottier.com)
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
 */
package eXo.eXoPlatform;

import greendroid.app.GDActivity;
import greendroid.app.GDListActivity;
import greendroid.image.ImageLoader;
import greendroid.image.ImageProcessor;
import greendroid.widget.AsyncImageView;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.cyrilmottier.android.greendroid.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class TestActivityBrowserView extends GDListActivity {
    
  public static TestActivityBrowserView testActivityBrowserViewInstance;
 
  Mock_Social_Activity mock;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
//        setActionBarContentView(R.layout.activitybrowserview);
        testActivityBrowserViewInstance = this;
        
        setTitle("Activity Browser");
        
        mock = new Mock_Social_Activity(false);
        
        List<Item> items = new ArrayList<Item>();
        
        for(int i = 0; i < mock.arrayOfActivities.size(); i++)
        {
          
//          items.add(new SeparatorItem(gadgetTab._strDbItemName));
          Bitmap bm =  BitmapFactory.decodeResource(getResources(), R.drawable.homeactivitystreamsiconiphone);
          String name = mock.arrayOfActivities.get(i).userID;
          String msg = mock.arrayOfActivities.get(i).title;
          int nbComment = mock.arrayOfActivities.get(i).nbComments;
          int nbLike = mock.arrayOfActivities.get(i).nbLikes;
          String time = Long.toString(mock.arrayOfActivities.get(i).postedTime);
          
          ActivityStreamBrowserItem item = new ActivityStreamBrowserItem(bm, name, msg, nbComment, nbLike, time, false); 
          items.add(item);
          
        }
        
        ActivityStreamBrowserItem item = new ActivityStreamBrowserItem(null, null, null, 0, 0, null, true); 
        items.add(item);
        
        final ItemAdapter adapter = new ItemAdapter(this, items);
        
        setListAdapter(adapter);
    }
        
    // Key down listener
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      // Save data to the server once the user hits the back button
      if (keyCode == KeyEvent.KEYCODE_BACK) {
//        Toast.makeText(AppController.this, strCannotBackToPreviousPage, Toast.LENGTH_LONG).show();

      }

      return false;
    }
    
    private static class MyAdapter extends BaseAdapter implements ImageProcessor {

      private static final StringBuilder BUILDER = new StringBuilder();

      private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      private final Rect mRectSrc = new Rect();
      private final Rect mRectDest = new Rect();
      private final String mImageForPosition;

      static class ViewHolder {
          public AsyncImageView imageViewAvatar;
          public TextView textViewName;
          public TextView textViewMessage;
          public Button buttonComment;
          public Button buttonLike;
          public TextView textViewTime;
          public TextView textViewShowMore;
          
          public StringBuilder textBuilder = new StringBuilder();
      }

      private Bitmap mMask;
      private int mThumbnailSize;
      private int mThumbnailRadius;
      private LayoutInflater mInflater;
      private String[] data;

      public MyAdapter(Context context,  String[] d) {
          mInflater = LayoutInflater.from(context);
          
          data = d;
          
          mImageForPosition = context.getString(R.string.image_for_position);

          mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

          mThumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
          mThumbnailRadius = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_radius);

          prepareMask();
      }

      private void prepareMask() {
          mMask = Bitmap.createBitmap(mThumbnailSize, mThumbnailSize, Bitmap.Config.ARGB_8888);

          Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
          paint.setColor(Color.RED);
          paint.setStyle(Paint.Style.FILL_AND_STROKE);

          Canvas c = new Canvas(mMask);
          c.drawRoundRect(new RectF(0, 0, mThumbnailSize, mThumbnailSize), mThumbnailRadius, mThumbnailRadius, paint);
      }

      public int getCount() {
          return data.length;
      }

      public Object getItem(int position) {
          return position;
      }

      public long getItemId(int position) {
          return position;
      }

      public View getView(int position, View convertView, ViewGroup parent) {

          ViewHolder holder;

          if (convertView == null) {
              convertView = mInflater.inflate(R.layout.activitybrowserviewcell, parent, false);
              holder = new ViewHolder();
              
              holder.imageViewAvatar = (AsyncImageView) convertView.findViewById(R.id.imageView_Avatar); 
              holder.imageViewAvatar.setImageProcessor(this);
              holder.textViewName = (TextView) convertView.findViewById(R.id.textView_Name);
                      
              holder.textViewMessage = (TextView) convertView.findViewById(R.id.textView_Message);
              
              holder.buttonComment = (Button) convertView.findViewById(R.id.button_Comment);
              
              holder.buttonLike = (Button) convertView.findViewById(R.id.button_Like);
              
              holder.textViewTime = (TextView) convertView.findViewById(R.id.textView_Time);
              
              holder.textViewShowMore = (TextView) convertView.findViewById(R.id.textView_Show_More);
              
              convertView.setTag(holder);
          } else {
              holder = (ViewHolder) convertView.getTag();
          }

          BUILDER.setLength(0);
          BUILDER.append(data[position]);
//          BUILDER.append(BASE_URL_PREFIX);
//          BUILDER.append(position);
//          BUILDER.append(BASE_URL_SUFFIX);
          holder.imageViewAvatar.setUrl(BUILDER.toString());

//          final StringBuilder textBuilder = holder.textBuilder;
//          textBuilder.setLength(0);
//          textBuilder.append(mImageForPosition);
//          textBuilder.append(position);
//          holder.textView.setText(textBuilder);

          return convertView;
      }

      public Bitmap processImage(Bitmap bitmap) {
          Bitmap result = Bitmap.createBitmap(mThumbnailSize, mThumbnailSize, Bitmap.Config.ARGB_8888);
          Canvas c = new Canvas(result);

          mRectSrc.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
          mRectDest.set(0, 0, mThumbnailSize, mThumbnailSize);
          c.drawBitmap(bitmap, mRectSrc, mRectDest, null);
          c.drawBitmap(mMask, 0, 0, mPaint);

          return result;
      }
  }

    public void finishMe() {
      
      Intent next = new Intent(TestActivityBrowserView.this, eXoApplicationsController2.class);
      startActivity(next);
      
      testActivityBrowserViewInstance = null;
      GDActivity.TYPE = 0;
    }

}
