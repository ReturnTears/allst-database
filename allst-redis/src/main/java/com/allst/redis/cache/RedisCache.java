package com.allst.redis.cache;

import com.allst.redis.utils.ProtoStuffSerializerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Redis缓存
 * @author June
 * @version 1.0
 * @date 2018-07-02
 */
@Component
public class RedisCache {
    /**
     * 缓存名
     */
    public final static String CAHCENAME="cache";
    /**
     * 默认缓存时间
     */
    public final static int CAHCETIME=60;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 存入redis，不带过期时间
     * @param key
     * @param obj
     * @param <T>
     * @return
     */
    public <T> boolean putCache(String key, T obj) {
        final byte[] buffs = key.getBytes();
        final byte[] objs = ProtoStuffSerializerUtil.serialize(obj);
        boolean result = redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> redisConnection.setNX(buffs, objs));
        return result;
    }

    /**
     * 存入redis, 带过期时间
     * @param key
     * @param obj
     * @param expireTime
     * @param <T>
     */
    public <T> void putCacheWithExpireTime(String key, T obj, final long expireTime) {
        final byte[] bkey = key.getBytes();
        final byte[] bvalue = ProtoStuffSerializerUtil.serialize(obj);
        redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            connection.setEx(bkey, expireTime, bvalue);
            return true;
        });
    }

    public <T> boolean putListCache(String key, List<T> objList) {
        final byte[] bkey = key.getBytes();
        final byte[] bvalue = ProtoStuffSerializerUtil.serializeList(objList);
        boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.setNX(bkey, bvalue));
        return result;
    }

    public <T> boolean putListCacheWithExpireTime(String key, List<T> objList, final long expireTime) {
        final byte[] bkey = key.getBytes();
        final byte[] bvalue = ProtoStuffSerializerUtil.serializeList(objList);
        boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            connection.setEx(bkey, expireTime, bvalue);
            return true;
        });
        return result;
    }

    public <T> T getCache(final String key, Class<T> targetClass) {
        byte[] result = redisTemplate.execute((RedisCallback<byte[]>) connection -> connection.get(key.getBytes()));
        if (result == null) {
            return null;
        }
        return ProtoStuffSerializerUtil.deserialize(result, targetClass);
    }

    public <T> List<T> getListCache(final String key, Class<T> targetClass) {
        byte[] result = redisTemplate.execute((RedisCallback<byte[]>) connection -> connection.get(key.getBytes()));
        if (result == null) {
            return null;
        }
        return ProtoStuffSerializerUtil.deserializeList(result, targetClass);
    }

    /**
     * 删除redis中的key
     * @param key
     */
    public void deleteCache(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 模糊删除key
     * @param pattern
     */
    public void deleteCacheWithPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

    /**
     * 清空所有缓存
     */
    public void clearCache() {
        deleteCacheWithPattern(RedisCache.CAHCENAME+"|*");
    }
}
