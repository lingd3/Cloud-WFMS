package org.sysu.processexecutionservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.sysu.processexecutionservice.util.CommonUtil;

import java.util.Map;

@Service
public class ActivitiService {

    private static Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    @Autowired
    RestTemplate restTemplate;

    private String activitiExecutionService = "activiti-service";

    public ResponseEntity<?> getCurrentSingleTask(String processInstanceId) {
        String url = "http://" + this.activitiExecutionService + "/getCurrentSingleTask/" + processInstanceId;
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        return result;
    }

    public ResponseEntity<?> getCurrentTasks(String processInstanceId) {
        String url = "http://" + this.activitiExecutionService + "/getCurrentTasks" + processInstanceId;
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        return result;
    }

    public ResponseEntity<?> startProcessInstanceByKey(Map<String, Object> variables, String processModelKey) {
        String url = "http://" + this.activitiExecutionService+ "/startProcessInstanceByKey/" + processModelKey;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.postForEntity(url, valueMap, String.class);
        return result;
    }

    public ResponseEntity<?> startProcessInstanceById(Map<String, Object> variables, String processDefinitionId) {
        String url = "http://" + this.activitiExecutionService+ "/startProcessInstanceById/" + processDefinitionId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.postForEntity(url, valueMap, String.class);
        return result;
    }

    //不延迟
    public ResponseEntity<?> complete(Map<String, Object> variables, String processDefinitionId, String processInstanceId, String taskId) {
        String url = "http://" + this.activitiExecutionService + "/completeTask/" + processDefinitionId + "/" + processInstanceId + "/" + taskId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.postForEntity(url, valueMap, String.class);
        return result;
    }

    //延迟到最后时间片
//    public ResponseEntity<?> complete(Map<String, Object> variables, String processDefinitionId, String processInstanceId, String taskId) {
//        String url = "http://" + this.activitiExecutionService + "/completeTask/" + processDefinitionId + "/" + processInstanceId + "/" + taskId;
//        int rtl = Integer.valueOf((String) variables.get("rtl"));
//        variables.remove("rtl");
//        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
//        Long requestTime = System.currentTimeMillis();
//        if (rtl == 1) {
//            DelayQueueContext delayQueueContext = new DelayQueueContext(url, valueMap, requestTime+1000, requestTime, restTemplate);
//            RequestQueue.queue_rtl.offer(delayQueueContext);
//        } else if (rtl == 2) {
//            DelayQueueContext delayQueueContext = new DelayQueueContext(url, valueMap, requestTime+2000, requestTime, restTemplate);
//            RequestQueue.queue_rtl2.offer(delayQueueContext);
//        } else if (rtl == 3) {
//            DelayQueueContext delayQueueContext = new DelayQueueContext(url, valueMap, requestTime+3000, requestTime, restTemplate);
//            RequestQueue.queue_rtl3.offer(delayQueueContext);
//        }
//        return ResponseEntity.ok("请求正在调度中");
//    }
    

}
