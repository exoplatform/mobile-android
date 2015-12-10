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

/**
 * Created by minhtdh on 4/7/15.
 */
public class Log {
  private static final int DEFAULT_LOG_RETURN_VAL = 0;
  public static boolean LOGGABLE = true;
    public static int LOG_LEVEL = android.util.Log.VERBOSE;
    public final static boolean LOGV;
    public final static boolean LOGD;
    public final static boolean LOGI;
    public final static boolean LOGW;
    public final static boolean LOGE;
    static {
        LOGV = (LOG_LEVEL <= android.util.Log.VERBOSE);
        LOGD = (LOG_LEVEL <= android.util.Log.DEBUG);
        LOGI = (LOG_LEVEL <= android.util.Log.INFO);
        LOGW = (LOG_LEVEL <= android.util.Log.WARN);
        LOGE = (LOG_LEVEL <= android.util.Log.ERROR);
    }

    public static boolean isLoggable(Object obj) {
        return LOGGABLE;
    }

    public static boolean isDLoggable(Object obj) {
        return LOGGABLE && LOGD;
    }
    
    public static int v(final String tag, final String msg) {
        return android.util.Log.v(tag, msg);
    }

  public static String getStackTraceString(final Throwable tr) {
    return new StringBuilder("\n").append(android.util.Log.getStackTraceString(tr)).toString();
  }

  public static int d(final String tag, final String msg) {
    if (LOGD) {
      return android.util.Log.d(tag, msg);
    } else
      return DEFAULT_LOG_RETURN_VAL;
  }

  public static int d(final String tag, final String msg, Object... args) {
    if (LOGD) {
      StringBuilder bld = new StringBuilder();
      bld.append(msg);
      if (args != null) {
        for (Object obj : args) {
          bld.append(obj);
        }
      }
      return android.util.Log.d(tag, bld.toString());
    } else
      return DEFAULT_LOG_RETURN_VAL;
  }

  public static int e(final String tag, final String msg) {
    if (LOGE) {
      return android.util.Log.e(tag, msg);
    } else
      return DEFAULT_LOG_RETURN_VAL;
  }

  public static int e(final String tag, final String msg, Object... args) {
    if (LOGE) {
      StringBuilder bld = new StringBuilder();
      bld.append(msg);
      if (args != null) {
        for (Object obj : args) {
          bld.append(obj);
        }
      }
      return android.util.Log.e(tag, bld.toString());
    } else
      return DEFAULT_LOG_RETURN_VAL;
  }
    
  public static int i(final String tag, final String msg) {
    if (LOGI)
      return android.util.Log.i(tag, msg);
    else
      return DEFAULT_LOG_RETURN_VAL;
  }

    public static int i(final String tag, final String msg, Object... args) {
      if (LOGI) {
        StringBuilder bld = new StringBuilder();
        bld.append(msg);
        if (args != null) {
          for (Object obj : args) {
            bld.append(obj);
          }
        }
        return android.util.Log.i(tag, bld.toString());
      } else
        return DEFAULT_LOG_RETURN_VAL;
    }

    public static int wtf(final String tag, final String msg) {
        return android.util.Log.wtf(tag, msg);
    }

    public static int w(final String tag, final Throwable tr) {
        return android.util.Log.w(tag, tr);
    }

    public static boolean isLoggable(final String s, final int i) {
        return android.util.Log.isLoggable(s, i);
    }

    public static int println(final int priority, final String tag, final String msg) {
        return android.util.Log.println(priority, tag, msg);
    }

    public static int wtf(final String tag, final String msg, final Throwable tr) {
        return android.util.Log.wtf(tag, msg, tr);
    }

    public static int e(final String tag, final String msg, final Throwable tr) {
        return android.util.Log.e(tag, msg, tr);
    }

    public static int v(final String tag, final String msg, final Throwable tr) {
        return android.util.Log.v(tag, msg, tr);
    }

    public static int wtf(final String tag, final Throwable tr) {
        return android.util.Log.wtf(tag, tr);
    }

  public static int d(final String tag, final String msg, final Throwable tr) {
    if (LOGD)
      return android.util.Log.d(tag, msg, tr);
    else
      return DEFAULT_LOG_RETURN_VAL;
  }

    public static int w(final String tag, final String msg, final Throwable tr) {
        return android.util.Log.w(tag, msg, tr);
    }

    public static int i(final String tag, final String msg, final Throwable tr) {
        return android.util.Log.i(tag, msg, tr);
    }

    public static int w(final String tag, final String msg) {
        return android.util.Log.w(tag, msg);
    }
}
