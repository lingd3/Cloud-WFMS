package org.sysu.processexecutionservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import java.util.List;

/**
 * Jedis工具类
 * @author: Gordan Lin
 * @create: 2019/12/22
 **/
public class JedisUtil {

    private final static Logger logger = LoggerFactory.getLogger(JedisUtil.class);

    public static long zadd(String key, double score, String value) {
        Jedis jedis = null;
        try {
            jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
            return jedis.zadd(key, score, value);
        } catch (Exception e) {
            logger.error("zadd发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public static long zcount(String key, long min, long max) {
        Jedis jedis = null;
        try {
            jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
            return jedis.zcount(key, min, max);
        } catch (Exception e) {
            logger.error("zcount发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public static long zrem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
            return jedis.zrem(key, value);
        } catch (Exception e) {
            logger.error("zrem发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public static List<Tuple> zscan(String key, int cursor) {
        Jedis jedis = null;
        try {
            jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
            return jedis.zscan(key, 0).getResult();
        } catch (Exception e) {
            logger.error("zscan发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

}
