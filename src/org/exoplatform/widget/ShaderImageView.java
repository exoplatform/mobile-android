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

import android.graphics.*;
//import greendroid.widget.AsyncImageView;

import org.exoplatform.R;

import android.content.Context;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import org.exoplatform.utils.AsyncImageView;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jun
 * 8, 2012
 */
public class ShaderImageView extends AsyncImageView {

  private static final int LAYER_COLOR  = 0x66000000;

  private static final int SHADOW_COLOR = 0x22000000;

  private int              mBorderColor = 0xFFFFFFFF;

  private Paint            mPaint       = new Paint(Paint.ANTI_ALIAS_FLAG);

  private boolean          slideLeft    = false;

  private BlurMaskFilter   mBlurMaskFilter = new BlurMaskFilter(2, Blur.INNER);

  private static final String TAG = "eXo____ShaderImageView____";

  public ShaderImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public ShaderImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ShaderImageView(Context context, boolean slideLeft) {
    super(context);
    this.slideLeft = slideLeft;
  }
 
  private RectF getNewFrame(float left, float top, float right, float bottom) {
    return new RectF(left, top, right, bottom);
  }
    
  private Bitmap getScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
    return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, filter);
  }
  
  private BitmapShader createBitmapShader(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
    return new BitmapShader(bitmap, tileX, tileY);
  }
    

  @Override
  protected void onDraw(Canvas canvas) {
    BitmapDrawable drawable = (BitmapDrawable) getDrawable();
    if (drawable == null) return;
    if (getWidth() == 0 || getHeight() == 0) return;
    Bitmap fullSizeBitmap = drawable.getBitmap();

    int scaledWidth = getMeasuredWidth();
    int scaledHeight = getMeasuredHeight();

    /* scale the bitmap if necessary */
    Bitmap scaledBitmap;
    if (scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight()) {
      scaledBitmap = fullSizeBitmap;
    } else {
      scaledBitmap = getScaledBitmap(fullSizeBitmap, scaledWidth, scaledHeight, true );
    }

    int radius = getContext().getResources().getDimensionPixelSize(R.dimen.image_radius);
    int padding = 2;
    RectF frame = getNewFrame(padding, padding, getWidth() - padding, getHeight() - padding);

    /* Draw the border background - white */
    mPaint.setColor(mBorderColor);
    canvas.drawRoundRect(frame, radius, radius, mPaint);

    /* Draw the border frame - transparent rounded rectangle */
    mPaint.setColor(LAYER_COLOR);
    canvas.drawRoundRect(frame, radius, radius, mPaint);

    /* Draw the image bitmap with bitmap shader inside the frame */
    mPaint.setColor(mBorderColor);
    mPaint.setMaskFilter(mBlurMaskFilter);
    mPaint.setShader(createBitmapShader(scaledBitmap, TileMode.CLAMP, TileMode.CLAMP));
    canvas.drawRoundRect(frame, radius, radius, mPaint);

    /* Draw shadow for left and top edge */
    // comment out this to avoid bizarre bug on api >= 17 - no blurry edge effect
    /**
    mPaint.setColor(SHADOW_COLOR);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Join.ROUND);
    mPaint.setStrokeCap(Cap.ROUND);
    canvas.drawLine(padding + 1, padding + 1, getWidth() - padding, padding + 1, mPaint);
    canvas.drawLine(getWidth() - padding,
                    padding + 1,
                    getWidth() - padding,
                    padding + radius,
                    mPaint);
    canvas.drawLine(padding + 1, padding + 1, padding + 1, getHeight() - padding, mPaint);
    canvas.drawLine(padding + 1,
                    getHeight() - padding,
                    padding + radius,
                    getHeight() - padding,
                    mPaint);
    **/
  }

  public void setBorderColor(int color) {
    mBorderColor = color;
  }

  @Override
  public void setImageBitmap(Bitmap bm) {
    if (bm != null) {
      if (slideLeft)
        this.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_left));
      else
        this.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.gd_grow_from_bottom));
    }
    super.setImageBitmap(bm);
  }

  @Override
  public void setImageDrawable(Drawable drawable) {
    super.setImageDrawable(drawable);
    if (slideLeft)
      this.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_left));
  }

}
