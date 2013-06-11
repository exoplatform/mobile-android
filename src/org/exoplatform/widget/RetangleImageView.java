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

import org.exoplatform.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jan
 * 16, 2012 This image view for attached image view in compose message activity
 */
public class RetangleImageView extends ImageView {
  
  public RetangleImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);    
  }

  public RetangleImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public RetangleImageView(Context context) {
    super(context);
  }

  private Bitmap _bitmap = null;
  
  private Bitmap getCachedBitmap(int width, int height) {
    if ((_bitmap != null) && (_bitmap.getWidth() == width) && (_bitmap.getHeight() == height)) return _bitmap; 
    
    _bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    return _bitmap;
  }
  
  private Canvas _canvasT = null;
  
  private Canvas getCachedCanvas(int width, int height) {
    if (_canvasT != null) return _canvasT;
    Log.i("RetangleImageView", "create new canvas");

    _canvasT = new Canvas(getCachedBitmap(width, height));
    return _canvasT;
  }
  
  private Canvas getDrawnCanvas(int width, int height, Bitmap fullSizeBitmap) {
    Canvas canvas = getCachedCanvas(width, height);
    Paint p = new Paint();
    p.setColor(Color.TRANSPARENT);
    // Draw in the original image with transparent
    canvas.drawBitmap(fullSizeBitmap, 0, 0, p);
    return canvas;
  }
  
  private NinePatchDrawable _nicepatchDrawable = null;
  
  private NinePatchDrawable getCachedNinePatchDrawable(int width, int height) {
    if ((_nicepatchDrawable != null) 
      && (_nicepatchDrawable.getBounds().height() == height) 
      && (_nicepatchDrawable.getBounds().width() == width)) 
      return _nicepatchDrawable; 
    
    // Get the image bitmap frame
    Bitmap maskBm = BitmapFactory.decodeResource(getResources(),
                                                 R.drawable.social_attached_image_border);
    // Create nine patch drawable from image bitmap frame
    byte[] chunk = maskBm.getNinePatchChunk();
    _nicepatchDrawable = new NinePatchDrawable(maskBm, chunk, new Rect(), null);
    _nicepatchDrawable.setBounds(0, 0, width, height);
    return _nicepatchDrawable;
  }
  
  @Override
  protected void onDraw(Canvas canvas) {
    try {
      // get the original bitmap drawable
      BitmapDrawable drawable = (BitmapDrawable) getDrawable();

      if (drawable == null || getWidth() == 0 || getHeight() == 0) {
        return;
      }
      Bitmap fullSizeBitmap = drawable.getBitmap();

      int height = fullSizeBitmap.getHeight();
      int width = fullSizeBitmap.getWidth();
      
      Canvas canvasT = getDrawnCanvas(width, height, fullSizeBitmap);
      
      // Draw this bitmap to the image view
      canvas.drawBitmap(fullSizeBitmap, 0, 0, null);

      NinePatchDrawable nicepatchDrawable = getCachedNinePatchDrawable(width, height);
      
      // Draw the NinePatchDrawable to a new output_bitmap
      Bitmap output_bitmap = getCachedBitmap(width, height);

      canvasT = getCachedCanvas(width, height);

      nicepatchDrawable.draw(canvasT);
      // Draw the output_bitmap to image view
      canvas.drawBitmap(output_bitmap, 0, 0, null);
    } catch (OutOfMemoryError e) {
      if (greendroid.util.Config.GD_ERROR_LOGS_ENABLED)
        Log.e("RetangleImageView", e.getMessage());
    }
  }

}
