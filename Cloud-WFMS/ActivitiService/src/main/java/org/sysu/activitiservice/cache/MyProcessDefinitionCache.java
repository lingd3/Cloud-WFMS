package org.sysu.activitiservice.cache;

import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.sysu.activitiservice.util.CommonUtil;
import redis.clients.jedis.Jedis;

import java.io.*;

/**
 * 自定义缓存处理类
 * 缓存流程模型
 * @author: Gordan Lin
 * @create: 2019/12/21
 **/
public class MyProcessDefinitionCache implements DeploymentCache<ProcessDefinitionEntity> {

    // 实例化Jedis类
    private static Jedis jedis = new Jedis("192.168.0.93", 6379);

    @Override
    public ProcessDefinitionEntity get(String id) {
        // 获取数据
        byte[] bs = jedis.get(id.getBytes());
        if (bs == null) {
            return null;
        }
        // 将二进制数据转换为ProcessDefinitionEntity对象
        Object object = CommonUtil.toObject(bs);
        if (object == null) {
            return null;
        }
        ProcessDefinitionEntity pdf = (ProcessDefinitionEntity) object;
        return pdf;
    }

    @Override
    public void add(String id, ProcessDefinitionEntity object) {
        // 添加到缓存，因为value为object对象，所以需要将该对象转化为二进制进行存储
        jedis.set(id.getBytes(), CommonUtil.toByteArray(object));
    }

    @Override
    public void remove(String id) {
        // 删除缓存
        jedis.del(id.getBytes());
    }

    @Override
    public void clear() {

    }

}
