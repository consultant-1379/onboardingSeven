package com.ericsson.asr.poc.cachetest.cache;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.ericcson.asr.poc.cachetest.cache.Cache;
import com.ericcson.asr.poc.cachetest.cache.MultiCache;
import com.ericsson.asr.poc.cachetest.types.CTUMEvent;

public class MultiCacheTest {

    @Test
    public void countCaches() {
        int expected = 4;

        MultiCache multiCache = new MultiCache();

        for (int i = 1; i <= 4; i++) {
            multiCache.createCache(i);
        }

        int actual = multiCache.size();

        assertEquals(expected, actual);
    }

    @Test
    public void verifyCacheContents() throws ExecutionException {
        List<CTUMEvent> expected = new ArrayList<CTUMEvent>();

        expected.add(new CTUMEvent());
        expected.add(new CTUMEvent());
        expected.add(new CTUMEvent());

        MultiCache multiCache = new MultiCache();

        multiCache.createCache(1);
        multiCache.createCache(2);

        Cache cache = multiCache.getCache(1);

        cache.put(0L, new CTUMEvent());
        cache.put(1L, new CTUMEvent());
        cache.put(2L, new CTUMEvent());

        Cache cache1 = multiCache.getCache(1);
        Cache cache2 = multiCache.getCache(2);
        List<CTUMEvent> actual = new ArrayList<CTUMEvent>();

        actual.add(cache1.get(0L));
        actual.add(cache1.get(1L));
        actual.add(cache1.get(2L));

        assertEquals(0L, cache2.size());
        assertEquals(3L, cache1.size());
        assertEquals(expected, actual);
    }
}
