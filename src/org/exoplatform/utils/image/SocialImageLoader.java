/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.utils.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.Base64;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.utils.SocialActivityUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Apr
 * 5, 2012
 */
public class SocialImageLoader {

  private MemoryCache            memoryCache             = new MemoryCache();

  private FileCache              fileCache;

  /*
   * The mapping between image view and image's url
   */
  private Map<ImageView, String> imageViews              = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

  /*
   * Provides methods to manage termination and methods that can produce a
   * Future for tracking progress of one or more asynchronous tasks
   */
  private ExecutorService        executorService;

  // The image drawable for progress downloading
  private final int              DOWNLOAD_PROGRESS_IMAGE = R.drawable.loading;

  // The image drawable for link type
  private final int              UNREADABLE_LINK         = R.drawable.icon_for_unreadable_link;

  // The image drawable for image type
  private final int              PLACEHOLDER_IMAGE       = R.drawable.icon_for_placeholder_image;

  private final int              REQUIRED_SIZE           = 100;

  private String                 username;

  private String                 password;

  public SocialImageLoader(Context context) {
    username = AccountSetting.getInstance().getUsername();
    password = AccountSetting.getInstance().getPassword();
    fileCache = new FileCache(context, ExoConstants.SOCIAL_FILE_CACHE);
    executorService = Executors.newFixedThreadPool(5);

  }

  public void displayImage(String url, ImageView imageView, boolean isLink) {
    url = SocialActivityUtil.convertToThumbnail(url);
    imageViews.put(imageView, url);
    Bitmap bitmap = memoryCache.get(url);
    if (bitmap != null)
      imageView.setImageBitmap(bitmap);
    else {
      queuePhoto(url, imageView, isLink);
      imageView.setImageResource(DOWNLOAD_PROGRESS_IMAGE);
    }
  }

  private void queuePhoto(String url, ImageView imageView, boolean isLink) {
    PhotoToLoad p = new PhotoToLoad(url, imageView, isLink);
    executorService.submit(new PhotosLoader(p));

  }

  /*
   * Check if this url already has file cache then decode and get bitmap else
   * get image bitmap from url and save it to file cache
   */
  private Bitmap getBitmap(String url) {
    HttpParams httpParameters = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
    HttpConnectionParams.setSoTimeout(httpParameters, 10000);
    HttpConnectionParams.setTcpNoDelay(httpParameters, true);
    DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
    try {
      File f = fileCache.getFile(url);

      // from SD cache
      Bitmap b = decodeFile(f);
      if (b != null)
        return b;
      else {
        // from web
        Bitmap bitmap = null;
        /*
         * Send authentication each time we execute HttpGet to avoid the step
         * checking session time out.
         */
        HttpGet getRequest = new HttpGet(url);
        StringBuilder buffer = new StringBuilder(username);
        buffer.append(":");
        buffer.append(password);
        getRequest.setHeader("Authorization",
                             "Basic " + Base64.encodeBytes(buffer.toString().getBytes()));
        HttpResponse response = httpClient.execute(getRequest);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          InputStream is = entity.getContent();
          OutputStream os = new FileOutputStream(f);
          PhotoUtils.copyStream(is, os);
          os.close();
          bitmap = decodeFile(f);
        }

        return bitmap;
      }
    } catch (IOException ex) {
      return null;
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  // decodes image and scales it to reduce memory consumption
  private Bitmap decodeFile(File f) {
    try {
      // decode image size
      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(new FileInputStream(f), null, o);

      // Find the correct scale value. It should be the power of 2.
      int width_tmp = o.outWidth, height_tmp = o.outHeight;
      int scale = 1;
      while (true) {
        if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
          break;
        width_tmp /= 2;
        height_tmp /= 2;
        scale *= 2;
      }

      // decode with inSampleSize
      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = scale;
      return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
    } catch (FileNotFoundException e) {
      return null;
    }

  }

  // Task for the queue
  private class PhotoToLoad {
    public String    url;

    public ImageView imageView;

    public boolean   isLinkType;

    public PhotoToLoad(String u, ImageView i, boolean isLink) {
      url = u;
      imageView = i;
      isLinkType = isLink;
    }
  }

  class PhotosLoader implements Runnable {
    PhotoToLoad photoToLoad;

    PhotosLoader(PhotoToLoad photoToLoad) {
      this.photoToLoad = photoToLoad;
    }

    @Override
    public void run() {
      if (imageViewReused(photoToLoad))
        return;
      Bitmap bmp = getBitmap(photoToLoad.url);
      memoryCache.put(photoToLoad.url, bmp);
      if (imageViewReused(photoToLoad))
        return;
      BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
      Activity a = (Activity) photoToLoad.imageView.getContext();
      a.runOnUiThread(bd);
    }
  }

  boolean imageViewReused(PhotoToLoad photoToLoad) {
    String tag = imageViews.get(photoToLoad.imageView);
    if (tag == null || !tag.equals(photoToLoad.url))
      return true;
    return false;
  }

  // Used to display bitmap in the UI thread
  class BitmapDisplayer implements Runnable {
    Bitmap      bitmap;

    PhotoToLoad photoToLoad;

    public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
      bitmap = b;
      photoToLoad = p;
    }

    public void run() {
      if (imageViewReused(photoToLoad))
        return;
      if (bitmap != null) {
        photoToLoad.imageView.setImageBitmap(bitmap);
      } else {
        /*
         * if can not get image bitmap from web link, we will display this
         * instead
         */
        if (photoToLoad.isLinkType) {
          photoToLoad.imageView.setImageResource(UNREADABLE_LINK);
        } else
          photoToLoad.imageView.setImageResource(PLACEHOLDER_IMAGE);

      }

    }
  }

  public void clearCache() {
    memoryCache.clear();
    fileCache.clear();
  }

}
