package org.sysu.processexecutionservice.admission.timewheel;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;

/**
 * 定时器
 * @author: Gordan Lin
 * @create: 2019/12/12
 **/
public class Timer {

    // 时间轮
    private TimerWheel timerWheel;

    // 对于一个Timer以及附属的时间轮，都只有一个priorityQueue
    private PriorityBlockingQueue<Bucket> priorityQueue = new PriorityBlockingQueue<>(1000,new Comparator<Bucket>() {
        @Override
        public int compare(Bucket bucket1, Bucket bucket2) {
            return (int) (bucket1.getExpire()-bucket2.getExpire());
        }
    });

    private ExecutorService workerThreadPool;

    private ScheduledExecutorService bossThreadPool;

    private static Timer TIMER_INSTANCE;

    public static Timer getInstance() {
        if (TIMER_INSTANCE == null) {
            synchronized (Timer.class) {
                if (TIMER_INSTANCE == null) {
                    TIMER_INSTANCE = new Timer();
                }
            }
        }
        return TIMER_INSTANCE;
    }

    private Timer() {
        workerThreadPool = Executors.newFixedThreadPool(100, new ThreadFactoryBuilder().setPriority(10)
                .setNameFormat("TimerWheelWorker")
                .build());
        bossThreadPool = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setPriority(10)
                .setNameFormat("TimerWheelBoss")
                .build());

        timerWheel = new TimerWheel(200, 60, System.currentTimeMillis(), priorityQueue);
        bossThreadPool.scheduleAtFixedRate(() -> {
            TIMER_INSTANCE.advanceClock();
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * 将任务添加到时间轮
     */
    public void addTask(TimerTask timerTask) {
        if (!timerWheel.addTask(timerTask)) {
            workerThreadPool.submit(timerTask.getTask());
        }
    }

    /**
     *  指针推进
     */
    public synchronized void advanceClock() {
        long currentTimestamp = System.currentTimeMillis();
        timerWheel.advanceClock(currentTimestamp);
        Bucket bucket = priorityQueue.peek();
        if (bucket == null || bucket.getExpire() > currentTimestamp) return;
        priorityQueue.poll();
        List<TimerTask> taskList = bucket.removeTaskAndGet(-1);
        // 执行具体的请求
        for (TimerTask timerTask : taskList) {
            workerThreadPool.submit(timerTask.getTask());
        }
    }

}
