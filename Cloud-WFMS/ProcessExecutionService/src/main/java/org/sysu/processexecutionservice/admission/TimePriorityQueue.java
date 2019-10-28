package org.sysu.processexecutionservice.admission;

import org.sysu.processexecutionservice.admission.queuecontext.DelayQueueContext;
import org.sysu.processexecutionservice.scheduler.rule.MyRoundRobinRule;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author: Gordan Lin
 * @create: 2019/10/27
 **/
public class TimePriorityQueue {

    public static PriorityBlockingQueue<DelayQueueContext> queue = new PriorityBlockingQueue<>(2000, new Comparator<DelayQueueContext>() {
        @Override
        public int compare(DelayQueueContext c1, DelayQueueContext c2) {
            if (c1.getExpireExecuteTime() > c2.getExpireExecuteTime()) return 1;
            return -1;
        }
    });

    static {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int count = ExecuteQueue.requestCounts.intValue();
                    if ((!queue.isEmpty()) && count < 80) {
                        DelayQueueContext delayQueueContext = queue.poll();
//                        Long expireExecuteTime = delayQueueContext.getExpireExecuteTime();
//                        Long remain = expireExecuteTime-System.currentTimeMillis();
//                        if (remain <= 0) {
//                            ExecuteQueue.queue.offer(delayQueueContext);
//                        }

                        ExecuteQueue.queue.offer(delayQueueContext);
                    }
                }
            }
        });
        thread1.start();
    }

}
