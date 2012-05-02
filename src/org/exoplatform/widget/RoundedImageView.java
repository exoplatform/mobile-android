package org.exoplatform.widget;

import greendroid.util.Config;
import greendroid.widget.AsyncImageView;

import org.exoplatform.utils.PhotoUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;

public class RoundedImageView extends AsyncImageView {

  public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public RoundedImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public RoundedImageView(Context context) {
    super(context);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    try {

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

      Bitmap mScaledBitmap;
      if (scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight()) {
        mScaledBitmap = fullSizeBitmap;
      } else {
        mScaledBitmap = Bitmap.createScaledBitmap(fullSizeBitmap, scaledWidth, scaledHeight, true /* filter */);
      }
      mScaledBitmap = PhotoUtils.getRoundedCornerBitmap(mScaledBitmap, 5);
      if (mScaledBitmap != null) {
        canvas.drawBitmap(mScaledBitmap, 0, 0, null);
      }
    } catch (OutOfMemoryError e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("RoundedImageView", e.getMessage());
    }
  }

}