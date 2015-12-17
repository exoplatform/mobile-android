/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.utils.image;

import java.lang.ref.WeakReference;

import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoWebAddress;
import org.exoplatform.utils.ExoWebAddress.ParseException;
import org.exoplatform.utils.Log;
import org.exoplatform.utils.Utils;
import org.exoplatform.widget.PicassoTextView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.webkit.URLUtil;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS
 * 
 * @author MinhTDH MinhTDH@exoplatform.com Jul 31, 2015
 */
public class PicassoImageGetter implements ImageGetter {

  private WeakReference<TextView> mTvRef;

  public PicassoImageGetter(TextView textView) {
    super();
    mTvRef = new WeakReference<TextView>(textView);
  }

  private static final int PLACEHOLDER_SIZE = 300;

  @Override
  public Drawable getDrawable(String paramString) {
    final DrawablePlaceHolder ret = new DrawablePlaceHolder();
    ret.setBounds(0, 0, PLACEHOLDER_SIZE, PLACEHOLDER_SIZE);
    TextView tv = Utils.getVal(mTvRef);
    if (tv != null) {
      PicassoGetterTarget target = new PicassoGetterTarget(tv, ret);
      if (tv instanceof PicassoTextView) {
        ((PicassoTextView) tv).addTarget(target);
      }
      String imageUrl = paramString;
      // Check the image URL format
      ExoWebAddress imageAddr = null;
      try {
        imageAddr = new ExoWebAddress(imageUrl);
      } catch (ParseException e) {
        if (Log.LOGI)
          Log.i(getClass().getName(), e.getMessage());
      }
      if (imageAddr == null || imageAddr.isRelativeURL()) {
        // relative URL => prefix with current server URL
        imageUrl = AccountSetting.getInstance().getDomainName() + imageUrl;
      }
      // add http protocol if the URL has none
      if (!URLUtil.isNetworkUrl(imageUrl))
        imageUrl = ExoConnectionUtils.HTTP + imageUrl;
      // load the image
      ExoPicasso.picasso(tv.getContext()).load(imageUrl).resize(PLACEHOLDER_SIZE, PLACEHOLDER_SIZE).centerInside().into(target);
    }
    // TODO add loading placeholder
    return ret;
  }

  public static class PicassoGetterTarget implements Target {

    private WeakReference<TextView>            mTvRef;

    private WeakReference<DrawablePlaceHolder> mDrawRef;

    public PicassoGetterTarget(TextView textView, DrawablePlaceHolder drawable) {
      super();
      mTvRef = new WeakReference<TextView>(textView);
      mDrawRef = new WeakReference<PicassoImageGetter.DrawablePlaceHolder>(drawable);
    }

    @Override
    public void onBitmapFailed(Drawable arg0) {
      if (Log.LOGD)
        Log.d(this.getClass().getName(), "Image could not be loaded");
    }

    @Override
    public void onBitmapLoaded(Bitmap bm, LoadedFrom arg1) {
      TextView tv = Utils.getVal(mTvRef);
      if (tv != null) {
        DrawablePlaceHolder drawable = Utils.getVal(mDrawRef);
        if (drawable != null) {
          BitmapDrawable bd = new BitmapDrawable(tv.getResources(), bm);
          bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
          drawable.drawable = bd;
          drawable.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
          tv.setText(tv.getText());
        }
      }
    }

    @Override
    public void onPrepareLoad(Drawable placeholder) {
    }
  }

  public static class DrawablePlaceHolder extends BitmapDrawable {

    private Drawable drawable;

    @Override
    public void draw(final Canvas canvas) {
      if (drawable != null) {
        drawable.draw(canvas);
      }
    }

    public void setDrawable(Drawable drawable) {
      this.drawable = drawable;
    }

  }
}
