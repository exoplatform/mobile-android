package org.exoplatform.utils;

/**
 * Created by minhtdh on 4/7/15.
 */
public class Log {
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
        return android.util.Log.getStackTraceString(tr);
    }

    public static int d(final String tag, final String msg) {
        return android.util.Log.d(tag, msg);
    }

    public static int d(final String tag, final String msg, Object... args) {
      StringBuilder bld = new StringBuilder(msg);
      if (args != null) {
        for (Object obj : args) {
          bld.append(obj);
        }
      }
      return android.util.Log.d(tag, bld.toString());
    }
    
    public static int e(final String tag, final String msg) {
        return android.util.Log.e(tag, msg);
    }

    public static int e(final String tag, final String msg, Object... args) {
      StringBuilder bld = new StringBuilder(msg);
      if (args != null) {
        for (Object obj : args) {
          bld.append(obj);
        }
      }
      return android.util.Log.e(tag, bld.toString());
    }
    
    public static int i(final String tag, final String msg) {
        return android.util.Log.i(tag, msg);
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
        return android.util.Log.d(tag, msg, tr);
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
