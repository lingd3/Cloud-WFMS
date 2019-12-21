package org.sysu.activitiservice.cache;

import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
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
        //


        return null;
    }

    @Override
    public void add(String id, ProcessDefinitionEntity object) {

    }

    @Override
    public void remove(String id) {

    }

    @Override
    public void clear() {

    }

    /**
     * 将对象转化为byte数组
     */
    public static byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {

        }
        return bytes;
    }

    /**
     * 将byte数组转化为对象
     */
    public static Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception e) {

        }
        return obj;
    }
}
