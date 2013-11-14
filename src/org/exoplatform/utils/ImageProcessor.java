package org.exoplatform.utils;


import android.graphics.Bitmap;

/**
 * An interface specifying a way to process an image prior storing it in the
 * application-wide cache. A great way to use this interface is to prepare a
 * Bitmap (resizing, adding rounded corners, changing the tint color, etc.) for
 * faster drawing.
 *
 * @author Cyril Mottier
 */
public interface ImageProcessor {

  /**
   * Called whenever the bitmap need to be processed. The returned may have
   * been modified or completely different.
   *
   * @param bitmap The Bitmap to process
   * @return A Bitmap that has been modified
   */
  Bitmap processImage(Bitmap bitmap);
}
