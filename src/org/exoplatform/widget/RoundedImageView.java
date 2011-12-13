package org.exoplatform.widget;

import org.exoplatform.utils.PhotoUltils;

import greendroid.widget.AsyncImageView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

    Bitmap roundBitmap = PhotoUltils.getRoundedCornerBitmap(mScaledBitmap, 5);

    canvas.drawBitmap(roundBitmap, 0, 0, null);

  }

}
