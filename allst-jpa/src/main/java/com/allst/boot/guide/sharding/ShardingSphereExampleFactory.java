package com.allst.boot.guide.sharding;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 单库两张物理表：g_shard_order_0 / g_shard_order_1，按 user_id 取模。
 * 这个 DataSource 不作为默认 Bean，避免挤掉业务库的自动配置。
 */
public final class ShardingSphereExampleFactory {

    private ShardingSphereExampleFactory() {
    }

    public static HikariDataSource createRawDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("shard0");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:mem:shard0;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUsername("sa");
        dataSource.setMaximumPoolSize(5);
        return dataSource;
    }

    public static void createPhysicalTables(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("drop table if exists g_shard_order_0");
            statement.execute("drop table if exists g_shard_order_1");
            statement.execute("create table g_shard_order_0 ("
                    + "id bigint primary key, user_id bigint not null, amount decimal(19,2) not null)");
            statement.execute("create table g_shard_order_1 ("
                    + "id bigint primary key, user_id bigint not null, amount decimal(19,2) not null)");
        }
    }

    public static DataSource createShardingDataSource(DataSource raw) throws SQLException {
        createPhysicalTables(raw);
        Map<String, DataSource> dataSources = new HashMap<>();
        dataSources.put("ds0", raw);

        org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration rule =
                new org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration();
        org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration table =
                new org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration(
                        "g_shard_order", "ds0.g_shard_order_${0..1}");
        table.setTableShardingStrategy(
                new org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration(
                        "user_id", "order-inline"));
        rule.getTables().add(table);
        Properties algorithmProps = new Properties();
        algorithmProps.setProperty("algorithm-expression", "g_shard_order_${user_id % 2}");
        rule.getShardingAlgorithms().put("order-inline",
                new org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration("INLINE", algorithmProps));
        return org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory.createDataSource(
                dataSources,
                Collections.singleton(rule),
                new Properties());
    }
}
