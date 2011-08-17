package org.exoplatform.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import android.text.format.DateFormat;

public class PhotoUltils {
  private static final String[] suffix  = { ".jpeg", ".jpg", ".png", ".jpe", ".bmp",".gif" };

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
      if(folder.listFiles()!=null){
        for (File file : folder.listFiles()) {
          getAllImageFiles(file, all);
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
  
  public static String getImageFileName(){
    StringBuffer buffer = new StringBuffer();
    buffer.append("MobileImage_");
    buffer.append(getDateFormat());
    buffer.append(".jpeg");
    return buffer.toString();
  }

}
