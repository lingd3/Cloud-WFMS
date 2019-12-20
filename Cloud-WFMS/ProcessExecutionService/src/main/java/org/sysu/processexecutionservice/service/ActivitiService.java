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

    private static final String ACTIVITI_SERVICE = "activiti-service";
    private static final String QUERY_SERVICE = "query-service";

    public ResponseEntity<?> getCurrentSingleTask(String processInstanceId) {
        String url = "http://" + QUERY_SERVICE + "/getCurrentSingleTask/" + processInstanceId;
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        return result;
    }

    public ResponseEntity<?> getCurrentTasks(String processInstanceId) {
        String url = "http://" + ACTIVITI_SERVICE + "/getCurrentTasks" + processInstanceId;
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        return result;
    }

    public ResponseEntity<?> startProcessInstanceByKey(Map<String, Object> variables, String processModelKey) {
        String url = "http://" + ACTIVITI_SERVICE+ "/startProcessInstanceByKey/" + processModelKey;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.postForEntity(url, valueMap, String.class);
        return result;
    }

    public ResponseEntity<?> startProcessInstanceById(Map<String, Object> variables, String processDefinitionId) {
        String url = "http://" + ACTIVITI_SERVICE+ "/startProcessInstanceById/" + processDefinitionId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.postForEntity(url, valueMap, String.class);
        return result;
    }

//    public ResponseEntity<?> startProcessInstanceById(Map<String, Object> variables, String processDefinitionId) {
//        String url = "http://" + ACTIVITI_SERVICE+ "/startProcessInstanceById/" + processDefinitionId;
//        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
//        ActivitiTask activitiTask = new ActivitiTask(url, valueMap, restTemplate);
//        TimerTask timerTask = new TimerTask(3000, activitiTask);
//        Timer.getInstance().addTask(timerTask);
//        return ResponseEntity.ok("请求正在调度中");
//    }

    //不延迟
    public ResponseEntity<?> complete(Map<String, Object> variables, String processDefinitionId, String processInstanceId, String taskId) {
        String url = "http://" + ACTIVITI_SERVICE + "/completeTask/" + processDefinitionId + "/" + processInstanceId + "/" + taskId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.postForEntity(url, valueMap, String.class);
        return result;
    }

    //延迟请求
//    public ResponseEntity<?> complete(Map<String, Object> variables, String processDefinitionId, String processInstanceId, String taskId) {
//        String url = "http://" + ACTIVITI_SERVICE + "/completeTask/" + processDefinitionId + "/" + processInstanceId + "/" + taskId;
//        // 延时级别rtl
//        int rtl = Integer.valueOf((String) variables.get("rtl"));
//        variables.remove("rtl");
//        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
//
//        ActivitiTask activitiTask = new ActivitiTask(url, valueMap, restTemplate);
//        TimerTask timerTask = new TimerTask(rtl*1000, activitiTask);
//        Timer.getInstance().addTask(timerTask);
//        return ResponseEntity.ok("请求正在调度中");
//    }

}
