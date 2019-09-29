package org.sysu.processexecutionservice.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Scheduler {
    @Autowired
    RestTemplate restTemplate;

    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) {
        return restTemplate.getForEntity(url, responseType);
    }

    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return restTemplate.postForEntity(url, request, responseType);
    }

}
