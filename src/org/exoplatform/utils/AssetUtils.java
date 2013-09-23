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

  private static Context mContext;

  private static AssetManager mAssetManager;

  /* simple caching of typefaces */
  private static HashMap<String, Typeface> mFontMaps;

  public static final String ROBOTO_BOLD    = "typefaces/Roboto-Bold.ttf";

  public static final String ROBOTO_REGULAR = "typefaces/Roboto-Regular.ttf";

  public static final String ROBOTO_MEDIUM  = "typefaces/Roboto-Medium.ttf";

  public static final String ROBOTO_BLACK   = "typefaces/Roboto-Black.ttf";

  public static void setContext(Context context) {
    mContext      = context;
    mAssetManager = mContext.getAssets();
    mFontMaps     = new HashMap<String, Typeface>();
  }

  /**
   * Get custom typeface from assets
   *
   * @param assetsPath
   * @return
   */
  public static Typeface getCustomTypeface(String assetsPath) {
    if (mFontMaps.get(assetsPath) == null)
      mFontMaps.put(assetsPath, Typeface.createFromAsset(mAssetManager, assetsPath));
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
