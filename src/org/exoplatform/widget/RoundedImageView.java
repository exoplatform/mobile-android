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

    mScaledBitmap = PhotoUltils.resizeImage(mScaledBitmap, 200);

    Bitmap roundBitmap = getRoundedCornerBitmap(mScaledBitmap, 5);

    canvas.drawBitmap(roundBitmap, 0, 0, null);

  }

  public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
    int width = bitmap.getWidth();
    int heigth = bitmap.getHeight();
    Bitmap output = Bitmap.createBitmap(width, heigth, Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final int color = 0xff424242;
    final Paint paint = new Paint();
    final Rect rect = new Rect(0, 0, width, heigth);
    final RectF rectF = new RectF(rect);
    final float roundPx = pixels;

    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(color);
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    canvas.drawBitmap(bitmap, rect, rect, paint);

    return output;
  }

}
