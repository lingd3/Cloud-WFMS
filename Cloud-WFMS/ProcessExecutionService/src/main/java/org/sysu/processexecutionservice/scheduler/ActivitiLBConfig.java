package org.sysu.processexecutionservice.scheduler;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sysu.processexecutionservice.scheduler.rule.LBEGSRule;

/**
 * 流程驱动请求使用的负载均衡策略
 * @author: Gordan Lin
 * @create: 2019/12/20
 **/
@Configuration
@RibbonClient(name = "activiti-service", configuration = ActivitiLBConfig.class)
public class ActivitiLBConfig {

    @Bean
    public IRule ribbonRule() {
        return new LBEGSRule();
    }
}
