package com.ericsson.asr.poc.cachetest.objects;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import com.ericcson.asr.poc.cachetest.cache.Cache;
import com.ericsson.asr.poc.cachetest.types.CTUMEvent;
import com.google.common.cache.*;

@State(Scope.Thread)
@Warmup(iterations = 20)
@Measurement(iterations = 20)
@Fork(value = 1)
@Threads(value = 1)
// use these annotations for the JMH benchmarking tool
public class GuavaCTUMCache implements Cache {

    LoadingCache<Long, CTUMEvent> ctumCache;
    CTUMEvent ctumEvent = new CTUMEvent();
    long cacheId = 0;

    public GuavaCTUMCache() {
        this.ctumCache = CacheBuilder.newBuilder().maximumSize(10000000000L)
                .expireAfterAccess(3, TimeUnit.MINUTES).recordStats()
                .build(new CacheLoader<Long, CTUMEvent>() {
                    @Override
                    public CTUMEvent load(Long arg0) throws Exception {
                        return new CTUMEvent();
                    }

                });
    }

    public CTUMEvent get(Long id) throws ExecutionException {
        return ctumCache.get(id);
    }

    public void put(Long id, CTUMEvent event) {
        ctumCache.put(id, event);
    }

    public long size() {
        return ctumCache.size();
    }

    /*
     * public CTUMEvent getCtumEvent(){ return this.ctumEvent; }
     * 
     * 
     * public void add(long id, CTUMEvent ctumEvent){ ctumCache.put(id,
     * ctumEvent); }
     * 
     * @Benchmark public void populateCache(){ cacheId +=1;
     * ctumCache.put(cacheId, ctumEvent); }
     * 
     * public static void main(String args[]){
     * 
     * GuavaCTUMCache guavaCtumCache = new GuavaCTUMCache();
     * 
     * for (long id = 0; id < 10000000L; id++){ guavaCtumCache.add(id,
     * guavaCtumCache.getCtumEvent()); if( id % 10000 == 0){
     * System.out.print("."); } }
     * 
     * try { System.out.println("gone to sleep"); Thread.sleep(10000L); } catch
     * (InterruptedException e) { e.printStackTrace(); }
     * 
     * System.out.println("Finished");
     * 
     * }
     */

}
