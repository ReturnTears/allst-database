package com.allst.trino.config;

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
 * 单独注册 Trino 数据源。连接池使用 Spring Boot JDBC 自带的 HikariCP。
 *
 * @author Hutu
 * @since 2026-10-23
 */
@Configuration
@EnableConfigurationProperties(TrinoProperties.class)
public class TrinoDataSourceConfig {

    public static final String DATA_SOURCE = "trinoDataSource";
    public static final String JDBC_TEMPLATE = "trinoJdbcTemplate";

    @Bean(name = DATA_SOURCE)
    public DataSource trinoDataSource(TrinoProperties properties) {
        TrinoProperties.Jdbc jdbc = properties.getJdbc();
        TrinoProperties.Pool pool = properties.getPool();

        HikariConfig config = new HikariConfig();
        config.setPoolName("trino-pool");
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
        // 小于 0 时，Trino 未启动也不会阻止 Spring 容器起来。
        config.setInitializationFailTimeout(-1);
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("applicationNamePrefix", "allst-trino");
        applyDriverProperties(config, jdbc);
        return new HikariDataSource(config);
    }

    @Bean(name = JDBC_TEMPLATE)
    public JdbcTemplate trinoJdbcTemplate(@Qualifier(DATA_SOURCE) DataSource dataSource,
                                          TrinoProperties properties) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(properties.getQuery().getTimeoutSeconds());
        jdbcTemplate.setMaxRows(properties.getQuery().getMaxRows());
        return jdbcTemplate;
    }

    private void applyDriverProperties(HikariConfig config, TrinoProperties.Jdbc jdbc) {
        if (jdbc.isSsl()) {
            config.addDataSourceProperty("SSL", "true");
        }
        putIfPresent(config, "SSLVerification", jdbc.getSslVerification());
        putIfPresent(config, "SSLTrustStorePath", jdbc.getSslTrustStorePath());
        putIfPresent(config, "SSLTrustStorePassword", jdbc.getSslTrustStorePassword());
        putIfPresent(config, "SSLKeyStorePath", jdbc.getSslKeyStorePath());
        putIfPresent(config, "SSLKeyStorePassword", jdbc.getSslKeyStorePassword());
        putIfPresent(config, "source", jdbc.getSource());
        putIfPresent(config, "timezone", jdbc.getTimezone());
        putIfPresent(config, "accessToken", jdbc.getAccessToken());
        putIfPresent(config, "extraCredentials", jdbc.getExtraCredentials());
    }

    private void putIfPresent(HikariConfig config, String key, String value) {
        if (StringUtils.hasText(value)) {
            config.addDataSourceProperty(key, value);
        }
    }
}
