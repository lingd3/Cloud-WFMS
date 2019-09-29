package org.sysu.processexecutionservice.scheduler.rule;

import com.google.common.util.concurrent.AtomicDouble;
import com.netflix.loadbalancer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Gordan Lin
 * @create: 2019/4/18
 **/
@Component
public class MyBestAvailableRule extends ClientConfigEnabledRoundRobinRule {

    private static Logger logger = LoggerFactory.getLogger(MyBestAvailableRule.class);

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
            writerForBusyness = new FileWriter("D:\\lb\\bestavailable_busyness.txt");
//            writerForBusyness = new FileWriter("bestavailable_busyness.txt");
        } catch (IOException e) {

        }
        MyBestAvailableRule.Task task = new MyBestAvailableRule.Task();
        MyBestAvailableRule.WriteLogTask writeLogTask = new MyBestAvailableRule.WriteLogTask();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(writeLogTask, 5, 5, TimeUnit.SECONDS);
    }

    public MyBestAvailableRule() {
        super();
    }

    private LoadBalancerStats loadBalancerStats;

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
        if (loadBalancerStats == null) {
            return super.choose(key);
        }
        List<Server> serverList = getLoadBalancer().getAllServers();
        int minimalConcurrentConnections = Integer.MAX_VALUE;
        long currentTime = System.currentTimeMillis();
        Server chosen = null;
        for (Server server: serverList) {
            ServerStats serverStats = loadBalancerStats.getSingleServerStat(server);
            if (!serverStats.isCircuitBreakerTripped(currentTime)) {
                int concurrentConnections = serverStats.getActiveRequestsCount(currentTime);
                if (concurrentConnections < minimalConcurrentConnections) {
                    minimalConcurrentConnections = concurrentConnections;
                    chosen = server;
                }
            }
        }
        if (chosen == null) {
            chosen = super.choose(key);
        }
        this.serverRequestCounts.get(chosen).incrementAndGet();
        return chosen;
    }

    @Override
    public void setLoadBalancer(ILoadBalancer lb) {
        super.setLoadBalancer(lb);
        if (lb instanceof AbstractLoadBalancer) {
            loadBalancerStats = ((AbstractLoadBalancer) lb).getLoadBalancerStats();
        }
    }

    public synchronized void  updateServerHistoryBusyness() {
        List<Server> servers = getLoadBalancer().getAllServers();
        String log2 = "bestavailable:";
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
            MyBestAvailableRule.this.updateServerHistoryBusyness();
        }
    }

    private class WriteLogTask implements  Runnable {
        @Override
        public void run() {
            MyBestAvailableRule.this.writeBusyness();
        }
    }
}
