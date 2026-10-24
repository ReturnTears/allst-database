package com.allst.trino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Trino 查询服务入口。数据源由 {@code trino.jdbc} 单独注册，不使用 Spring Boot 的默认数据源。
 *
 * @author Hutu
 * @since 2023-11-06 下午 08:34
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class BootTrinoApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootTrinoApplication.class, args);
    }
}
