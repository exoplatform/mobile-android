package org.exoplatform.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SocialCache implements KCache {

  private Map<Object, Object[]> cache;

  /**
   * Used to restrict the size of the cache map.
   */
  private Queue                 queue;

  private int                   maxSize;

  /**
   * Using this integer because ConcurrentLinkedQueue.size is not constant time.
   */
  private AtomicInteger         size = new AtomicInteger();

  public SocialCache(int maxSize) {
    this.maxSize = maxSize;
    cache = new ConcurrentHashMap(maxSize);
    queue = new ConcurrentLinkedQueue();
  }

  public void put(Object key, Object val) {
    put(key, val, null);
  }

  public void put(Object key, Object val, Integer seconds_to_store) {
    if (key == null)
      throw new RuntimeException("Key cannot be null!");
    seconds_to_store = seconds_to_store != null ? seconds_to_store : 9999999;
    cache.put(key, new Object[] { System.currentTimeMillis() + seconds_to_store, val });
    queue.add(key);
    size.incrementAndGet();

    while (size.get() > maxSize && maxSize > 0) {
      Object toRemove = queue.poll();
      if (toRemove == null)
        break;
      // System.out.println("toRemove=" + toRemove + " size=" + size.get() +
      // " maxSize=" + maxSize);
      if (toRemove != null) {
        remove(key);
      }
    }
  }

  public Object get(Object key) {
    if (cache.containsKey(key)) {
      Long expires = (Long) cache.get(key)[0];
      if (expires - System.currentTimeMillis() > 0) {
        return cache.get(key)[1];
      } else {
        remove(key);
      }
    }
    return null;
  }

  /**
   * Returns boolean to stay compatible with ehcache and memcached.
   * 
   * @see #removeAndGet for alternate version.
   */
  public boolean remove(Object key) {
    return removeAndGet(key) != null;
  }

  public Object removeAndGet(Object key) {
    Object[] entry = cache.remove(key);
    // System.out.println("entry=" + entry);
    if (entry != null) {
      return entry[1];
    }
    size.decrementAndGet();
    return null;
  }

  public int size() {
    return size.get();
  }

  public Map getAll(Collection collection) {
    Map ret = new HashMap();
    for (Object o : collection) {
      ret.put(o, cache.get(o));
    }
    return ret;
  }

  public void clear() {
    cache.clear();
  }

  public int mapSize() {
    return cache.size();
  }

  public int queueSize() {
    return queue.size();
  }
}
