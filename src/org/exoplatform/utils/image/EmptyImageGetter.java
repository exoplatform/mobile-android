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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;

/**
 * An ImageGetter that replaces every {@code <img>} tag by an empty drawable.<br/>
 * Usage:
 * 
 * <pre>
 * EmptyImageGetter emptyImage = new EmptyImageGetter(context);
 * Html.fromHtml(anHtmlString, emptyImage, null);
 * </pre>
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since May 20, 2015
 */
public class EmptyImageGetter implements ImageGetter {

  private Context context;

  public EmptyImageGetter(Context ctx) {
    this.context = ctx;
  }

  @Override
  public Drawable getDrawable(String src) {
    return context.getResources().getDrawable(R.drawable.empty_drawable);
  }

}
