package com.allst.redis.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

/**
 * Jedis连接池工具类
 *
 * @author June
 * @since 2021年07月
 */
public class JedisPoolUtil {

    private static volatile JedisPool jedisPool = null;

    private static volatile JedisSentinelPool jedisSentinelPool = null;

    private JedisPoolUtil() {
    }

    /**
     * 获取redis实例
     */
    public static JedisPool getJedisPoolInstance() {
        if (null == jedisPool) {
            synchronized (JedisPoolUtil.class) {
                if (null == jedisPool) {
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxTotal(200);
                    poolConfig.setMaxIdle(32);
                    poolConfig.setMaxWaitMillis(100 * 1000);
                    poolConfig.setBlockWhenExhausted(true);
                    poolConfig.setTestOnBorrow(true);  // ping  PONG

                    jedisPool = new JedisPool(poolConfig, "192.168.33.100", 6379, 60000);
                }
            }
        }
        return jedisPool;
    }

    /**
     * 获取主从复制模式下redis实例
     */
    public static Jedis getJedisFromSentinel() {
        if (jedisSentinelPool == null) {
            Set<String> sentinelSet = new HashSet<>();
            sentinelSet.add("192.168.33.100:6379");
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10); // 最大可用连接数
            poolConfig.setMaxIdle(5); // 最大闲置连接数
            poolConfig.setMinIdle(5); // 最小闲置连接数
            poolConfig.setMaxWaitMillis(2000); // 等待时间
            poolConfig.setBlockWhenExhausted(true); // 连接耗尽是否等待
            poolConfig.setTestOnBorrow(true);  // 取连接的时候进行一下测试 ping  PONG

            jedisSentinelPool = new JedisSentinelPool("shizhan", sentinelSet, poolConfig);
            return jedisSentinelPool.getResource();
        }
        return jedisSentinelPool.getResource();
    }

    public static void release(JedisPool jedisPool, Jedis jedis) {
        if (null != jedis) {
            // jedisPool.returnResource(jedis);
        }
    }
}
