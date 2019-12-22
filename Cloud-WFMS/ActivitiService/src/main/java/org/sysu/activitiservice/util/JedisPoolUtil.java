package org.sysu.activitiservice.util;

import org.springframework.security.access.method.P;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Jedis连接池类
 * @author: Gordan Lin
 * @create: 2019/12/22
 **/
public class JedisPoolUtil {

    private JedisPoolUtil() {}

    private static volatile JedisPool jedisPool = null;

    public static JedisPool getJedisPoolInstance() {
        if (jedisPool == null) {
            synchronized (JedisPoolUtil.class) {
                if (jedisPool == null) {
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxIdle(32);
                    poolConfig.setMaxTotal(1000);
                    poolConfig.setMaxWaitMillis(1000*10);
                    jedisPool = new JedisPool(poolConfig, "192.168.0.93", 6379);
                }
            }
        }
        return jedisPool;
    }

}
