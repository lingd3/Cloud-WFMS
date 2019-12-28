package org.sysu.processexecutionservice.admission.timewheel;

import com.google.common.collect.EvictingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.processexecutionservice.util.JedisUtil;
import redis.clients.jedis.Tuple;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 负载预测，定时更新
 * @author: Gordan Lin
 * @create: 2019/12/26
 **/
public class LoadPrediction {

    private final static Logger logger = LoggerFactory.getLogger(LoadPrediction.class);

    public static final String REDIS_KEY = "task";
    public static final long UPDATE_DURATION = 3000;
    public static final long PREDICTION_DURATION = 1000;
    public static final int PREDICTION_WINDOW_SIZE = 60;

    private EvictingQueue<Integer> futureLoadQueue = EvictingQueue.create(PREDICTION_WINDOW_SIZE);

    public static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);

    {
        UpdateTask updateTask = new UpdateTask();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(updateTask, 0, UPDATE_DURATION, TimeUnit.MILLISECONDS);
        PredictTask predictTask = new PredictTask();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(predictTask, 0, PREDICTION_DURATION, TimeUnit.MILLISECONDS);
    }


    /**
     * 定时更新并删除过期数据
     */
    private void updateData() {
        Set<Tuple> tuples = JedisUtil.zrange(REDIS_KEY, 0, -1);
        for (Tuple t : tuples) {
            String member = t.getElement();
            long score = (long) t.getScore();
            if (score < System.currentTimeMillis()) {
                JedisUtil.zrem(REDIS_KEY, member);
            }
        }
    }

    /**
     * 从redis中获取负载数据，构造负载时序队列
     */
    private void predictData() {
        long current = System.currentTimeMillis();
        for (int i = 0; i < PREDICTION_WINDOW_SIZE; i++) {
            long timestamp = current+i*1000;
            int load = (int) JedisUtil.zcount(REDIS_KEY, timestamp, timestamp+1000);
            futureLoadQueue.add(load);
        }
        Iterator<Integer> iterator = futureLoadQueue.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();
    }

    private class UpdateTask implements  Runnable {
        @Override
        public void run() {
            updateData();
        }
    }

    private class PredictTask implements  Runnable {
        @Override
        public void run() {
            predictData();
        }
    }

}
