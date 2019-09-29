/*
 *
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.sysu.processexecutionservice.scheduler.rule;


import com.google.common.util.concurrent.AtomicDouble;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * The most well known and basic load balancing strategy, i.e. Round Robin Rule.
 *
 * @author stonse
 * @author Nikos Michalakis <nikos@netflix.com>
 *
 */
public class MyRoundRobinRule extends AbstractLoadBalancerRule {

    private static Logger logger = LoggerFactory.getLogger(MyRoundRobinRule.class);

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
//            writerForBusyness = new FileWriter("D:\\lb\\roundrobin_busyness.txt");
            writerForBusyness = new FileWriter("roundrobin_busyness.txt");
        } catch (IOException e) {

        }
        MyRoundRobinRule.Task task = new MyRoundRobinRule.Task();
        MyRoundRobinRule.WriteLogTask writeLogTask = new MyRoundRobinRule.WriteLogTask();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(writeLogTask, 5, 5, TimeUnit.SECONDS);
    }

    private AtomicInteger nextServerCyclicCounter;
    private static final boolean AVAILABLE_ONLY_SERVERS = true;
    private static final boolean ALL_SERVERS = false;

    private static Logger log = LoggerFactory.getLogger(MyRoundRobinRule.class);

    public MyRoundRobinRule() {
        nextServerCyclicCounter = new AtomicInteger(0);
    }

    public MyRoundRobinRule(ILoadBalancer lb) {
        this();
        setLoadBalancer(lb);
    }

    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        }

        Server server = null;
        int count = 0;
        while (server == null && count++ < 10) {
            List<Server> reachableServers = lb.getReachableServers();
            List<Server> allServers = lb.getAllServers();
            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if ((upCount == 0) || (serverCount == 0)) {
                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }

            int nextServerIndex = incrementAndGetModulo(serverCount);
            server = allServers.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }

            // Next.
            server = null;
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + lb);
        }
        return server;
    }

    /**
     * Inspired by the implementation of {@link AtomicInteger#incrementAndGet()}.
     *
     * @param modulo The modulo to bound the value of the counter.
     * @return The next value.
     */
    private int incrementAndGetModulo(int modulo) {
        for (;;) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next))
                return next;
        }
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
            MyRoundRobinRule.this.updateServerHistoryBusyness();
        }
    }

    private class WriteLogTask implements  Runnable {
        @Override
        public void run() {
            MyRoundRobinRule.this.writeBusyness();
        }
    }
}
