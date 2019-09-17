package org.sysu.activitiservice.service;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 引擎服务类接口
 * @author: Gordan Lin
 * @create: 2019/9/16
 **/
@Service
public interface ActivitiService {

    /**
     * 根据流程key启动流程
     * @param processModelKey
     * @param variables
     * @return
     */
    public ProcessInstance startProcessInstanceByKey(String processModelKey, Map<String,Object> variables);

    /**
     * 根据流程id启动流程
     * @param processInstanceId
     * @param variables
     * @return
     */
    public ProcessInstance startProcessInstanceById(String processInstanceId, Map<String,Object> variables);

    /**
     * 根据流程实例获取当前任务列表
     * @param processInstancedId
     * @return
     */
    public List<Task> getCurrentTasks(String processInstancedId);

    /**
     * 根据流程实例和配置人，获取配置人的任务列表
     * @param processInstancedId
     * @param assignee
     * @return
     */
    public List<Task> getCurrentTasks(String processInstancedId, String assignee);

    /**
     * 根据流程实例获取当前可执行的单个任务
     * @param processInstanceId
     * @return
     */
    public Task getCurrentSingleTask(String processInstanceId);

    /**
     * 认领任务
     * @param taskId
     * @param assignee
     */
    public boolean claimTask(String taskId, String assignee);

    /**
     * 完成任务
     * @param processInstanceId
     * @param taskId
     * @param variables
     * @return
     */
    public boolean completeTask(String processInstanceId,String taskId, Map<String, Object> variables);

    /**
     * 根据流程实例Id判断流程实例是否结束
     * @param processInstanceId
     * @return
     */
    public boolean isEnded(String processInstanceId);

}
