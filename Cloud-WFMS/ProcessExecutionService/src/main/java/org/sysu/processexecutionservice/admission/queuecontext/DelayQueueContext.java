package org.sysu.processexecutionservice.admission.queuecontext;

import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author: Gordan Lin
 * @create: 2019/10/19
 **/
public class DelayQueueContext {

    private String url;

    private MultiValueMap<String, Object> variables;

    private Long expireExecuteTime;

    private Long requestTime;

    private RestTemplate restTemplate;

    public DelayQueueContext(String url, MultiValueMap<String, Object> variables, Long expireExecuteTime, Long requestTime, RestTemplate restTemplate) {
        this.url = url;
        this.variables = variables;
        this.expireExecuteTime = expireExecuteTime;
        this.requestTime = requestTime;
        this.restTemplate = restTemplate;
    }

    public Long getExpireExecuteTime() {
        return expireExecuteTime;
    }

    public void setExpireExecuteTime(Long expireExecuteTime) {
        this.expireExecuteTime = expireExecuteTime;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MultiValueMap<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(MultiValueMap<String, Object> variables) {
        this.variables = variables;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
    }
}
