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
package org.exoplatform.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS
 * Author :  MinhTDH
 *           MinhTDH@exoplatform.com
 * Jul 14, 2015
 * utilities class for common use code  
 */
public class Utils {

  public static void hideSoftKeyboard(Activity activity) {
      if (activity == null) {
          return;
      }
      InputMethodManager imm = (InputMethodManager) activity
              .getSystemService(Activity.INPUT_METHOD_SERVICE);
      View v = activity.getCurrentFocus();
      if (imm != null && v != null) {
          imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
      }
  }

  public static void setText(View tv, CharSequence cs) {
      if (tv instanceof TextView) {
          ((TextView) tv).setText(cs);
      }
  }
  
  public static void setTextSize(View tv, float size) {
      if (tv instanceof TextView) {
          ((TextView) tv).setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
      }
  }
  
  public static void setTextViewColor(View tv, int color) {
      if (tv instanceof TextView) {
          ((TextView) tv).setTextColor(color);
      }
  }
  
  public static void setTextColor(View tv, int colorId) {
      if (tv instanceof TextView) {
          ((TextView) tv).setTextColor(tv.getResources().getColorStateList(colorId));
      }
  }
  
  public static void setVisibility(View v, int visiblility) {
      if (v != null) {
          v.setVisibility(visiblility);
      }
  }
  public static void setBackGround(View v, int backGroundId) {
      if (v != null) {
          v.setBackgroundResource(backGroundId);
      }
  }
  
  public static void setOnclick(View v, OnClickListener listener) {
      if (v != null) {
          v.setOnClickListener(listener);
      }
  }
  public static void setEnable(View v, boolean enabled) {
      if (v != null) {
          v.setEnabled(enabled);
      }
  }
  public static boolean compare(String s1, String s2) {
      return s1 != null && s1.equals(s2);
  }
  
  public static int getSize(Collection list) {
      return list != null ? list.size() : 0 ;
  }

  public static <E> E getItem(List<E> list, int pos) {
      return list == null ? null : list.get(pos);
  }

  public static <T> T getFirstItem(List<T> list) {
      return list == null || list.size() == 0 ? null : list.get(0);
  }
  
  public static boolean isEmpty(CharSequence s) {
      return s == null || s.length() == 0;
  }

  public static boolean notEmpty(CharSequence s) {
      return s != null && s.length() > 0;
  }
  
  public static <T> T getVal(WeakReference<T> ref) {
      return ref == null ? null : ref.get();
  }

  /**
   * copy the input stream into a file, close input stream when done
   * @param is
   * @param file
   * @return
   */
  public static boolean writeToFile(InputStream is, File file) {
      OutputStream out = null;
      boolean ret = false;
      if (is == null) {
          return ret;
      }
      try {
          out = new FileOutputStream(file);
          byte[] buf = new byte[1024];
          int len;
          while ((len = is.read(buf)) > 0) {
              out.write(buf, 0, len);
          }
          ret = true;
      } catch (IOException e) {
          Log.e(Utils.class.getSimpleName(), Log.getStackTraceString(e));
      } finally {
          if (is != null) {
              try {
                  is.close();
              } catch (IOException e) {
                  Log.e(Utils.class.getSimpleName(), Log.getStackTraceString(e));
              }
          }
          if (out != null) {
              try {
                  out.close();
              } catch (IOException e) {
                  Log.e(Utils.class.getSimpleName(), Log.getStackTraceString(e));
              }
          }
      }
      return ret;
  }
  
  public static boolean writeToFile(String data, File file) {
      OutputStreamWriter outputStreamWriter = null;
      boolean ret = false;
      try {
          outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
          outputStreamWriter.write(data);
          outputStreamWriter.flush();
          ret = true;
      } catch (IOException e) {
          Log.e(Utils.class.getSimpleName(), Log.getStackTraceString(e));
      } finally {
          if (outputStreamWriter != null) {
              try {
                  outputStreamWriter.close();
              } catch (IOException e) {
                  Log.e(Utils.class.getSimpleName(), Log.getStackTraceString(e));
              }
          }
      }
      return ret;
  }


  public static final String SYSTEM_NEWLINE = System.getProperty("line.separator");

  public static StringBuilder readFromFile(File file) {
      StringBuilder ret = null;
      try {
          InputStream inputStream = new FileInputStream(file);

          if ( inputStream != null ) {
              InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
              BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
              String receiveString = "";
              ret = new StringBuilder();

              while ( (receiveString = bufferedReader.readLine()) != null ) {
                  ret.append(receiveString);
                  ret.append(SYSTEM_NEWLINE);
              }
              if (ret.length() > 0) {
                  ret.delete(ret.length() - SYSTEM_NEWLINE.length(), ret.length());
              }
              inputStream.close();
          }
      }
      catch (FileNotFoundException e) {
          Log.e("login activity", "File not found: " + e.toString());
      } catch (IOException e) {
          Log.e("login activity", "Can not read file: " + e.toString());
      }
      return ret;
  }
  
  public static int getRelativeTopWithRoot(View v) {
      int ret = 0;
      if (v != null) {
          
          while (v.getParent() instanceof View && v.getParent() != v.getRootView()) {
              ret += v.getTop();
              v = (View) v.getParent();
          }
      }
      return ret;
  }
}
