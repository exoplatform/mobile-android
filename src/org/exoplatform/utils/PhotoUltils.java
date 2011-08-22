package org.exoplatform.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.exoplatform.social.image.PhotoInfo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;

public class PhotoUltils {
  private static final String[] suffix  = { ".jpeg", ".jpg", ".png" };

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

  public static PhotoInfo[] listFilesAsArray(File directory, FilenameFilter[] filter, int recurse) {
    Collection<PhotoInfo> files = listFiles(directory, filter, recurse);

    PhotoInfo[] arr = new PhotoInfo[files.size()];
    return files.toArray(arr);
  }

  public static Collection<PhotoInfo> listFiles(File directory, FilenameFilter[] filter, int recurse) {

    Vector<PhotoInfo> photoList = new Vector<PhotoInfo>();

    File[] entries = directory.listFiles();

    if (entries != null) {
      for (File entry : entries) {
        // if (entry.getName().equals(".thumbnails")) {
        // break;
        // }
        if (entry.getName().startsWith(".")) {
          break;
        }

        PhotoInfo info = new PhotoInfo();
        ArrayList<String> listFile = new ArrayList<String>();
        for (FilenameFilter filefilter : filter) {
          if (filter == null || filefilter.accept(directory, entry.getName())) {
            listFile.add(entry.getAbsolutePath());
          }
        }
        info.setImageList(listFile);
        info.setAlbumsName(entry.getName());
        photoList.add(info);
        if ((recurse <= -1) || (recurse > 0 && entry.isDirectory())) {
          recurse--;
          photoList.addAll(listFiles(entry, filter, recurse));
          recurse++;
        }
      }
    }
    return photoList;
  }

  public static Set<File> getImageFolder(File directory, FilenameFilter[] filter, int recurse) {
    Set<File> files = new HashSet<File>();

    File[] entries = directory.listFiles();

    if (entries != null) {
      for (File entry : entries) {
        if (entry.getName().startsWith(".")) {
//          break;
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

  public static ArrayList<PhotoInfo> listFileToArray(File directory,
                                                     FilenameFilter[] filter,
                                                     int recurse) {
    ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
    Set<File> collection = getImageFolder(directory, filter, recurse);
    for (File file : collection) {
      PhotoInfo info = new PhotoInfo();
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

  public static Collection<File> listAlbumsFiles(File directory,
                                                 FilenameFilter[] filter,
                                                 int recurse) {

    Vector<File> files = new Vector<File>();

    File[] entries = directory.listFiles();

    if (entries != null) {
      for (File entry : entries) {
        for (FilenameFilter filefilter : filter) {
          if (filter == null || filefilter.accept(directory, entry.getName())) {
            files.add(entry);
          }
        }
        if ((recurse <= -1) || (recurse > 0 && entry.isDirectory())) {
          recurse--;
          files.addAll(listAlbumsFiles(entry, filter, recurse));
          recurse++;
        }
      }
    }
    return files;
  }

}
