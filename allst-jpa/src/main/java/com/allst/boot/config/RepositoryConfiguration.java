package com.allst.boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author Hutu
 * @since 2024-08-05 下午 10:38
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.allst.boot.repository.jpa")
@EnableMongoRepositories(basePackages = "com.allst.boot.repository.mongo")
public class RepositoryConfiguration {
}
