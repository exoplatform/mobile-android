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

import org.exoplatform.utils.Log;
import org.exoplatform.utils.Utils;
import org.exoplatform.widget.PicassoTextView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS
 * Author :  MinhTDH
 *           MinhTDH@exoplatform.com
 * Jul 31, 2015  
 */
public class PicassoImageGetter implements ImageGetter {

  private WeakReference<TextView> mTvRef;
  
  public PicassoImageGetter(TextView textView) {
    super();
    mTvRef = new WeakReference<TextView>(textView);
  }
  private static final int PLACEHODER_SIZE = 200;
  @Override
  public Drawable getDrawable(String paramString) {
    final DrawablePlaceHolder ret = new DrawablePlaceHolder();
    // TODO calculate and set place holder size;
    ret.setBounds(0, 0, PLACEHODER_SIZE, PLACEHODER_SIZE);
    TextView tv = Utils.getVal(mTvRef);
    if (tv != null) {
      PicassoGetterTarget target = new PicassoGetterTarget(tv, ret);
      if (tv instanceof PicassoTextView) {
        ((PicassoTextView) tv).addTarget(target);
      }
      ExoPicasso.picasso(tv.getContext())
                .load(paramString)
                // TODO calculate and set place holder size;
                .resize(PLACEHODER_SIZE, PLACEHODER_SIZE)
                .centerInside()
                .into(target);
    }
    return ret;
  }

  public static class PicassoGetterTarget implements Target {
    
    private WeakReference<TextView> mTvRef;
    private WeakReference<DrawablePlaceHolder> mDrawRef;

    public PicassoGetterTarget(TextView textView, DrawablePlaceHolder drawable) {
      super();
      mTvRef = new WeakReference<TextView>(textView);
      mDrawRef = new WeakReference<PicassoImageGetter.DrawablePlaceHolder>(drawable); 
    }
    
    @Override
    public void onBitmapFailed(Drawable arg0) {
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
    public void onPrepareLoad(Drawable arg0) {
    }
  }
  
  public static class DrawablePlaceHolder extends BitmapDrawable {

    Drawable drawable;

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
