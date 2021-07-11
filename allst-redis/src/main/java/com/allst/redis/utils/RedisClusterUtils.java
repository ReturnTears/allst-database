package com.allst.redis.utils;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

/**
 * redis集群简单使用
 *
 * @author June
 * @since 2021年07月
 */
public class RedisClusterUtils {
    public static void main(String[] args) {
        // 创建对象
        HostAndPort hostAndPort = new HostAndPort("192.168.33.100", 6379);
        JedisCluster jedisCluster = new JedisCluster(hostAndPort);
        // 操作redis集群
        jedisCluster.set("myname", "cluster-redis");
        String myname = jedisCluster.get("myname");
        System.out.println("myname: " + myname);
        jedisCluster.close();
    }
}
