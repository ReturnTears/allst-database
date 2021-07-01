package com.allst.jedis;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author June
 * @since 2021年07月
 */
public class JedisTest {
    public static void main(String... args) {
        // 连接虚拟机上测redis6.2.0服务
        Jedis jedis = new Jedis("192.168.33.100", 6379);
        // 如果连接超时，请查看centos中防火墙是否关闭
        String ping = jedis.ping();
        System.out.println(ping);
    }

    // 连接虚拟机上测redis6.2.0服务
    Jedis jedis = new Jedis("192.168.33.100", 6379);

    /**
     * String
     */
    @Test
    public void testString() {
        jedis.set("name", "ZhSan");
        jedis.mset("k5", "v5", "k6", "v6");
        Set<String> keys = jedis.keys("*");
        keys.forEach(System.out::println);
    }

    /**
     * list
     */
    @Test
    public void testList() {
        jedis.lpush("key1", "value1", "value2", "value3");
        List<String> lrange = jedis.lrange("key1", 0, -1);
        System.out.println(lrange);
    }

    /**
     * set
     */
    @Test
    public void testSet() {
        jedis.sadd("key2", "val1", "val2", "val3");
        Set<String> smembers = jedis.smembers("key2");
        System.out.println(smembers);
    }

    /**
     * Hash
     */
    @Test
    public void testHash() {
        jedis.hset("user1", "age", "18");
        String hget = jedis.hget("user1", "age");
        System.out.println(hget);

        Map<String, String> map = new HashMap<>();
        map.put("tel", "13812345678");
        map.put("address", "shanghai");
        map.put("email", "12345678@qq.com");
        jedis.hmset("user2", map);
        List<String> hmget = jedis.hmget("user2", "tel", "email");
        System.out.println(hmget);
    }

    /**
     * Zset
     */
    @Test
    public void testZSet() {
        jedis.zadd("user3", 101, "kangkang");
        jedis.zadd("user3", 102, "19");
        jedis.zadd("user3", 103, "henan");
        Set<String> user3 = jedis.zrange("user3", 0, -1);
        user3.forEach(System.out::println);
    }
}
