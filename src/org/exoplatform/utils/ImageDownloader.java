package org.exoplatform.utils;

import greendroid.app.GDApplication;
import greendroid.app.GDApplication.OnLowMemoryListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageView;

public class ImageDownloader implements OnLowMemoryListener {

  private static final int                                       HARD_CACHE_CAPACITY = 100;

  private static final int                                       DELAY_BEFORE_PURGE  = 3 * 1000;

  // in
  // milliseconds

  // Hard cache, with a fixed maximum capacity and a life duration
  private final HashMap<String, Bitmap>                          sHardBitmapCache;

  // Soft cache for bitmap kicked out of hard cache
  private final ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache;

  private final Handler                                          purgeHandler        = new Handler();

  private final Runnable                                         purger;

  public ImageDownloader(Context context) {
    sHardBitmapCache = new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {

      /**
		 * 
		 */
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
        if (size() > HARD_CACHE_CAPACITY) {
          sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
          return true;
        } else {
          return false;
        }
      }
    };
    sSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

    purger = new Runnable() {
      public void run() {
        clearCache();
      }
    };

    ((GDApplication) context).registerOnLowMemoryListener(this);
    resetPurgeTimer();
  }

  /**
   * Download the specified image from the Internet and binds it to the provided
   * ImageView. The binding is immediate if the image is found in the cache and
   * will be done asynchronously otherwise. A null bitmap will be associated to
   * the ImageView if an error occurs.
   * 
   * @param url The URL of the image to download.
   * @param imageView The ImageView to bind the downloaded image to.
   */
  public void download(String url, ImageView imageView) {
    url = url.replaceAll(" ", "%20");
    download(url, imageView, null);
  }

  /**
   * Same as {@link #download(String, ImageView)}, with the possibility to
   * provide an additional cookie that will be used when the image will be
   * retrieved.
   * 
   * @param url The URL of the image to download.
   * @param imageView The ImageView to bind the downloaded image to.
   * @param cookie A cookie String that will be used by the http connection.
   */
  public void download(String url, ImageView imageView, String cookie) {
    // resetPurgeTimer();
    url = url.replaceAll(" ", "%20");
    Bitmap bitmap = getBitmapFromCache(url);

    if (bitmap == null) {
      forceDownload(url, imageView, cookie);
      // imageView.setImageResource(R.drawable.documenticonforunknown);
    } else {
      cancelPotentialDownload(url, imageView);
      imageView.setImageBitmap(bitmap);
    }
  }

  /*
   * Same as download but the image is always downloaded and the cache is not
   * used. Kept private at the moment as its interest is not clear. private void
   * forceDownload(String url, ImageView view) { forceDownload(url, view, null);
   * }
   */

  /**
   * Same as download but the image is always downloaded and the cache is not
   * used. Kept private at the moment as its interest is not clear.
   */
  private void forceDownload(String url, ImageView imageView, String cookie) {
    // State sanity: url is guaranteed to never be null in DownloadedDrawable
    // and cache keys.
    if (url == null) {
      imageView.setImageDrawable(null);
      return;
    }

    if (cancelPotentialDownload(url, imageView)) {
      BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
      DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
      imageView.setImageDrawable(downloadedDrawable);
      task.execute(url, cookie);
    }
  }

  /**
   * Clears the image cache used internally to improve performance. Note that
   * for memory efficiency reasons, the cache will automatically be cleared
   * after a certain inactivity delay.
   */
  public void clearCache() {
    sHardBitmapCache.clear();
    sSoftBitmapCache.clear();
  }

  public void resetPurgeTimer() {
    purgeHandler.removeCallbacks(purger);
    purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
  }

  /**
   * Returns true if the current download has been canceled or if there was no
   * download in progress on this image view. Returns false if the download in
   * progress deals with the same url. The download is not stopped in that case.
   */
  private static boolean cancelPotentialDownload(String url, ImageView imageView) {
    BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

    if (bitmapDownloaderTask != null) {
      String bitmapUrl = bitmapDownloaderTask.url;
      if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
        bitmapDownloaderTask.cancel(true);
      } else {
        // The same URL is already being downloaded.
        return false;
      }
    }
    return true;
  }

  /**
   * @param imageView Any imageView
   * @return Retrieve the currently active download task (if any) associated
   *         with this imageView. null if there is no such task.
   */
  private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
    if (imageView != null) {
      Drawable drawable = imageView.getDrawable();
      if (drawable instanceof DownloadedDrawable) {
        DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
        return downloadedDrawable.getBitmapDownloaderTask();
      }
    }
    return null;
  }

  /**
   * @param url The URL of the image that will be retrieved from the cache.
   * @return The cached bitmap or null if it was not found.
   */
  private Bitmap getBitmapFromCache(String url) {
    // First try the hard reference cache
    synchronized (sHardBitmapCache) {
      final Bitmap bitmap = sHardBitmapCache.get(url);
      if (bitmap != null) {
        // Bitmap found in hard cache
        // Move element to first position, so that it is removed last
        sHardBitmapCache.remove(url);
        sHardBitmapCache.put(url, bitmap);
        return bitmap;
      }
    }

    // Then try the soft reference cache
    SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
    if (bitmapReference != null) {
      final Bitmap bitmap = bitmapReference.get();
      if (bitmap != null) {
        // Bitmap found in soft cache
        return bitmap;
      } else {
        // Soft reference has been Garbage Collected
        sSoftBitmapCache.remove(url);
      }
    }

    return null;
  }

  /**
   * The actual AsyncTask that will asynchronously download the image.
   */
  class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
    private static final int               IO_BUFFER_SIZE = 4 * 1024;

    private String                         url;

    private final WeakReference<ImageView> imageViewReference;

    public BitmapDownloaderTask(ImageView imageView) {
      imageViewReference = new WeakReference<ImageView>(imageView);
    }

    /**
     * Actual download method.
     */
    @Override
    public Bitmap doInBackground(String... params) {
      url = params[0];

      HttpParams httpParameters = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
      HttpConnectionParams.setSoTimeout(httpParameters, 30000);
      HttpConnectionParams.setTcpNoDelay(httpParameters, true);

      DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
      HttpGet getRequest = null;
      try {
        getRequest = new HttpGet(url);
        String cookie = params[1];
        if (cookie != null) {
          getRequest.setHeader("cookie", cookie);
        }

        httpClient.getCredentialsProvider().setCredentials(AccountSetting.getInstance()
                                                                         .getAuthScope(),
                                                           AccountSetting.getInstance()
                                                                         .getCredentials());
        HttpResponse response = httpClient.execute(getRequest);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
          return null;
        }

        HttpEntity entity = response.getEntity();
        if (entity != null) {
          InputStream inputStream = null;
          try {
            Bitmap bitmap = null;
            inputStream = entity.getContent();
            long contentLength = entity.getContentLength();
            if (inputStream != null) {
              BitmapFactory.Options bmOption = new BitmapFactory.Options();
              if (contentLength < 10000) {
                bmOption.inSampleSize = 1;
              } else if (contentLength < 50000) {
                bmOption.inSampleSize = 2;
              } else {
                bmOption.inSampleSize = 4;
              }

              bitmap = BitmapFactory.decodeStream(inputStream, null, bmOption);
            }
            return bitmap;
          } catch (OutOfMemoryError e) {
            clearCache();
            return null;
          } catch (IOException e) {
            return null;
          } finally {
            if (inputStream != null) {
              inputStream.close();
            }
            entity.consumeContent();
          }
        }
      } catch (Exception e) {
        return null;
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
      return null;
    }

    /**
     * Once the image is downloaded, associates it to the imageView
     */
    @Override
    public void onPostExecute(Bitmap bitmap) {
      if (isCancelled()) {
        bitmap = null;
      }

      // Add bitmap to cache
      if (bitmap != null) {
        synchronized (sHardBitmapCache) {
          if (!sHardBitmapCache.containsValue(bitmap)) {
            sHardBitmapCache.put(url, bitmap);
          }

        }
      }

      if (imageViewReference != null) {
        ImageView imageView = imageViewReference.get();
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
        // Change bitmap only if this process is still associated with it
        if (this == bitmapDownloaderTask) {
          if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
          } else
            imageView.setImageResource(R.drawable.documenticonforunknown);
        }
      }
    }

    public void copy(InputStream in, OutputStream out) throws IOException {
      byte[] b = new byte[IO_BUFFER_SIZE];
      int read;
      while ((read = in.read(b)) != -1) {
        out.write(b, 0, read);
      }
    }
  }

  /**
   * A fake Drawable that will be attached to the imageView while the download
   * is in progress.
   * <p>
   * Contains a reference to the actual download task, so that a download task
   * can be stopped if a new binding is required, and makes sure that only the
   * last started download process can bind its result, independently of the
   * download finish order.
   * </p>
   */
  static class DownloadedDrawable extends ColorDrawable {
    private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

    public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
      super(Color.BLACK);
      bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
    }

    public BitmapDownloaderTask getBitmapDownloaderTask() {
      return bitmapDownloaderTaskReference.get();
    }
  }

  // @Override
  public void onLowMemoryReceived() {
    resetPurgeTimer();
  }
}
