package org.exoplatform.widget;

import greendroid.widget.AsyncImageView;

import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.utils.PhotoUltils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

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
      mScaledBitmap = PhotoUltils.getRoundedCornerBitmap(mScaledBitmap, 5);
      if (mScaledBitmap != null) {
        canvas.drawBitmap(mScaledBitmap, 0, 0, null);
      }
    } catch (OutOfMemoryError e) {
      SocialDetailHelper.getInstance().imageDownloader.clearCache();
    }
  }

}
