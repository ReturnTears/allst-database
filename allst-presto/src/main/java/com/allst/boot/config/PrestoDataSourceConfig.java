package com.allst.boot.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * 单独注册 Presto 数据源。连接池使用 Spring Boot JDBC 自带的 HikariCP。
 *
 * @author Hutu
 * @since 2026-10-23
 */
@Configuration
@EnableConfigurationProperties(PrestoProperties.class)
public class PrestoDataSourceConfig {

    public static final String DATA_SOURCE = "prestoDataSource";
    public static final String JDBC_TEMPLATE = "prestoJdbcTemplate";

    @Bean(name = DATA_SOURCE)
    public DataSource prestoDataSource(PrestoProperties properties) {
        PrestoProperties.Jdbc jdbc = properties.getJdbc();
        PrestoProperties.Pool pool = properties.getPool();

        HikariConfig config = new HikariConfig();
        config.setPoolName("presto-pool");
        config.setDriverClassName(jdbc.getDriverClassName());
        config.setJdbcUrl(jdbc.getUrl());
        config.setUsername(jdbc.getUsername());
        if (StringUtils.hasText(jdbc.getPassword())) {
            config.setPassword(jdbc.getPassword());
        }
        config.setMaximumPoolSize(pool.getMaximumPoolSize());
        config.setMinimumIdle(pool.getMinimumIdle());
        config.setConnectionTimeout(pool.getConnectionTimeoutMs());
        config.setIdleTimeout(pool.getIdleTimeoutMs());
        config.setMaxLifetime(pool.getMaxLifetimeMs());
        // 小于 0 时，Presto 未启动也不会阻止 Spring 容器起来。
        config.setInitializationFailTimeout(-1);
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("applicationNamePrefix", "allst-presto");
        applySsl(config, jdbc);
        return new HikariDataSource(config);
    }

    @Bean(name = JDBC_TEMPLATE)
    public JdbcTemplate prestoJdbcTemplate(@Qualifier(DATA_SOURCE) DataSource dataSource,
                                           PrestoProperties properties) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(properties.getQuery().getTimeoutSeconds());
        jdbcTemplate.setMaxRows(properties.getQuery().getMaxRows());
        return jdbcTemplate;
    }

    private void applySsl(HikariConfig config, PrestoProperties.Jdbc jdbc) {
        if (jdbc.isSsl()) {
            config.addDataSourceProperty("SSL", "true");
        }
        if (StringUtils.hasText(jdbc.getSslTrustStorePath())) {
            config.addDataSourceProperty("SSLTrustStorePath", jdbc.getSslTrustStorePath());
        }
        if (StringUtils.hasText(jdbc.getSslTrustStorePassword())) {
            config.addDataSourceProperty("SSLTrustStorePassword", jdbc.getSslTrustStorePassword());
        }
        if (StringUtils.hasText(jdbc.getSslKeyStorePath())) {
            config.addDataSourceProperty("SSLKeyStorePath", jdbc.getSslKeyStorePath());
        }
        if (StringUtils.hasText(jdbc.getSslKeyStorePassword())) {
            config.addDataSourceProperty("SSLKeyStorePassword", jdbc.getSslKeyStorePassword());
        }
    }
}
