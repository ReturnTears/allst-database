package com.allst.boot.guide.config;

import com.allst.boot.guide.web.EmployeeSearchArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Pageable 和 Sort 由 Spring Data Web 自动注册。这里只追加项目自己的解析器。
 */
@Configuration
public class WebMvcGuideConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new EmployeeSearchArgumentResolver());
    }
}
