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
package org.exoplatform.controller.dashboard;

import greendroid.image.ImageProcessor;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.ui.WebViewActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ShaderImageView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DashboardItemAdapter extends BaseAdapter implements ImageProcessor {

  private ArrayList<GadgetInfo> _arrayOfItems;

  private final Paint           mPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final Rect            mRectSrc  = new Rect();

  private final Rect            mRectDest = new Rect();

  private Bitmap                mMask;

  private int                   mThumbnailSize;

  private int                   mThumbnailRadius;

  private LayoutInflater        mInflater;

  private Context               mContext;

  public DashboardItemAdapter(Context context, ArrayList<GadgetInfo> items) {
    mContext = context;
    mInflater = LayoutInflater.from(context);

    mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

    mThumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
    mThumbnailRadius = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_radius);

    _arrayOfItems = items;

    prepareMask();
  }

  private void prepareMask() {
    mMask = Bitmap.createBitmap(mThumbnailSize, mThumbnailSize, Bitmap.Config.ARGB_8888);

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(Color.RED);
    paint.setStyle(Paint.Style.FILL_AND_STROKE);

    Canvas c = new Canvas(mMask);
    c.drawRoundRect(new RectF(0, 0, mThumbnailSize, mThumbnailSize),
                    mThumbnailRadius,
                    mThumbnailRadius,
                    paint);
  }

  public int getCount() {

    return _arrayOfItems.size();
  }

  public Object getItem(int position) {
    return null;
  }

  public long getItemId(int position) {
    return position;
  }
  
  private static final int VIEW_TYPE_TAB = 0;
  private static final int VIEW_TYPE_ITEM = 1;
  @Override
  public int getViewTypeCount() {
    // for tab and item layout
    return 2;
  }
  
  @Override
  public int getItemViewType(int position) {
    int type = VIEW_TYPE_ITEM;
    final GadgetInfo inforGadget = _arrayOfItems.get(position);

    if (inforGadget.getTabName() != null) {
      type = VIEW_TYPE_TAB;
    } else {
      type = VIEW_TYPE_ITEM;
    }
    return type;
  }
  
  public View getView(int position, View convertView, ViewGroup parent) {

    final GadgetInfo inforGadget = _arrayOfItems.get(position);
    final int viewType = getItemViewType(position);
    if (viewType == VIEW_TYPE_TAB) {
      TabHolder holder;
      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.gadget_tab_layout, parent, false);
        holder = new TabHolder();
        holder.textViewTabTitle = (TextView) convertView.findViewById(R.id.textView_Tab_Title);
        convertView.setTag(holder);
      } else {
        holder = (TabHolder) convertView.getTag();
      } 
      holder.textViewTabTitle.setText(inforGadget.getTabName());

    } else {
      ItemHolder holder;
      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.gadget_item_layout, parent, false);
        holder = new ItemHolder();
        holder.imageViewAvatar = (ShaderImageView) convertView.findViewById(R.id.gadget_image);
        holder.textViewName = (TextView) convertView.findViewById(R.id.gadget_title);
        holder.textViewMessage = (TextView) convertView.findViewById(R.id.gadget_content);
        convertView.setTag(holder);
      } else {
        holder = (ItemHolder) convertView.getTag();
      } 
      if (position + 1 < _arrayOfItems.size()) {
        GadgetInfo nextItem = _arrayOfItems.get(position + 1);

        if (inforGadget.getGadgetIndex() == 0) {
          if (nextItem.getTabName() != null)
            convertView.setBackgroundResource(R.drawable.dashboard_single_background_shape);
          else
            convertView.setBackgroundResource(R.drawable.dashboard_top_background_shape);
        } else {
          if (nextItem.getTabName() != null)
            convertView.setBackgroundResource(R.drawable.dasboard_bottom_background_shape);
          else {
            convertView.setBackgroundResource(R.drawable.dashboard_middle_background_shape);

          }
        }

      } else {
        GadgetInfo previousItem = _arrayOfItems.get(position - 1);
        if (previousItem.getTabName() == null)
          convertView.setBackgroundResource(R.drawable.dasboard_bottom_background_shape);
        else
          convertView.setBackgroundResource(R.drawable.dashboard_single_background_shape);
      }

      holder.imageViewAvatar.setDefaultImageResource(R.drawable.gadgetplaceholder);
      holder.imageViewAvatar.setUrl(inforGadget.getStrGadgetIcon());
      
      holder.textViewName.setText(inforGadget.getGadgetName());

      holder.textViewMessage.setText(inforGadget.getGadgetDescription());

    }

    convertView.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        if (inforGadget.getTabName() == null)
          showGadget(inforGadget);
      }
    });

    return convertView;
  }

  static class TabHolder {
    TextView textViewTabTitle;
  }
  static class ItemHolder {
    ShaderImageView imageViewAvatar;
    TextView textViewName, textViewMessage;
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

  public void showGadget(GadgetInfo gadget) {
    String gadgetUrl = gadget.getGadgetUrl();
    Intent intent = new Intent(mContext, WebViewActivity.class);
    intent.putExtra(ExoConstants.WEB_VIEW_URL, gadgetUrl);
    intent.putExtra(ExoConstants.WEB_VIEW_TITLE, gadget.getGadgetName());
    intent.putExtra(ExoConstants.WEB_VIEW_ALLOW_JS, "true");
    mContext.startActivity(intent);

  }

}
