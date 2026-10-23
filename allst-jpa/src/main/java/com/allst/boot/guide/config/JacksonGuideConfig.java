package com.allst.boot.guide.config;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 未初始化的懒加载代理序列化成 null，而不是在 Jackson 里触发加载或抛 LazyInitializationException。
 */
@Configuration
public class JacksonGuideConfig {

    @Bean
    public Hibernate5Module hibernate5Module() {
        Hibernate5Module module = new Hibernate5Module();
        module.disable(Hibernate5Module.Feature.FORCE_LAZY_LOADING);
        return module;
    }
}
