package com.allst.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author Hutu
 * @since 2024-08-13 下午 09:43
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class PrestoApplication {
    public static void main(String... args) {
        SpringApplication.run(PrestoApplication.class, args);
    }
}
