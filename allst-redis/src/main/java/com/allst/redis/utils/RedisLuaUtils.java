package com.allst.redis.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

/**
 * Redis调用Lua脚本
 *
 * @author June
 * @since 2021年07月
 */
public class RedisLuaUtils {

    public static void main(String[] args) {
        JedisPool jedispool = JedisPoolUtil.getJedisPoolInstance();
        Jedis jedis = jedispool.getResource();
        System.out.println(jedis.ping());
    }

    static String secKillScript = "local userId=KEYS[1];\r\n" +
            "local prodId=KEYS[2];\r\n" +
            "local kcKey='sk:'..prodId..\":qt\";\r\n" +
            "local usersKey='sk:'..prodId..\":user\";\r\n" +
            "local userExists=redis.call(\"sismember\",usersKey,userId);\r\n" +
            "if tonumber(userExists)==1 then \r\n" +
            "   return 2;\r\n" +
            "end\r\n" +
            "local num= redis.call(\"get\" ,kcKey);\r\n" +
            "if tonumber(num)<=0 then \r\n" +
            "   return 0;\r\n" +
            "else \r\n" +
            "   redis.call(\"decr\",kcKey);\r\n" +
            "   redis.call(\"sadd\",usersKey,userId);\r\n" +
            "end\r\n" +
            "return 1";

    static String secKillScript2 =
            "local userExists=redis.call(\"sismember\",\"{sk}:0101:user\",userId);\r\n" +
                    " return 1";

    public static boolean doSecKill(String userId, String prodId) throws IOException {

        JedisPool jedispool = JedisPoolUtil.getJedisPoolInstance();
        Jedis jedis = jedispool.getResource();

        //String sha1=  .secKillScript;
        String sha1 = jedis.scriptLoad(secKillScript);
        Object result = jedis.evalsha(sha1, 2, userId, prodId);

        String reString = String.valueOf(result);
        if ("0".equals(reString)) {
            System.err.println("已抢空！！");
        } else if ("1".equals(reString)) {
            System.out.println("抢购成功！！！！");
        } else if ("2".equals(reString)) {
            System.err.println("该用户已抢过！！");
        } else {
            System.err.println("抢购异常！！");
        }
        jedis.close();
        return true;
    }
}
