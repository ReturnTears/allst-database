package com.allst.boot.guide.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 默认排除了 Redis 自动配置。加上 --spring.profiles.active=redis 才会连接。
 */
@Configuration
@Profile("redis")
public class RedisGuideConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host:127.0.0.1}") String host,
            @Value("${spring.redis.port:6379}") int port) {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
