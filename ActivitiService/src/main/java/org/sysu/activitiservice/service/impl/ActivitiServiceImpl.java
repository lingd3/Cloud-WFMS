package org.sysu.activitiservice.service.impl;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.sysu.activitiservice.service.ActivitiService;

import java.util.List;
import java.util.Map;

/**
 * 引擎服务类接口实现
 *
 * @author: Gordan Lin
 * @create: 2019/9/16
 **/
public class ActivitiServiceImpl implements ActivitiService {

    private final static Logger logger = LoggerFactory.getLogger(ActivitiServiceImpl.class);

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Override
    public ProcessInstance startProcessInstanceByKey(String processModelKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processModelKey, variables);
    }

    @Override
    public ProcessInstance startProcessInstanceById(String processInstanceId, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceById(processInstanceId, variables);
    }

    @Override
    public List<Task> getCurrentTasks(String processInstancedId) {
        return taskService.createTaskQuery().processInstanceId(processInstancedId).list();
    }

    @Override
    public List<Task> getCurrentTasks(String processInstancedId, String assignee) {
        return taskService.createTaskQuery().processInstanceId(processInstancedId).taskAssignee(assignee).list();
    }

    @Override
    public Task getCurrentSingleTask(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    }

    @Override
    public boolean claimTask(String taskId, String assignee) {
        taskService.claim(taskId, assignee);
        return true;
    }

    @Override
    public boolean completeTask(String processInstanceId, String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
        return true;
    }

    @Override
    public boolean isEnded(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance != null) {
            return processInstance.isEnded();
        }
        return true;
    }
}
