package org.activiti.engine.impl.persistence.deploy;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.sysu.activitiservice.component.Counter;

/**
 * Default cache: keep everything in memory, unless a limit is set.
 *
 * @author Joram Barrez
 */
public class DefaultDeploymentCache<T> implements DeploymentCache<T> {

    //使用类变量来统计; 每5秒打印一次总数
    public static LongAdder removeEldestEntryTotal = new LongAdder();
    public static LongAdder hitNum = new LongAdder();
    public static LongAdder missNum = new LongAdder();
    public static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    private static final Logger logger = LoggerFactory.getLogger(DefaultDeploymentCache.class);

    protected Map<String, T> cache;

    /** Cache with no limit */
    public DefaultDeploymentCache() {
        this(-1);
    }

    /** Cache which has a hard limit: no more elements will be cached than the limit. */
    public DefaultDeploymentCache(final int limit) {
//        System.out.println("DefaultDeploymentCache++" + " " + limit);
        if (limit > 0) {
            DefaultDeploymentCache.Task task = new DefaultDeploymentCache.Task();
            scheduledThreadPoolExecutor.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);

            this.cache = Collections.synchronizedMap(new LinkedHashMap<String, T>(limit + 1, 0.75f, true) { // +1 is needed, because the entry is inserted first, before it is removed
                // 0.75 is the default (see javadocs)
                // true will keep the 'access-order', which is needed to have a real LRU cache
                private static final long serialVersionUID = 1L;

                protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
                    boolean removeEldest = size() > limit;
                    if (removeEldest)  {
                        {
                            removeEldestEntryTotal.increment();
//                            System.out.println("current: " + removeEldestEntryTotal.longValue());
                            logger.trace("Cache limit is reached, {} will be evicted",  eldest.getKey());
                        }
                    }
                    return removeEldest;
                }

            });
        } else {
            this.cache = Collections.synchronizedMap(new HashMap<String, T>());
        }
    }

    public T get(String id) {
        T c = cache.get(id);
        if (c != null) hitNum.increment();
        else missNum.increment();
        return c;
    }

    public void add(String id, T obj) {
        cache.put(id, obj);
    }

    public void remove(String id) {
        cache.remove(id);
    }

    public void clear() {
        cache.clear();
    }

    // For testing purposes only
    public int size() {
        return cache.size();
    }

    private class Task implements Runnable {
        LongAdder removeEldestEntryTotal = DefaultDeploymentCache.removeEldestEntryTotal;
        @Override
        public void run() {
            System.out.println("@" + this.toString() +  " 缓存命中 的数量是: " + hitNum.longValue());
            System.out.println("@" + this.toString() +  " 缓存不命中 的数量是: " + missNum.longValue());
            System.out.println("@" + this.toString() +  " 缓存查询数 的数量是: " + (hitNum.longValue()+missNum.longValue()));
            System.out.println("@" + this.toString() +  " removeEldestEntry 的数量是: " + removeEldestEntryTotal.longValue());
        }
    }


}
