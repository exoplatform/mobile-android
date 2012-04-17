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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Apr
 * 5, 2012
 */

public class MemoryCache {
  /*
   * Last argument true for LRU ordering
   */
  private Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10,
                                                                                                    1.5f,
                                                                                                    true));

  /*
   * current allocated size
   */
  private long                size  = 0;

  /*
   * max memory in bytes
   */
  private long                limit = 1000000;

  public MemoryCache() {
    // use 25% of available heap size
    setLimit(Runtime.getRuntime().maxMemory() / 4);
  }

  private void setLimit(long new_limit) {
    limit = new_limit;
  }

  public Bitmap get(String id) {
    if (!cache.containsKey(id))
      return null;
    return cache.get(id);
  }

  public void put(String id, Bitmap bitmap) {
    if (cache.containsKey(id))
      size -= getSizeInBytes(cache.get(id));
    cache.put(id, bitmap);
    size += getSizeInBytes(bitmap);
    checkSize();
  }

  private void checkSize() {
    if (size > limit) {
      /*
       * least recently accessed item will be the first one iterated
       */
      Iterator<Entry<String, Bitmap>> iter = cache.entrySet().iterator();
      while (iter.hasNext()) {
        Entry<String, Bitmap> entry = iter.next();
        size -= getSizeInBytes(entry.getValue());
        iter.remove();
        if (size <= limit)
          break;
      }
    }
  }

  public void clear() {
    cache.clear();
  }

  private long getSizeInBytes(Bitmap bitmap) {
    if (bitmap == null)
      return 0;
    return bitmap.getRowBytes() * bitmap.getHeight();
  }
}
