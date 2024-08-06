package com.allst.redis;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AllstRedisApplication {
    public static void main(String[] args) {
        SpringApplication.run(AllstRedisApplication.class, args);
    }

    @Bean
    public Redisson redisson() {
        // 单机模式
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(0);
        return (Redisson) Redisson.create(config);
    }
}
