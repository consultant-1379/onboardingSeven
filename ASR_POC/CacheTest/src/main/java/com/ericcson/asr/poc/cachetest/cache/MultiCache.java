package com.ericcson.asr.poc.cachetest.cache;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.asr.poc.cachetest.objects.GuavaCTUMCache;

public class MultiCache {

    Map<Integer, Cache> caches = new HashMap<Integer, Cache>();

    public void createCache(int i) {
        Cache guavaCTUMCache = new GuavaCTUMCache();
        caches.put(i, guavaCTUMCache);
    }

    public Cache getCache(int i) {
        return caches.get(i);
    }

    public int size() {
        return caches.size();
    }
}
