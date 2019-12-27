package org.sysu.processexecutionservice.admission.timewheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.processexecutionservice.util.JedisUtil;
import redis.clients.jedis.Tuple;

import java.util.List;
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
    public static final long PREDICTION_DURATION = 5000;

    public static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    {
        UpdateTask updateTask = new UpdateTask();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(updateTask, 0, UPDATE_DURATION, TimeUnit.MILLISECONDS);
    }

    /**
     * 计算在有序集合中指定区间分数的成员数
     * @return
     */
    public long getFutureLoad() {
        return JedisUtil.zcount(REDIS_KEY, 0, System.currentTimeMillis()+PREDICTION_DURATION);
    }

    /**
     * 定时更新并删除过期数据
     */
    private void updateData() {
        List<Tuple> tuples = JedisUtil.zscan(REDIS_KEY, 0);
        for (Tuple t : tuples) {
            String member = t.getElement();
            long score = (long) t.getScore();
            if (score < System.currentTimeMillis()) {
                JedisUtil.zrem(REDIS_KEY, member);
            }
        }
    }

    private class UpdateTask implements  Runnable {
        @Override
        public void run() {
            updateData();
        }
    }
}
