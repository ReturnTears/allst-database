package com.allst.redis;

import com.alibaba.fastjson.JSONObject;
import com.allst.redis.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import javax.annotation.Resource;
import java.util.Set;

/**
 * redis常用功能测试类
 */
@SpringBootTest
class AllstRedisApplicationTests {

//    @Autowired
//    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void contextRedisLoads() {
        redisTemplate.opsForList().leftPush("user:3", "hello world");
    }

    @Test
    void contextLoads() {
        redisTemplate.opsForValue().set("addr", "重庆市渝北区");
    }

    @Test
    void redisLoads() {
        String addr = redisTemplate.opsForValue().get("addr").toString();
        System.out.println(addr);
    }

    @Test
    void redisSetLoads() {
        redisUtil.set("k2", "Hello Redis ~");
    }

    @Test
    void redisSetLoads2() {
        String[] values = {"Hello", "World", "Redis"};
        boolean result = redisUtil.set2("k4", values);
        System.out.println(result);
    }

    @Test
    void redisKeysLoads() {
        Set<String> set = redisUtil.keys("*");
        assert set != null;
        set.forEach(System.out::println);
    }

    /**
     * 为已存在的键设置过期时间有效
     */
    @Test
    void redisExpireLoads() {
        boolean result = redisUtil.expire("k2", 30);
        System.out.println(result);
    }

    @Test
    void redisGetExpireLoads() {
        long result = redisUtil.getExpire("k2");
        System.out.println(result);
    }

    @Test
    void redisHasKeyLoads() {
        boolean result = redisUtil.hasKey("k2");
        System.out.println(result);
    }

    @Test
    void redisDelLoads() {
        redisUtil.del("k2", "k3");
    }

    /**
     * jedis测试
     */
    @Test
    void jedisLoads() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        System.out.println(jedis.ping());
        jedis.set("user:name", "xiaohu");
        System.out.println(jedis.get("user:name"));
    }

    /**
     * jedis基本命令操作
     */
    @Test
    void jedisBasicLoads() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        System.out.println("清空数据:" + jedis.flushDB());
        System.out.println("判断某个键是否存在:" + jedis.exists("user"));
        System.out.println("新增:" + jedis.set("user:name", "XiaoMing"));
        System.out.println("新增:" + jedis.set("user:age", "18"));
        System.out.println("新增:" + jedis.set("user:sex", "18"));
        Set<String> keys = jedis.keys("*");
        System.out.println(keys);
        System.out.println("删除键:" + jedis.del("user:sex"));
        System.out.println("查看键类型:" + jedis.type("user:age"));
        System.out.println("随机返回一个key: " + jedis.randomKey());
        System.out.println("重名key: " + jedis.rename("user:name", "user:dename"));
        System.out.println("索引查询: " + jedis.select(0));
        System.out.println("key数量: " + jedis.dbSize());
        //System.out.println("清库：" + jedis.flushAll());
    }

    @Test
    void jedisMultiLoads() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        JSONObject object = new JSONObject();
        object.put("name", "JUN");
        object.put("age", 18);
        Transaction multip = jedis.multi();
        try {
            multip.set("user:1", object.toJSONString());
            multip.set("user:2", object.toJSONString());
            multip.exec();
        } catch (Exception e) {
            multip.discard();
            e.printStackTrace();
        } finally {
            System.out.println(jedis.get("user:1"));
            jedis.close();
        }
    }
}
