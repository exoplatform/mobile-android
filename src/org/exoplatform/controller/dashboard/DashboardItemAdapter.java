package org.exoplatform.controller.dashboard;

import greendroid.image.ImageProcessor;
import greendroid.widget.AsyncImageView;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.exoplatform.R;
import org.exoplatform.model.DashBoardItem;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.ui.WebViewActivity;
import org.exoplatform.utils.ExoConstants;

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
import android.widget.Toast;

public class DashboardItemAdapter extends BaseAdapter implements ImageProcessor {

  private ArrayList<DashBoardItem>    _arrayOfItems;
  
  private final Paint    mPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final Rect     mRectSrc  = new Rect();

  private final Rect     mRectDest = new Rect();

  private Bitmap         mMask;

  private int            mThumbnailSize;

  private int            mThumbnailRadius;

  private LayoutInflater mInflater;

  private Context        mContext;

  public DashboardItemAdapter(Context context, ArrayList<DashBoardItem> items) {
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

  public View getView(int position, View convertView, ViewGroup parent) {

    final DashBoardItem item = _arrayOfItems.get(position);
    GadgetInfo inforGadget = item.gadget;
    
    if (item.strTabName != null) {

      convertView = mInflater.inflate(R.layout.gadget_tab_layout, parent, false);
      TextView textViewTabTitle = (TextView) convertView.findViewById(R.id.textView_Tab_Title);
      textViewTabTitle.setText(item.strTabName);
      
    } else {
      convertView = mInflater.inflate(R.layout.gadget_item_layout, parent, false);
      
      if(position + 1 < _arrayOfItems.size()) 
      {
      
        DashBoardItem nextItem = _arrayOfItems.get(position + 1);
        
        if(inforGadget.getGadgetIndex() == 0)
        {
          if(nextItem.strTabName != null)
            convertView.setBackgroundResource(R.drawable.dashboardcustombgforcellsingle);
          else
            convertView.setBackgroundResource(R.drawable.dashboardcustombgforcelltop);
        }
        else 
        {
          if(nextItem.strTabName != null)
             convertView.setBackgroundResource(R.drawable.dashboardcustombgforcellbottom);
          else
          {
            convertView.setBackgroundResource(R.drawable.dashboardcustombgforcellmiddle);
           
          }
        }
        
      }
      else
      {
        if(item.strTabName != null)
          convertView.setBackgroundResource(R.drawable.dashboardcustombgforcellbottom);
       else
         convertView.setBackgroundResource(R.drawable.dashboardcustombgforcellsingle);
      }

      AsyncImageView imageViewAvatar = (AsyncImageView) convertView.findViewById(R.id.gadget_image);
      imageViewAvatar.setUrl(item.gadget.getStrGadgetIcon());
      TextView textViewName = (TextView) convertView.findViewById(R.id.gadget_title);
      textViewName.setText(item.gadget.getGadgetName());

      TextView textViewMessage = (TextView) convertView.findViewById(R.id.gadget_content);
      textViewMessage.setText(item.gadget.getGadgetDescription());

    }

    convertView.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        if (item.strTabName == null)
          showGadget(item.gadget);
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

  public void showGadget(GadgetInfo gadget) {

//    ExoApplicationsController2.webViewMode = 0;
    DefaultHttpClient client = new DefaultHttpClient();

    HttpGet get = new HttpGet(gadget.getGadgetUrl());
    try {
      HttpResponse response = client.execute(get);
      int status = response.getStatusLine().getStatusCode();
      if (status < 200 || status >= 300) {
        Toast.makeText(mContext, "Connection timed out", Toast.LENGTH_LONG).show();
        return;
      }
    } catch (Exception e) {

      return;
    }

    WebViewActivity._titlebar = gadget.getGadgetName();
    WebViewActivity._url = gadget.getGadgetUrl();

    Intent next = new Intent(mContext, WebViewActivity.class);
    next.putExtra(ExoConstants.WEB_VIEW_TYPE, 0);
    mContext.startActivity(next);
    //((Activity) mContext).finish();

  }

}
