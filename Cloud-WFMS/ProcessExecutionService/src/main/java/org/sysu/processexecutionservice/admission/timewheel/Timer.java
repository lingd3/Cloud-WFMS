package org.sysu.processexecutionservice.admission.timewheel;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;

/**
 * 定时器
 * @author: Gordan Lin
 * @create: 2019/12/12
 **/
public class Timer {

    // 时间槽时间长度，单位是毫秒
    private static final int TICK_MS = 200;
    // 时间槽个数
    private static final int WHEEL_SIZE = 60;
    // 滑动时间窗口大小
    private static final int TIME_WINDOW_SIZE = 10;

    // 时间轮
    private TimerWheel timerWheel;

    // 对于一个Timer以及附属的时间轮，都只有一个priorityQueue
    private PriorityBlockingQueue<Bucket> priorityQueue = new PriorityBlockingQueue<>(WHEEL_SIZE+1, new Comparator<Bucket>() {
        @Override
        public int compare(Bucket bucket1, Bucket bucket2) {
            return (int) (bucket1.getExpire()-bucket2.getExpire());
        }
    });

    // 优先队列中各个bucket任务数，通过TIME_WINDOW_SIZE控制长度实现滑动时间窗口（空间换时间）
    private LinkedList<Integer> timeWindow = new LinkedList<>();

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

        timerWheel = new TimerWheel(TICK_MS, WHEEL_SIZE, System.currentTimeMillis(), priorityQueue);
        bossThreadPool.scheduleAtFixedRate(() -> {
            TIMER_INSTANCE.advanceClock();
        }, 0, TICK_MS, TimeUnit.MILLISECONDS);
    }

    public LinkedList<Integer> getTimeWindow() {
        return timeWindow;
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

        for (int i = 0; i < timeWindow.size(); i++) {
            System.out.print(timeWindow.get(i) +" ");
        }
        System.out.println();

        // 提前执行请求


        Bucket bucket = priorityQueue.peek();
        if (bucket == null || bucket.getExpire() > currentTimestamp) return;
        priorityQueue.poll();
        List<TimerTask> taskList = bucket.removeTaskAndGet(-1);
        System.out.println("执行请求量：" + taskList.size());
        timeWindow.removeFirst();

        // 执行具体的请求
        for (TimerTask timerTask : taskList) {
            workerThreadPool.submit(timerTask.getTask());
        }
    }

    /**
     * admitting算法
     * @return
     */
    private int admitting() {
        int sum = 0, i;
        for (i = 0; i < timeWindow.size(); i++) {
            if (i >= TIME_WINDOW_SIZE) break;
            sum += timeWindow.get(i);
        }
        int avg = sum/i;
        if (timeWindow.get(0) < avg) {

        }

        return 0;
    }

}
