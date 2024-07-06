package com.allst.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * @author Hutu
 * @since 2024-07-06 下午 02:52
 */
@Configuration
public class LuaConfig {
    @Bean
    public DefaultRedisScript<Long> limitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/limit.lua")));
        script.setResultType(Long.class);
        return script;
    }
}
