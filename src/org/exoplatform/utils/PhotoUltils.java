package org.exoplatform.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.exoplatform.R;
import org.exoplatform.model.SocialPhotoInfo;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.os.Environment;
import android.text.format.DateFormat;

public class PhotoUltils {
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

  public static void getAllImageFiles(File folder, ArrayList<String> all) {
    if (folder.getName().equals(".thumbnails")) {
      return;
    }
    if (folder.isFile()) {
      if (isImages(folder)) {
        all.add(folder.getAbsolutePath());
      }
    }
    if (folder.isDirectory()) {
      if (folder.listFiles() != null) {
        for (File file : folder.listFiles()) {
          getAllImageFiles(file, all);
        }
      }

    }

  }

  public static void getAllImages(File folder, ArrayList<String> all) {

    if (folder.getName().equalsIgnoreCase(".thumbnails")) {
      if (folder.listFiles() != null) {
        for (File file : folder.listFiles()) {
          if (file.isFile()) {
            all.add(file.getAbsolutePath());
          }
        }
      }
    } else if (folder.isDirectory()) {
      if (folder.listFiles() != null) {
        for (File file : folder.listFiles()) {
          getAllImages(file, all);
        }
      }
    }
  }

  private static String getDateFormat() {
    String dateFormat = null;
    Calendar cal = Calendar.getInstance();
    long minus = cal.getTimeInMillis();
    String inFormat = new String("yyyy_MM_dd_hh_mm_ss");
    dateFormat = (String) DateFormat.format(inFormat, minus);
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

  public static Bitmap shrinkBitmap(InputStream inputStream, int width, int height) {

    BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
    bmpFactoryOptions.inJustDecodeBounds = true;
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

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
    bitmap = BitmapFactory.decodeStream(inputStream, null, bmpFactoryOptions);
    return bitmap;
  }

  public static Bitmap reflectionPhoto(Bitmap originalImage) {
    // The gap we want between the reflection and the original image
    final int reflectionGap = 4;

    int width = originalImage.getWidth();
    int height = originalImage.getHeight();

    // This will not scale but will flip on the Y axis
    Matrix matrix = new Matrix();
    matrix.preScale(1, -1);

    // Create a Bitmap with the flip matix applied to it.
    // We only want the bottom half of the image
    Bitmap reflectionImage = Bitmap.createBitmap(originalImage,
                                                 0,
                                                 height / 2,
                                                 width,
                                                 height / 2,
                                                 matrix,
                                                 false);

    // Create a new bitmap with same width but taller to fit reflection
    Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                                                      (height + height / 2),
                                                      Config.ARGB_8888);

    // Create a new Canvas with the bitmap that's big enough for
    // the image plus gap plus reflection
    Canvas canvas = new Canvas(bitmapWithReflection);
    // Draw in the original image
    canvas.drawBitmap(originalImage, 0, 0, null);
    // Draw in the gap
    Paint deafaultPaint = new Paint();
    canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);
    // Draw in the reflection
    canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

    // Create a shader that is a linear gradient that covers the reflection
    Paint paint = new Paint();
    LinearGradient shader = new LinearGradient(0,
                                               originalImage.getHeight(),
                                               0,
                                               bitmapWithReflection.getHeight() + reflectionGap,
                                               0x70ffffff,
                                               0x00ffffff,
                                               TileMode.CLAMP);
    // Set the paint to use this shader (linear gradient)
    paint.setShader(shader);
    // Set the Transfer mode to be porter duff and destination in
    paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
    // Draw a rectangle using the paint with our linear gradient
    canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

    return bitmapWithReflection;
  }

  public static Set<File> getImageFolder(File directory, FilenameFilter[] filter, int recurse) {
    Set<File> files = new HashSet<File>();

    File[] entries = directory.listFiles();

    if (entries != null) {
      for (File entry : entries) {
        if (entry.getName().startsWith(".")) {
          // break;
        } else {
          for (FilenameFilter filefilter : filter) {
            if (filter == null || filefilter.accept(directory, entry.getName())) {
              files.add(entry.getParentFile());
              break;
            }
          }
          if ((recurse <= -1) || (recurse > 0 && entry.isDirectory())) {
            recurse--;
            files.addAll(getImageFolder(entry, filter, recurse));
            recurse++;
          }

        }
      }
    }
    return files;
  }

  public static ArrayList<SocialPhotoInfo> listFileToArray(File directory,
                                                           FilenameFilter[] filter,
                                                           int recurse) {
    ArrayList<SocialPhotoInfo> list = new ArrayList<SocialPhotoInfo>();
    Set<File> collection = getImageFolder(directory, filter, recurse);
    for (File file : collection) {
      SocialPhotoInfo info = new SocialPhotoInfo();
      ArrayList<String> listFile = new ArrayList<String>();
      info.setAlbumsName(file.getName());
      File[] entries = file.listFiles();
      if (entries != null) {
        for (File entry : entries) {
          for (FilenameFilter filefilter : filter) {
            if (filter == null || filefilter.accept(directory, entry.getName())) {
              listFile.add(entry.getAbsolutePath());
            }
          }
        }
        info.setImageList(listFile);
      }
      list.add(info);
    }

    return list;

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

}
