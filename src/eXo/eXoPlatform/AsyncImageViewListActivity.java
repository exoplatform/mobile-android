/*
 * Copyright (C) 2011 Cyril Mottier (http://www.cyrilmottier.com)
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

import com.cyrilmottier.android.greendroid.R;

import greendroid.app.GDActivity;
import greendroid.app.GDListActivity;
import greendroid.image.ImageProcessor;
import greendroid.widget.AsyncImageView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class AsyncImageViewListActivity extends GDListActivity implements OnScrollListener {

  public static AsyncImageViewListActivity asyncImageViewListActivityInstance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        asyncImageViewListActivityInstance = this;
        setTitle("Activity Streams");
        
        Mock_Social_Activity mock = new Mock_Social_Activity(false);
        
        setListAdapter(new MyAdapter(this, mock));
        getListView().setOnScrollListener(this);
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
            
//            public StringBuilder textBuilder = new StringBuilder();
        }

        private Bitmap mMask;
        private int mThumbnailSize;
        private int mThumbnailRadius;
        private LayoutInflater mInflater;
        Mock_Social_Activity mock;
        
        public MyAdapter(Context context, Mock_Social_Activity _mock) {
            mInflater = LayoutInflater.from(context);
            
            mock = _mock;
            
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
            return mock.mStrings.length;
        }

        public Object getItem(int position) {
            return null;
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
            BUILDER.append(mock.mStrings[position]);
//            BUILDER.append(BASE_URL_PREFIX);
//            BUILDER.append(position);
//            BUILDER.append(BASE_URL_SUFFIX);
            holder.imageViewAvatar.setUrl(BUILDER.toString());

            if(position < mock.arrayOfActivities.size())
            {
              holder.textViewName.setText(mock.arrayOfActivities.get(position).userID);
              holder.textViewMessage.setText(mock.arrayOfActivities.get(position).title);
              holder.buttonComment.setText(Integer.toString(mock.arrayOfActivities.get(position).nbComments));
              holder.buttonLike.setText(Integer.toString(mock.arrayOfActivities.get(position).nbLikes));
              holder.textViewTime.setText(mock.arrayOfActivities.get(position).postedTime/60 + "minutes ago");
            }
            else
            {
              holder.textViewShowMore.setVisibility(View.VISIBLE);
              
              LayoutParams params = convertView.getLayoutParams();
              params.height = 40;
              convertView.setLayoutParams(params);
              
              holder.imageViewAvatar.setVisibility(View.INVISIBLE);
              holder.textViewName.setVisibility(View.INVISIBLE);
              holder.textViewMessage.setVisibility(View.INVISIBLE);
              holder.buttonComment.setVisibility(View.INVISIBLE);
              holder.buttonLike.setVisibility(View.INVISIBLE);
              holder.textViewTime.setVisibility(View.INVISIBLE);
            }
            
            final int pos = position;
            convertView.setOnClickListener(new View.OnClickListener() {
              
              public void onClick(View v) {
            
                if(pos == mock.arrayOfActivities.size())
                {
                  Log.e("Show more", "No more activity");
                }
                else
                {
                  GDActivity.TYPE = 1;
                  
                  Intent next = new Intent(asyncImageViewListActivityInstance, ActivityStreamDisplay.class);
                  asyncImageViewListActivityInstance.startActivity(next);  
                }
              }
            });
            
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

    public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
    }

    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        if (getListView() == listView) {
            searchAsyncImageViews(listView, scrollState == OnScrollListener.SCROLL_STATE_FLING);
        }
    }

    private void searchAsyncImageViews(ViewGroup viewGroup, boolean pause) {
        final int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AsyncImageView image = (AsyncImageView) viewGroup.getChildAt(i).findViewById(R.id.imageView_Avatar);
            if (image != null) {
                image.setPaused(pause);
            }
        }
    }
    
    public void finishMe()
    {
      
      GDActivity.TYPE = 0;
//      
      Intent next = new Intent(asyncImageViewListActivityInstance, eXoApplicationsController2.class);
      startActivity(next);
      asyncImageViewListActivityInstance = null;
      
    }

}
