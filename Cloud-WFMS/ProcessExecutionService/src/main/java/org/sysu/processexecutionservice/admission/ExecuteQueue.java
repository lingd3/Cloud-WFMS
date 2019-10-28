package org.sysu.processexecutionservice.admission;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.sysu.processexecutionservice.admission.queuecontext.DelayQueueContext;
import org.sysu.processexecutionservice.admission.requestcontext.ActivitiExecuteRequestContext;
import org.sysu.processexecutionservice.admission.requestcontext.IRequestContext;
import org.sysu.processexecutionservice.scheduler.rule.MyRoundRobinRule;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Gordan Lin
 * @create: 2019/10/19
 **/
@Component
public class ExecuteQueue {

    public FileWriter writerForBusyness = null;

    public static LinkedBlockingQueue<DelayQueueContext> queue = new LinkedBlockingQueue<>();

    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);

    public static AtomicInteger requestCounts = new AtomicInteger();
    public static AtomicInteger failCounts = new AtomicInteger();

    public void offer(DelayQueueContext delayQueueContext) {
        queue.offer(delayQueueContext);
    }

    public ExecuteQueue() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!queue.isEmpty()) {
                        DelayQueueContext delayQueueContext = queue.poll();
                        if (requestCounts.intValue() > 80) {
                            failCounts.incrementAndGet();
                        }
                        else {
                            requestCounts.incrementAndGet();
                            executorService.execute(new FutureTask<>(new Task(delayQueueContext)));
                        }
                    }
                }
            }
        });
        thread.start();
        try {
            writerForBusyness = new FileWriter("D:\\lb\\responseTime3.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ExecuteQueue.CountTask countTask = new ExecuteQueue.CountTask();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(countTask, 1, 1, TimeUnit.SECONDS);
    }

    public synchronized void writeResponseTime(Long time) {
//        try {
//            this.writerForBusyness.write(time + "\r\n");
//            this.writerForBusyness.flush();
//        } catch (IOException e ) {
//            e.printStackTrace();
//        }
    }

    class Task implements Callable<ResponseEntity<String>> {
        private DelayQueueContext delayQueueContext;

        public Task(DelayQueueContext delayQueueContext) {
            this.delayQueueContext = delayQueueContext;
        }

        @Override
        public ResponseEntity<String> call() throws Exception {
            ResponseEntity<String> result = delayQueueContext.getRestTemplate().postForEntity(delayQueueContext.getUrl(), delayQueueContext.getVariables(), String.class);
//            writeResponseTime(System.currentTimeMillis()-delayQueueContext.getRequestTime());
            return result;
        }
    }

    class CountTask implements Runnable {
        @Override
        public void run() {
            System.out.println(requestCounts.intValue());
            System.out.println("failCount: " + failCounts.intValue());
            requestCounts.set(0);
        }
    }
}
