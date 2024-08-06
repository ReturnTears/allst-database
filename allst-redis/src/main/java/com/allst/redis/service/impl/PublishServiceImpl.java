package com.allst.redis.service.impl;

import com.allst.redis.service.IPublishService;
import com.allst.redis.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Hutu
 * @since 2024-08-06 下午 09:39
 */
@Service
public class PublishServiceImpl implements IPublishService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean publishMessageByChannel(String channel, Object message) {
        return redisUtil.convertAndSend(channel, message);
    }

    @Override
    public boolean publishMessageByDefaultChannel(Object message) {
        return this.publishMessageByChannel("default_channel_topic", message);
    }
}
