package com.allst.es.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Hutu
 * @since 2025-01-02 下午 10:07
 */
@Data
@Component
public class MySqlConfig {
    private String hostname = "127.0.0.1";
    @Value("${spring.datasource.port:3306}")
    private Integer post;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password:123456}")
    private String password;
}
