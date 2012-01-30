package org.exoplatform.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;

public class PhotoUtils {
  private static final String[] suffix  = { ".jpeg", ".jpg", ".png", ".bmp", ".gif" };

  private static final String   dotSign = ".";

  public static boolean isImages(File file) {

    String name = file.getName();
    for (int i = 0; i < suffix.length; i++) {
      if (name.endsWith(suffix[i]))
        return true;
    }
    return false;
  }

  public static boolean isImages(String fileName) {
    for (int i = 0; i < suffix.length; i++) {
      if (fileName.endsWith(suffix[i]))
        return true;
    }
    return false;
  }

  public static String getExtension(String name) {
    int index = name.indexOf(dotSign);
    String extension = name.substring(index + 1, name.length());
    return extension;
  }

  public static String getFileName(String name) {
    int index = name.indexOf(dotSign);
    String fileName = name.substring(0, index);
    return fileName;
  }

  private static String getDateFormat() {
    String dateFormat = null;
    Calendar cal = Calendar.getInstance();
    long minus = cal.getTimeInMillis();
    String inFormat = new String("yyyy_MM_dd_hh_mm_ss");
    dateFormat = (String) DateFormat.format(inFormat, minus);
    return dateFormat;
  }

  public static String getDateFromString(String value) {
    String dateFormat = null;
    if (value != null) {
      long minus = Long.valueOf(value);
      String inFormat = new String("dd/MM/yyyy hh:mm");
      dateFormat = (String) DateFormat.format(inFormat, minus);
    }

    return dateFormat;
  }

  public static String getImageFileName() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("MobileImage_");
    buffer.append(getDateFormat());
    buffer.append(".png");
    return buffer.toString();
  }

  public static Bitmap shrinkBitmap(String file, int width, int height) {

    BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
    bmpFactoryOptions.inJustDecodeBounds = true;
    Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

    int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
    int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);

    if (heightRatio > 1 || widthRatio > 1) {
      if (heightRatio > widthRatio) {
        bmpFactoryOptions.inSampleSize = heightRatio;
      } else {
        bmpFactoryOptions.inSampleSize = widthRatio;
      }
    }

    bmpFactoryOptions.inJustDecodeBounds = false;
    bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
    return bitmap;
  }

  public static Bitmap resizeImage(Bitmap originalImage, int newW) {

    Bitmap resizedBitmap = null;

    int width = originalImage.getWidth();

    if (width >= newW) {

      int height = originalImage.getHeight();

      float scaleWidth = ((float) newW) / width;

      float scaleHeight = scaleWidth;

      Matrix matrix = new Matrix();

      matrix.postScale(scaleWidth, scaleHeight);

      resizedBitmap = Bitmap.createBitmap(originalImage, 0, 0,

      width, height, matrix, true);

    } else
      return originalImage;
    return resizedBitmap;

  }

  public static File reziseFileImage(File file) {
    try {
      String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
      Bitmap bitmap = shrinkBitmap(file.getPath(), 1024, 860);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      bitmap.compress(CompressFormat.PNG, 100, output);
      File tempFile = new File(parentPath + "temfile.png");
      FileOutputStream out = new FileOutputStream(tempFile);
      output.writeTo(out);
      return tempFile;
    } catch (Exception e) {
      return null;
    }

  }

  public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
    try {

      bitmap = resizeImage(bitmap, 100);
      int width = bitmap.getWidth();
      if (width > 100) {
        width = 100;
      }
      int heigth = bitmap.getHeight();
      if (heigth > 100) {
        heigth = 100;
      }

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
      bitmap.recycle();
      return output;
    } catch (OutOfMemoryError e) {
      return null;
    }
  }

  /*
   * This method for getting the image url which return from Intent.ACTION_PICK
   */
  public static String extractFilenameFromUri(Uri uri, Activity activity) {

    String filePath = null;
    String[] projection = { MediaStore.Images.ImageColumns.DATA /* col1 */};

    Cursor c = activity.managedQuery(uri, projection, null, null, null);
    if (c != null && c.moveToFirst()) {
      filePath = c.getString(0);
    }
    return filePath;
  }

  /*
   * Resize the image bitmap with the maximum size is 1/4 of the screen width
   */
  public static Bitmap resizeImageBitmap(Context context, Bitmap bm) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    int widthScreen = displayMetrics.widthPixels;
    // the fixed size is 1/4 the screen width
    int fixedSize = widthScreen / 4;
    // the minimum size is 1/9 the screen width
    int minSize = widthScreen / 9;

    // get the real size of image
    int scaledWidth = bm.getWidth();
    int scaledHeight = bm.getHeight();
    
    int height = 0;
    int width = 0;
    // set the minimum size
    if (scaledHeight <= minSize) {
      scaledHeight = minSize;
    }
    if (scaledWidth <= minSize) {
      scaledWidth = minSize;
    }

    // resize the image
    if (scaledWidth > scaledHeight) {
      width = fixedSize;
      height = (width * scaledHeight) / scaledWidth;
    } else if (scaledWidth < scaledHeight) {
      height = fixedSize;
      width = (height * scaledWidth) / scaledHeight;
    } else {
      width = height = fixedSize;
    }

    return Bitmap.createScaledBitmap(bm, width, height, true /* filter */);
  }
}
