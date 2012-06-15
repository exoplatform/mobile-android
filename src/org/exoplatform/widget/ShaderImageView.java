/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.widget;

import greendroid.widget.AsyncImageView;
import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jun
 * 8, 2012
 */
public class ShaderImageView extends AsyncImageView {

  private int   boderColor  = 0xFFFFFFFF;

  private int   layerColor  = 0x66666666;

  private int   shadowColor = 0x99999999;

  private Paint mPaint;

  public ShaderImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public ShaderImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ShaderImageView(Context context) {
    super(context);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    BitmapDrawable drawable = (BitmapDrawable) getDrawable();

    if (drawable == null) {
      return;
    }

    if (getWidth() == 0 || getHeight() == 0) {
      return;
    }

    Bitmap fullSizeBitmap = drawable.getBitmap();

    int scaledWidth = getMeasuredWidth();
    int scaledHeight = getMeasuredHeight();

    Bitmap mBitmap;
    if (scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight()) {
      mBitmap = fullSizeBitmap;
    } else {
      mBitmap = Bitmap.createScaledBitmap(fullSizeBitmap, scaledWidth, scaledHeight, true /* filter */);
    }

    /*
     * Draw bimap
     */
    int radius = 4;
    int padding = 2;
    int bleed = 2;
    RectF frame = new RectF(padding, padding, getWidth() - padding, getHeight() - padding);
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setColor(boderColor);
    canvas.drawRoundRect(frame, radius, radius, mPaint);

    padding = 3;
    frame = new RectF(padding, padding, getWidth() - padding, getHeight() - padding);
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setColor(layerColor);
    canvas.drawRoundRect(frame, radius, radius, mPaint);

    frame = new RectF(padding + 1, padding + 1, getWidth() - padding, getHeight() - padding);
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setColor(shadowColor);
    canvas.drawRoundRect(frame, radius, radius, mPaint);

    Shader bitmapShader = new BitmapShader(mBitmap, TileMode.CLAMP, TileMode.CLAMP);
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setColor(0xFF000000);
    mPaint.setMaskFilter(new BlurMaskFilter(bleed, Blur.INNER));
    mPaint.setShader(bitmapShader);
    canvas.drawRoundRect(frame, radius, radius, mPaint);
  }

  public void setBorderColor(int color) {
    boderColor = color;
  }

}
