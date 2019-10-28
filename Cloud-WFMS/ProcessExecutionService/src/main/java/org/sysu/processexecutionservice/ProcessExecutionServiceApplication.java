package org.sysu.processexecutionservice;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.sysu.processexecutionservice.scheduler.rule.LBLMBRule;
import org.sysu.processexecutionservice.scheduler.rule.MyRandomRule;
import org.sysu.processexecutionservice.scheduler.rule.MyRoundRobinRule;

@SpringBootApplication
public class ProcessExecutionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcessExecutionServiceApplication.class, args);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public IRule myRule() {
        // 比较的三种rule
//        return new MyRandomRule();
//        return new LBLMBRule();
        return new MyRoundRobinRule();
    }

}
