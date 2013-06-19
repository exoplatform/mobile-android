package org.exoplatform.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.exoplatform.utils.image.FileCache;

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
import android.graphics.RadialGradient;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;

public class PhotoUtils {
  private static final String[] suffix         = { ".jpeg", ".jpg", ".png", ".bmp", ".gif" };

  private static final String   dotSign        = ".";

  private static final int      IMAGE_WIDTH    = 1024;

  private static final int      IMAGE_HEIGH    = 860;

  private static final int      IMAGE_QUALITY  = 100;

  private static final String   TEMP_FILE_NAME = "temfile.png";

  private static final String   MOBILE_IMAGE   = "MobileImage_";

  public static String getParentImagePath(Context context) {
    FileCache cache = new FileCache(context, ExoConstants.DOCUMENT_FILE_CACHE);
    return cache.getCachePath();
  }

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
    buffer.append(MOBILE_IMAGE);
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
      Bitmap bitmap = shrinkBitmap(file.getPath(), IMAGE_WIDTH, IMAGE_HEIGH);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      bitmap.compress(CompressFormat.PNG, IMAGE_QUALITY, output);
      File tempFile = new File(parentPath + TEMP_FILE_NAME);
      FileOutputStream out = new FileOutputStream(tempFile);
      output.writeTo(out);
      return tempFile;
    } catch (IOException e) {
      return null;
    }

  }

  public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
    try {

      bitmap = resizeImage(bitmap, ExoConstants.AVATAR_DEFAULT_SIZE);
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
      bitmap.recycle();
      return output;
    } catch (OutOfMemoryError e) {
      return null;
    }
  }

  /*
   * This method for getting the image url which return from Intent.ACTION_PICK
   */
  public static String getFileFromUri(Uri uri, Activity activity) {
    String filePath = null;
    String[] projection = { MediaStore.Images.ImageColumns.DATA /* col1 */};
    Cursor c = activity.managedQuery(uri, projection, null, null, null);
    if (c != null && c.moveToFirst()) {
      int columnIndex = c.getColumnIndex(MediaColumns.DATA);
      filePath = c.getString(columnIndex);
    }

    return filePath;
  }

  public static String downloadFile(String url, String name) {
    File file = null;
    HttpParams httpParameters = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
    HttpConnectionParams.setSoTimeout(httpParameters, 30000);
    HttpConnectionParams.setTcpNoDelay(httpParameters, true);

    DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
    HttpGet httpGet = new HttpGet(url);
    try {
      HttpResponse response = httpClient.execute(httpGet);
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        InputStream is = entity.getContent();
        String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
        file = new File(parentPath + name);
        OutputStream out = new FileOutputStream(file);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0)
          out.write(buf, 0, len);
        out.close();
        is.close();
      }

    } catch (ClientProtocolException e) {
      return null;
    } catch (IOException e) {
      return null;
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    return file.getAbsolutePath();
  }

  /*
   * This method for getting the image url which return from Intent.ACTION_PICK
   * include extra photos (from Picasa, etc...)
   */
  public static String extractFilenameFromUri(Uri uri, Activity activity) {

    String filePath = null;
    String[] projection = { MediaStore.Images.ImageColumns.DATA, MediaColumns.DISPLAY_NAME };

    Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
    if (cursor != null && cursor.moveToFirst()) {
      int columnIndex = cursor.getColumnIndex(MediaColumns.DATA);
      if (columnIndex != -1) {

        // regular processing for gallery files
        filePath = cursor.getString(columnIndex);
      } else {
        Log.i("PHOTO_PICKER", "Get image from Picasa Album");
        // this is for get image from other provider (exp. Picasa ...)
        columnIndex = cursor.getColumnIndex(MediaColumns.DISPLAY_NAME);
        if (columnIndex != -1) {
          try {
            String name = cursor.getString(columnIndex);
            Log.i("PHOTO_PICKER", "Image Name: " + name);
            final InputStream is = activity.getContentResolver().openInputStream(uri);
            String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
            File file = new File(parentPath + name);
            OutputStream out = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0)
              out.write(buf, 0, len);
            out.close();
            is.close();
            filePath = file.getAbsolutePath();
          } catch (FileNotFoundException e) {
            Log.e("PHOTO_PICKER", "could not find the path");
            return null;
          } catch (IOException e) {
            Log.e("PHOTO_PICKER", "error in write file to sdcard");
            return null;
          }
        }
      }
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

  public static void copyStream(InputStream is, OutputStream os) throws IOException {
    final int buffer_size = 1024;
    byte[] bytes = new byte[buffer_size];
    int count;
    while ((count = is.read(bytes, 0, buffer_size)) != -1) {
      os.write(bytes, 0, count);
    }
  }

  /*
   * Make radial gradient bitmap
   */
  public static Bitmap makeRadGrad(int width, int heigh) {
    RadialGradient gradient = new RadialGradient(width / 2,
                                                 heigh / 2,
                                                 width / 2,
                                                 0x8F8F8F8F,
                                                 0x1C1C1C1C,
                                                 android.graphics.Shader.TileMode.CLAMP);
    Paint p = new Paint();
    p.setDither(true);
    p.setShader(gradient);

    Bitmap bitmap = Bitmap.createBitmap(width, heigh, Config.ARGB_8888);
    Canvas c = new Canvas(bitmap);
    c.drawOval(new RectF(0, 0, width, heigh), p);

    return bitmap;
  }
}
