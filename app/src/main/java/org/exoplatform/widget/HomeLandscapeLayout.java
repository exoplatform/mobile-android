/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.widget;

import org.exoplatform.utils.PhotoUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jun
 * 8, 2012
 */
public class HomeLandscapeLayout extends LinearLayout {
  private Paint paint = new Paint();

  public HomeLandscapeLayout(Context context) {
    super(context, null);
  }

  public HomeLandscapeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int scaledWidth = getMeasuredWidth();
    int scaledHeight = getMeasuredHeight();
    int itemWidth = scaledWidth / 3;
    paint.setStyle(Style.STROKE);
    paint.setDither(true);
    paint.setAntiAlias(true);
    /*
     * Make radius gradient bitmap with double height
     */
    Bitmap bm = PhotoUtils.makeRadGrad(scaledWidth, scaledHeight * 2);
    /*
     * Draw the bitmap at left = -4 and top = -1/2 of scaleHeight
     */
    canvas.drawBitmap(bm, -4, -scaledHeight / 2, paint);

    /*
     * Draw the first line separator
     */
    paint.setColor(Color.BLACK);
    canvas.drawLine(itemWidth, 0, itemWidth, scaledHeight, paint);
    paint.setColor(Color.rgb(51, 51, 51));
    canvas.drawLine(itemWidth + 1, 0, itemWidth + 1, scaledHeight, paint);
    /*
     * Draw the second line separator
     */
    paint.setColor(Color.BLACK);
    itemWidth = itemWidth * 2;
    canvas.drawLine(itemWidth, 0, itemWidth, scaledHeight, paint);
    paint.setColor(Color.rgb(51, 51, 51));
    canvas.drawLine(itemWidth + 1, 0, itemWidth + 1, scaledHeight, paint);

  }

}
