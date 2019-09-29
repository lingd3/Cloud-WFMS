package org.sysu.processexecutionservice.scheduler.rule;


import com.google.common.util.concurrent.AtomicDouble;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class MyRandomRule extends AbstractLoadBalancerRule {

    private static Logger logger = LoggerFactory.getLogger(MyRandomRule.class);

    Random rand;

    public static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);

    public FileWriter writerForBusyness = null;

    private final Map<Server, AtomicInteger> serverRequestCounts = new ConcurrentHashMap<>();

    //服务器的繁忙度（类似ER，包含了历史数据，每秒更新一次）
    private final Map<Server, AtomicDouble> serverBusyness = new ConcurrentHashMap<>();

    private final Map<Server, AtomicInteger> serverRequestCountsIn5seconds = new ConcurrentHashMap<>();

    private final double historyRate = 0.6;
    private final int engineMaxRequest = 60;
    private boolean flag = false; //是否初始化过init

    public void init() {
        List<Server> servers =  getLoadBalancer().getAllServers();
        for(Server server : servers) {
            this.serverRequestCounts.put(server, new AtomicInteger());
            this.serverBusyness.put(server, new AtomicDouble());
            this.serverRequestCountsIn5seconds.put(server, new AtomicInteger());
        }
        try {
//            writerForBusyness = new FileWriter("D:\\lb\\random_busyness.txt");
            writerForBusyness = new FileWriter("random_busyness.txt");
        } catch (IOException e) {

        }
        MyRandomRule.Task task = new MyRandomRule.Task();
        MyRandomRule.WriteLogTask writeLogTask = new MyRandomRule.WriteLogTask();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(writeLogTask, 5, 5, TimeUnit.SECONDS);
    }


    public MyRandomRule() {
        rand = new Random();
    }

    /**
     * Randomly choose from all living servers
     */
    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            return null;
        }
        Server server = null;

        while (server == null) {
            if (Thread.interrupted()) {
                return null;
            }
            List<Server> upList = lb.getReachableServers();
            List<Server> allList = lb.getAllServers();

            int serverCount = allList.size();
            if (serverCount == 0) {
                /*
                 * No servers. End regardless of pass, because subsequent passes
                 * only get more restrictive.
                 */
                return null;
            }

            int index = rand.nextInt(serverCount);
            server = upList.get(index);

            if (server == null) {
                /*
                 * The only time this should happen is if the server list were
                 * somehow trimmed. This is a transient condition. Retry after
                 * yielding.
                 */
                Thread.yield();
                continue;
            }

            if (server.isAlive()) {
                return (server);
            }

            // Shouldn't actually happen.. but must be transient or a bug.
            server = null;
            Thread.yield();
        }
        System.out.println(server.getHostPort());

        return server;

    }

    @Override
    public Server choose(Object key) {
        if (!flag) {
            synchronized ((Object) flag) {
                if(!flag) {
                    init();
                    flag = true;
                }
            }
        }
        Server server = choose(getLoadBalancer(), key);
        this.serverRequestCounts.get(server).incrementAndGet();
        return server;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        // TODO Auto-generated method stub

    }

    public synchronized void  updateServerHistoryBusyness() {
        List<Server> servers = getLoadBalancer().getAllServers();
        String log2 = "random:";
        for(Server server : servers) {
            AtomicInteger requestCount = this.serverRequestCounts.get(server);
            double curBusyness = requestCount.doubleValue() / this.engineMaxRequest;
            double busyness = this.serverBusyness.get(server).doubleValue() * historyRate
                    + curBusyness * (1 - historyRate);
            log2 += server.getHostPort() + ": " + requestCount.intValue() + " - " + busyness + "  ";
            this.serverRequestCountsIn5seconds.get(server).addAndGet(requestCount.intValue());
            this.serverRequestCounts.get(server).set(0);
            this.serverBusyness.get(server).set(busyness);
        }
        logger.info(log2);
    }

    public synchronized void writeBusyness() {
        List<Server> servers = getLoadBalancer().getAllServers();
        String log = "";
        for(Server server : servers) {
            Double requestCount = this.serverRequestCountsIn5seconds.get(server).doubleValue()/(5*this.engineMaxRequest);
            log += new BigDecimal(requestCount + "").toString() + "   ";
            this.serverRequestCountsIn5seconds.get(server).set(0);
        }
        try {
            this.writerForBusyness.write(log + "\r\n");
            this.writerForBusyness.flush();
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }

    private class Task implements  Runnable {
        @Override
        public void run() {
            MyRandomRule.this.updateServerHistoryBusyness();
        }
    }

    private class WriteLogTask implements  Runnable {
        @Override
        public void run() {
            MyRandomRule.this.writeBusyness();
        }
    }

}
