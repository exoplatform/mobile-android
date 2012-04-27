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
import java.net.URLEncoder;

import android.content.Context;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Apr
 * 5, 2012
 */

public class FileCache {

  private File cacheDir;

  public FileCache(Context context) {
    // Find the dir to save cached images
    if (android.os.Environment.getExternalStorageState()
                              .equals(android.os.Environment.MEDIA_MOUNTED))
      cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "eXo/SocialCached");
    else
      cacheDir = context.getCacheDir();
    if (!cacheDir.exists())
      cacheDir.mkdirs();
  }

  public File getFile(String url) {
    String filename=String.valueOf(url.hashCode());
    File f = new File(cacheDir, filename);
    return f;

  }

  public void clear() {
    File[] files = cacheDir.listFiles();
    if (files == null)
      return;
    for (File f : files)
      f.delete();
  }

}
