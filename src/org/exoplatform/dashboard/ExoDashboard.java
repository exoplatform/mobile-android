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
package org.exoplatform.dashboard;

import greendroid.image.ImageProcessor;
import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.exoplatform.controller.ExoApplicationsController2;
import org.exoplatform.model.GateInDbItem;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyListActivity;

import com.cyrilmottier.android.greendroid.R;

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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ExoDashboard extends MyListActivity {

  public static ExoDashboard       eXoDashboardInstance;

  public static List<GateInDbItem> arrGadgets;

  class DashBoardItem {
    String    strTabName;

    ExoGadget gadget;

    public DashBoardItem(String _strTabName, ExoGadget _gadget) {
      this.strTabName = _strTabName;
      this.gadget = _gadget;
    }

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    eXoDashboardInstance = this;

    setTitle("Dashboard");

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    CookieSyncManager.createInstance(this);

    arrGadgets = ExoApplicationsController2.eXoApplicationsController2Instance.arrGadgets;
    List<DashBoardItem> items = new ArrayList<DashBoardItem>();

    for (int i = 0; i < arrGadgets.size(); i++) {
      GateInDbItem gadgetTab = arrGadgets.get(i);

      items.add(new DashBoardItem(gadgetTab._strDbItemName, null));
      for (int j = 0; j < gadgetTab._arrGadgetsInItem.size(); j++) {
        ExoGadget gadget = gadgetTab._arrGadgetsInItem.get(j);
        items.add(new DashBoardItem(null, gadget));
      }

    }

    // final ItemAdapter adapter = new ItemAdapter(this, items);
    //
    setListAdapter(new MyItemAdapter(this, items));
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      finish();
      break;
    case 0:

      break;

    default:

    }
    return true;

  }

  @Override
  public void onBackPressed() {
    // TODO Auto-generated method stub
    super.onBackPressed();
    finish();
  }

  public void showGadget(ExoGadget gadget) {

    ExoApplicationsController2.webViewMode = 0;
    DefaultHttpClient client = new DefaultHttpClient();

    HttpGet get = new HttpGet(gadget._strGadgetUrl);
    try {
      HttpResponse response = client.execute(get);
      int status = response.getStatusLine().getStatusCode();
      if (status < 200 || status >= 300) {
        Toast.makeText(this, "Connection timed out", Toast.LENGTH_LONG).show();
        return;
      }
    } catch (Exception e) {

      return;
    }

    ExoWebViewController._titlebar = gadget._strGadgetName;
    ExoWebViewController._url = gadget._strGadgetUrl;

    Intent next = new Intent(this, ExoWebViewController.class);
    next.putExtra(ExoConstants.WEB_VIEW_TYPE, 0);
    startActivity(next);
    finish();

  }

  private class MyItemAdapter extends BaseAdapter implements ImageProcessor {

    List<DashBoardItem>    _arrayOfItems;

    private final Paint    mPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Rect     mRectSrc  = new Rect();

    private final Rect     mRectDest = new Rect();

    // private final String mImageForPosition;

    private Bitmap         mMask;

    private int            mThumbnailSize;

    private int            mThumbnailRadius;

    private LayoutInflater mInflater;

    public MyItemAdapter(Context context, List<DashBoardItem> items) {
      mInflater = LayoutInflater.from(context);

      _arrayOfItems = items;

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

      // if (convertView == null) {
      if (item.strTabName != null) {
        convertView = mInflater.inflate(R.layout.activityheadersection, parent, false);
        TextView title = (TextView) convertView.findViewById(R.id.textView_Section_Title);
        title.setText(item.strTabName);
      } else {

        convertView = mInflater.inflate(R.layout.activitydisplayviewcell, parent, false);

        AsyncImageView imageViewAvatar = (AsyncImageView) convertView.findViewById(R.id.imageView_Avatar);
        imageViewAvatar.setUrl(item.gadget._strGadgetIcon);
        // imageViewAvatar.setImageBitmap(item.gadget._btmGadgetIcon);
        TextView textViewName = (TextView) convertView.findViewById(R.id.textView_Name);
        textViewName.setText(item.gadget._strGadgetName);

        TextView textViewMessage = (TextView) convertView.findViewById(R.id.textView_Message);
        textViewMessage.setText(item.gadget._strGadgetDescription);

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

  }

  public void finishMe() {

    Intent next = new Intent(ExoDashboard.this, ExoApplicationsController2.class);
    startActivity(next);

    eXoDashboardInstance = null;
    // GDActivity.TYPE = 0;
  }

}
