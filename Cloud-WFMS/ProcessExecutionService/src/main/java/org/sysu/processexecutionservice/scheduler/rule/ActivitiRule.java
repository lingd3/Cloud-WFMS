package org.sysu.processexecutionservice.scheduler.rule;


import com.netflix.loadbalancer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ActivitiRule extends BestAvailableRule {

    private static Logger logger = LoggerFactory.getLogger(ActivitiRule.class);

    public static Random rand = new Random();

    public static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    public static Map<String, AtomicInteger> requestsCount = new ConcurrentHashMap<>();

    static {
        requestsCount.put("192.168.0.99", new AtomicInteger());
        requestsCount.put("192.168.0.85", new AtomicInteger());
        requestsCount.put("192.168.0.55", new AtomicInteger());
        ActivitiRule.Task task = new ActivitiRule.Task();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
    }


    // 维护服务器组
    private final Map<String, Set<Server>> proDefinitionIdToServerGroup =
            Collections.synchronizedMap(new LinkedHashMap<String, Set<Server>>(200, 0.75f, true));

    public ActivitiRule() {
        super();
    }

    public Server chooseServer(ILoadBalancer lb, Object key) {
        if(lb == null) {
            logger.warn("no load balancer");
            return null;
        }
        LoadBalancerStats loadBalancerStats = ((AbstractLoadBalancer)lb).getLoadBalancerStats();
        if (loadBalancerStats == null) {
            return super.choose(key);
        }

        Server server = null;
        List<Server> reachableServers = lb.getReachableServers();
        int upCount = reachableServers.size();
        if((upCount == 0)) {
            logger.warn("no up servers available from load balancer");
            return null;
        }
        server = _choose(reachableServers, loadBalancerStats, key);
        if (server == null) {
            return super.choose(key);
        }
        else {
            return server;
        }
    }

    private Server _choose(List<Server> reachableServers, LoadBalancerStats stats, Object key) {
        if (stats == null) {
            logger.warn("no statistics, nothing to do so");
            return null;
        }

        String processDefinitionId = (String) key;
        Set<Server> servers = proDefinitionIdToServerGroup.get(processDefinitionId);
        Server result = null;
        // 第一次执行流程定义
        if (servers == null || servers.size() == 0) {
            int index = rand.nextInt(reachableServers.size());
            result = reachableServers.get(index);
            servers = new HashSet<>();
            servers.add(result);
            proDefinitionIdToServerGroup.put(processDefinitionId, servers);
        }
        // 否则从之前的引擎中选择
        else {
            List<Server> previousServerList = new ArrayList<>(servers);
            result = chooseMinConcurrentFromServerGroup(reachableServers, previousServerList, stats);
            if (result == null) {
                result = super.choose(key);
            }
            servers.add(result);
            proDefinitionIdToServerGroup.put(processDefinitionId, servers);
        }
        AtomicInteger temp = requestsCount.get(result.getHost());
        temp.incrementAndGet();
        requestsCount.put(result.getHost(), temp);
        return result;
    }

    private Server chooseMinConcurrentFromServerGroup(List<Server> reachableServers, List<Server> previousServerList, LoadBalancerStats stats) {
        Server result = null;
        int minCount = Integer.MAX_VALUE;


        return result;
    }

    public Server choose(Object key) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String uri = request.getRequestURI();

        //获取processDefinitionId的值
        String processDefinitionId = "";
        int startIndex = uri.indexOf('/', 1)+1;
        int endIndex = uri.indexOf('/', startIndex);
        if (endIndex == -1) endIndex = uri.length();
        processDefinitionId = uri.substring(startIndex, endIndex);

        return chooseServer(getLoadBalancer(), processDefinitionId);
    }

    private static class Task implements Runnable {
        @Override
        public void run() {
            System.out.println("请求总量：");
            for (String key : requestsCount.keySet()) {
                AtomicInteger temp = requestsCount.get(key);
                System.out.println(key + " 请求数量：" + temp.intValue());
            }
//            System.out.println("10秒内请求数量-----------------------");
//            for (String key : requestsCount.keySet()) {
//                AtomicInteger temp = requestsCount.get(key);
//                System.out.println(key + " 请求数量：" + temp.intValue());
//                temp.set(0);
//                requestsCount.put(key, temp);
//            }
        }
    }

}
