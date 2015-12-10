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

import org.exoplatform.R;

import com.squareup.picasso.Transformation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Created by The eXo Platform SAS May 14, 2015
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 */
public class RoundedCornersTranformer implements Transformation {

  Context mContext;

  public RoundedCornersTranformer(Context ctx) {
    this.mContext = ctx;
  }

  @Override
  public String key() {
    return "exo_rounded_corners()";
  }

  @Override
  public Bitmap transform(Bitmap source) {
    // Start a new bitmap with the same dimensions and config as the source
    Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
    // Create a shader based on the source bitmap
    BitmapShader shader;
    shader = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    // Create a paint that uses the shader (bitmap) as texture
    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setShader(shader);
    // Create a frame with a padding of 2 inside the source bitmap dimensions
    int padding = 2;
    RectF frame = new RectF(padding, padding, source.getWidth() - padding, source.getHeight() - padding);
    // Create a new canvas to draw on the new bitmap
    Canvas canvas = new Canvas(result);
    // Draw the rounded rect on the canvas, with the original bitmap (the paint)
    int radius = mContext.getResources().getDimensionPixelSize(R.dimen.image_radius);
    canvas.drawRoundRect(frame, radius, radius, paint);
    // Finish
    source.recycle();
    return result;
  }

}
