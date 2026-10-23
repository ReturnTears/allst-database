package com.allst.boot.config;

import com.allst.boot.guide.repository.support.GuideBaseRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * 旧的 JPA / Mongo repository 扫描保持原包不变。
 * guide 包是后续规范场景，使用自定义 Repository 基类。
 *
 * @author Hutu
 * @since 2024-08-05 下午 10:38
 */
@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.allst.boot.repository.jpa",
                "com.allst.boot.guide.repository"
        },
        repositoryBaseClass = GuideBaseRepositoryImpl.class
)
@EnableMongoRepositories(basePackages = "com.allst.boot.repository.mongo")
public class RepositoryConfiguration {
}
