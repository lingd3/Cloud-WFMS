package org.sysu.processexecutionservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.sysu.processexecutionservice.FileNameContext;
import org.sysu.processexecutionservice.admission.ActivitiExecuteAdmissionor;
import org.sysu.processexecutionservice.admission.requestcontext.ActivitiExecuteRequestContext;
import org.sysu.processexecutionservice.scheduler.Scheduler;
import org.sysu.processexecutionservice.util.CommonUtil;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.util.Map;

@Service
public class ActivitiService {
    private static Logger logger = LoggerFactory.getLogger(ActivitiService.class);


    @Autowired
    private Scheduler scheduler;

    private String activitiQueryService = "activiti-query-service";
    private String activitiExecutionService = "activiti-execute-service";

//    rtl级别的请求文件
//    private FileWriter writerForRTL0;
//    private FileWriter writerForRTL1;
//    private FileWriter writerForRTL2;
//
//    private String fileNameForRTL0 = FileNameContext.fileNameForRTL0;
//    private String fileNameForRTL1 = FileNameContext.fileNameForRTL1;
//    private String fileNameForRTL2 = FileNameContext.fileNameForRTL2;

//    @PostConstruct
//    private void init() {
//        try {
//            writerForRTL0 = new FileWriter(fileNameForRTL0);
//            writerForRTL1 = new FileWriter(fileNameForRTL1);
//            writerForRTL2 = new FileWriter(fileNameForRTL2);
//        } catch (Exception e) {
//            System.out.println(e.toString());
//        }
//    }

    public ResponseEntity<?> getCurrentSingleTask(String processInstanceId) {
        String url = "http://" + this.activitiQueryService + "/getCurrentSingleTask/" + processInstanceId;
        ResponseEntity<String> result = scheduler.getForEntity(url, String.class);
        return result;
    }

    public ResponseEntity<?> getCurrentTasks(String processInstanceId) {
        String url = "http://" + this.activitiQueryService + "/getCurrentTasks" + processInstanceId;
        ResponseEntity<String> result = scheduler.getForEntity(url, String.class);
        return result;
    }

    public ResponseEntity<?> startProcessInstanceByKey(Map<String, Object> variables, String processModelKey) {
//        String url = "http://" + this.activitiStartService + "/startProcessInstanceByKey/" + processModelKey;
        String url = "http://" + this.activitiExecutionService+ "/startProcessInstanceByKey/" + processModelKey;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = scheduler.postForEntity(url, valueMap, String.class);
        return result;
    }

    public ResponseEntity<?> startProcessInstanceById(Map<String, Object> variables, String processDefinitionId) {
//        String url = "http://" + this.activitiStartService + "/startProcessInstanceById/" + processDefinitionId;
        String url = "http://" + this.activitiExecutionService+ "/startProcessInstanceById/" + processDefinitionId;
//        封装参数，要使用MultiValueMap，不可以用Map或是HashMap
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = scheduler.postForEntity(url, valueMap, String.class);
        return result;
    }

    public ResponseEntity<?> complete(Map<String, Object> variables, String processDefinitionId, String processInstanceId, String taskId) {
        String url = "http://" + this.activitiExecutionService + "/completeTask/" + processDefinitionId + "/" + processInstanceId + "/" + taskId;
//        封装参数，要使用MultiValueMap，不可以用Map或是HashMap
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = scheduler.postForEntity(url, valueMap, String.class);
        return result;
    }

////    异步处理，不立即返回客户端结果
//    public ResponseEntity<?> completeTask(Map<String, Object> variables, String processInstanceId, String taskId) {
//        String url = "http://" + this.activitiExecutionService + "/completeTask/" + processInstanceId + "/" + taskId;
////      取出rtl
//        String rtl = (String) variables.get("rtl");
//        variables.remove("rtl");
////      封装参数
//        MultiValueMap valueMap = CommonUtil.map2MultiValueMap(variables);
//        ActivitiExecuteRequestContext activitiExecuteRequestContext = new ActivitiExecuteRequestContext(rtl, url, valueMap, this.restTemplate);
////        同步的处理方式
//        this.activitiExecuteAdmissionor.admit(activitiExecuteRequestContext);
////      主要还是如何处理请求线程和获取响应方式的问题； 如果同步的话，线程会一直占用，而且怎么个获取方法；
////      如果不同步的话，要怎么获取响应;
////      直接返回吧；完全符合前端的模拟就可以了；
//        return ResponseEntity.ok("请求正在调度中");
//    }
////    异步处理，返回客户端结果
//    public ResponseEntity<?> completeTaskWithFutureTask(Map<String, Object> variables, String processDefinitionId, String processInstanceId, String taskId) {
//        long startTime = System.currentTimeMillis();// 请求到达时间
//        long endTime; //请求完成时间
//        String url = "http://" + this.activitiExecutionService
//                    + "/completeTask/" + processDefinitionId + "/" + processInstanceId + "/" + taskId;
////      取出rtl
//        String rtl = (String) variables.get("rtl");
//        logger.info("complete task with rtl:" + rtl);
//        variables.remove("rtl");
//        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
//        ActivitiExecuteRequestContext activitiExecuteRequestContext = new ActivitiExecuteRequestContext(rtl, url, valueMap, this.restTemplate);
//        this.activitiExecuteAdmissionor.admit(activitiExecuteRequestContext);
//        try {
//            ResponseEntity<?> result = activitiExecuteRequestContext.getFutureTask().get();
//            endTime = System.currentTimeMillis();
//            //记录响应时间
//            if(rtl.equals("0")) {
//                writerForRTL0.write("" + (endTime - startTime) + "\r\n");
//                writerForRTL0.flush();
//            } else if(rtl.equals("1")) {
//                writerForRTL1.write("" + (endTime - startTime) + "\r\n");
//                writerForRTL1.flush();
//            } else if(rtl.equals("2")) {
//                writerForRTL2.write("" + (endTime - startTime) + "\r\n");
//                writerForRTL2.flush();
//            }
//            System.out.println(result);
//            return result;
//        } catch (Exception e) {
//            return ResponseEntity.ok(e.toString());
//        }
//    }
}
