package com.ericcson.asr.poc.cachetest.cache;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.ericsson.asr.poc.cachetest.types.CTUMEvent;

@Singleton
public class BasicCache {

    private static BasicCache instance = null;
    private Map<Long, CTUMEvent> cache = new HashMap<Long, CTUMEvent>();
    private long id = 0;

    protected BasicCache() {

    }

    public long getSize() {
        return cache.size() - 1;
    }

    private void incrementID() {
        id++;
    }

    public void add(CTUMEvent event) {
        cache.put(id, event);
        incrementID();
    }

    public CTUMEvent get(long id) {
        return cache.get(id);
    }

    public static BasicCache getInstance() {
        if (instance == null) {
            instance = new BasicCache();
        }

        return instance;
    }

}
