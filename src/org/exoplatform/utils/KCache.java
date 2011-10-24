package org.exoplatform.utils;
public interface KCache {
    void put(Object key, Object value, Integer secondsToLive);
}
