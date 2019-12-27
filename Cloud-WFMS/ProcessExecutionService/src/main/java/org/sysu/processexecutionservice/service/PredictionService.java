package org.sysu.processexecutionservice.service;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sysu.processexecutionservice.admission.timewheel.LoadPrediction;
import org.sysu.processexecutionservice.util.JedisUtil;

import java.util.List;

/**
 * @author: Gordan Lin
 * @create: 2019/12/27
 **/
@Service
public class PredictionService {

    private static Logger logger = LoggerFactory.getLogger(PredictionService.class);

    @Autowired
    TaskService taskService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    public void addData(String processDefinitionId, String taskId) {

        //        JedisUtil.zadd(LoadPrediction.REDIS_KEY, System.currentTimeMillis()+8000, processDefinitionId+"-"+taskId);

        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)(((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processDefinitionId));
        List<ActivityImpl> activityList = processDefinitionEntity.getActivities();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String excId = task.getExecutionId();
        ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(excId).singleResult();
        String activitiId = execution.getActivityId();
        for (ActivityImpl activityImpl : activityList){
            String id = activityImpl.getId();
            if(activitiId.equals(id)){
                List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();//获取从某个节点出来的所有线路
                for (PvmTransition tr : outTransitions){
                    PvmActivity ac = tr.getDestination(); //获取线路的终点节点

                    // 请求到达时间需要再解决，目前暂定为8000ms
                    if (ac.getProperty("name") != null) {
                        JedisUtil.zadd(LoadPrediction.REDIS_KEY, System.currentTimeMillis()+8000, processDefinitionId+"-"+taskId);
                    }
                }
                break;
            }
        }
    }

}
