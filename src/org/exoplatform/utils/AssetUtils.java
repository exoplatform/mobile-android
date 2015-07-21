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
package org.exoplatform.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

/**
 * manage assets: typeface
 */
public class AssetUtils {

  private static AssetManager mAssetManager;

  /* simple caching of typefaces */
  private static HashMap<String, Typeface> mFontMaps;

  public static final String ROBOTO_BOLD    = "typefaces/Roboto-Bold.ttf";

  public static final String ROBOTO_REGULAR = "typefaces/Roboto-Regular.ttf";

  public static final String ROBOTO_MEDIUM  = "typefaces/Roboto-Medium.ttf";

  public static final String ROBOTO_BLACK   = "typefaces/Roboto-Black.ttf";

  public static void setContext(Context context) {
    mAssetManager = context.getAssets();
    mFontMaps     = new HashMap<String, Typeface>();
  }

  /**
   * Get custom typeface from assets
   *
   * @param assetsPath
   * @return
   */
  public static Typeface getCustomTypeface(String assetsPath) {
    if (mFontMaps.get(assetsPath) == null) {
      mFontMaps.put(assetsPath, Typeface.createFromAsset(mAssetManager, assetsPath));
    }
    return mFontMaps.get(assetsPath);
  }

  /**
   * Set typeface for a viewgroup
   *
   * @param typeFace
   * @param parent
   */
  public static void setTypeFace(Typeface typeFace, ViewGroup parent){
    for (int i = 0; i < parent.getChildCount(); i++) {
      View v = parent.getChildAt(i);
      if (v instanceof ViewGroup) setTypeFace(typeFace, (ViewGroup) v);
      else if (v instanceof TextView)
        setTypeFace(typeFace, (TextView) v);
    }
  }

  /**
   * Set typeface for a TextView
   *
   * @param typeFace
   * @param view
   */
  public static void setTypeFace(Typeface typeFace, TextView view) {
    view.setTypeface(typeFace);
    view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
  }

}
