package com.allst.redis.service;

/**
 * @author Hutu
 * @since 2024-08-06 下午 09:38
 */
public interface IPublishService {
    /**
     * 向指定频道发布消息
     */
    boolean publishMessageByChannel(String channel, Object message);

    /**
     * 向默认频道发布消息
     */
    boolean publishMessageByDefaultChannel(Object message);
}
