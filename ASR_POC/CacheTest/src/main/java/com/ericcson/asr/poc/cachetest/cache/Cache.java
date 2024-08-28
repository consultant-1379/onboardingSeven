package com.ericcson.asr.poc.cachetest.cache;

import java.util.concurrent.ExecutionException;

import com.ericsson.asr.poc.cachetest.types.CTUMEvent;

public interface Cache {
    public CTUMEvent get(Long key) throws ExecutionException;

    public void put(Long key, CTUMEvent event);

    public long size();
}
