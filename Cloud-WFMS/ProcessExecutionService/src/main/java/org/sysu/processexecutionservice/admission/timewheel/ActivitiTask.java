package org.sysu.processexecutionservice.admission.timewheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 引擎执行的任务
 * @author: Gordan Lin
 * @create: 2019/12/13
 **/
public class ActivitiTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ActivitiTask.class);

    private String url;

    private MultiValueMap<String, Object> variables;

    private RestTemplate restTemplate;

    public ActivitiTask(String url, MultiValueMap<String, Object> variables, RestTemplate restTemplate) {
        this.url = url;
        this.variables = variables;
        this.restTemplate = restTemplate;
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            ResponseEntity<String> result = restTemplate.postForEntity(url, variables, String.class);
            long end = System.currentTimeMillis();
            System.out.println((end-start) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
