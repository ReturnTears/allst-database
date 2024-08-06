package com.allst.redis.service.subscription;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Hutu
 * @since 2024-08-06 下午 09:49
 */
@Slf4j
@Component
public class SubscriptionServiceImpl implements CommandLineRunner {
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("come in method run()" + Arrays.toString(args));
        this.handleChannelTask();
    }

    public void handleChannelTask() {
        try {
            log.info("come in method handleRedisMessage()");
            MessageListener listener = (message, pattern) -> {
                byte[] channel = message.getChannel();
                String channelStr = new String(channel);
                if (StrUtil.equals(channelStr, "default_channel_topic")) {
                    String body = new String(message.getBody());
                    System.out.println("body:" + body);
                }
            };
            redisMessageListenerContainer.addMessageListener(listener, new ChannelTopic("default_channel_topic"));
        } catch (Exception e) {
            log.error("redisMessageListenerContainer addMessageListener error", e);
        }
    }
}
