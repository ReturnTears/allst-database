package com.allst.boot.guide.sharding;

import com.allst.boot.guide.infra.PersistenceUnitFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * profile=sharding 时给分表实体单独一个持久化单元。
 * 同时补上 @Primary 业务数据源，因为容器里一旦出现任何 DataSource，Boot 就不再自动创建。
 */
@Configuration
@Profile("sharding")
@EnableJpaRepositories(
        basePackages = "com.allst.boot.guide.sharding",
        entityManagerFactoryRef = "shardingEntityManagerFactory",
        transactionManagerRef = "shardingTransactionManager"
)
public class ShardingJpaConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(com.zaxxer.hikari.HikariDataSource.class).build();
    }

    @Bean
    public DataSource shardingDataSource() throws SQLException {
        return ShardingSphereExampleFactory.createShardingDataSource(ShardingSphereExampleFactory.createRawDataSource());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean shardingEntityManagerFactory(
            @Qualifier("shardingDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = PersistenceUnitFactory.create(
                dataSource, "sharding", "com.allst.boot.guide.sharding");
        factory.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "none");
        return factory;
    }

    @Bean
    public PlatformTransactionManager shardingTransactionManager(
            @Qualifier("shardingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
