package org.sysu.processexecutionservice.admission;

import org.springframework.http.ResponseEntity;
import org.sysu.processexecutionservice.admission.queuecontext.DelayQueueContext;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: Gordan Lin
 * @create: 2019/10/19
 **/
public class RequestQueue {

    public static LinkedBlockingQueue<DelayQueueContext> queue_rtl = new LinkedBlockingQueue<>();
    public static LinkedBlockingQueue<DelayQueueContext> queue_rtl2 = new LinkedBlockingQueue<>();
    public static LinkedBlockingQueue<DelayQueueContext> queue_rtl3 = new LinkedBlockingQueue<>();

    static {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!queue_rtl.isEmpty()) {
                        DelayQueueContext delayQueueContext = queue_rtl.peek();
                        Long expireExecuteTime = delayQueueContext.getExpireExecuteTime();
                        Long remain = expireExecuteTime-System.currentTimeMillis();
                        if (remain <= 0) {
                            queue_rtl.poll();
                            ExecuteQueue.queue.offer(delayQueueContext);
                        }
                    }
                }
            }
        });
        thread1.start();

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!queue_rtl2.isEmpty()) {
                        DelayQueueContext delayQueueContext = queue_rtl2.peek();
                        Long expireExecuteTime = delayQueueContext.getExpireExecuteTime();
                        Long remain = expireExecuteTime-System.currentTimeMillis();
                        if (remain <= 0) {
                            queue_rtl2.poll();
                            ExecuteQueue.queue.offer(delayQueueContext);
                        }
                    }
                }
            }
        });
        thread2.start();

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!queue_rtl3.isEmpty()) {
                        DelayQueueContext delayQueueContext = queue_rtl3.peek();
                        Long expireExecuteTime = delayQueueContext.getExpireExecuteTime();
                        Long remain = expireExecuteTime-System.currentTimeMillis();
                        if (remain <= 0) {
                            queue_rtl3.poll();
                            ExecuteQueue.queue.offer(delayQueueContext);
                        }
                    }
                }
            }
        });
        thread3.start();
    }

}
