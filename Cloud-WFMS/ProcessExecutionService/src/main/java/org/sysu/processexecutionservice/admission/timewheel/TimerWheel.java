package org.sysu.processexecutionservice.admission.timewheel;

import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 时间轮
 * @author: Gordan Lin
 * @create: 2019/12/11
 **/
public class TimerWheel {

    // 一个时间槽的时间长度
    private long tickMs;

    // 时间轮大小，即时间槽个数
    private int wheelSize;

    // 槽
    private Bucket[] buckets;

    // 时间轮指针
    private long currentTimestamp;

    // 对于一个Timer以及附属的时间轮，都只有一个priorityQueue
    private PriorityBlockingQueue<Bucket> priorityQueue;

    public TimerWheel(long tickMs, int wheelSize, long currentTimestamp, PriorityBlockingQueue<Bucket> priorityQueue) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.currentTimestamp = currentTimestamp;
        this.buckets = new Bucket[wheelSize];
        this.priorityQueue = priorityQueue;
        for (int i = 0; i < wheelSize; i++) {
            buckets[i] = new Bucket();
        }
    }

    /**
     * 添加任务到时间轮
     * @param timerTask
     * @return
     */
    public synchronized boolean addTask(TimerTask timerTask) {
        long delayMs = timerTask.getDelayMs();
        if (delayMs < tickMs) { // 到期了,直接执行
            return false;
        } else {
            int bucketIndex = (int) (((delayMs-tickMs+currentTimestamp) / tickMs) % wheelSize);
            Bucket bucket = buckets[bucketIndex];
            bucket.addTask(timerTask);
            // 添加到优先队列中
            if (bucket.setExpire(delayMs + currentTimestamp - (delayMs + currentTimestamp) % tickMs)) {
                priorityQueue.offer(bucket);
            }
        }
        return true;
    }

    public void advanceClock(long timestamp) {
        if (timestamp >= currentTimestamp+tickMs) {
            currentTimestamp = timestamp - (timestamp % tickMs);
        }
    }

}