package eXo.eXoPlatform.utils;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.text.format.DateFormat;

public class PhotoUltils {
  private static final String[] suffix  = { ".jpeg", ".jpg", ".png", ".jpe", ".bmp" };

  private static final String   dotSign = ".";

  public static boolean isImages(File file) {
    String name = file.getName();
    for (int i = 0; i < suffix.length; i++) {
      if (name.endsWith(suffix[i]))
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

  public static void getAllImageFiles(File folder, List<String> all) {
    if (folder.getName().equals(".thumbnails")) {
      return;
    }
    if (folder.isFile()) {
      if (isImages(folder))
        all.add(folder.getAbsolutePath());
    }
    if (folder.isDirectory()) {
      for (File file : folder.listFiles()) {
        getAllImageFiles(file, all);
      }
    }

  }

  public static String getDateFormat() {
    String dateFormat = null;
    Calendar cal = Calendar.getInstance();
    long minus = cal.getTimeInMillis();
    String inFormat = new String("ddMMyyyyhhmmss");
    dateFormat = (String) DateFormat.format(inFormat, minus);
    return dateFormat;
  }

}
