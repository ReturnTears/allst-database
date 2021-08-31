package com.allst.mysql.id;

import org.apache.shardingsphere.core.strategy.keygen.SnowflakeShardingKeyGenerator;
import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator;

import java.util.Properties;

/**
 * 自定义主键生成器
 *
 * @author June
 * @since 2021年09月
 */
public class MyLOGOId implements ShardingKeyGenerator {

    // 借用雪花算法
    private final SnowflakeShardingKeyGenerator snow = new SnowflakeShardingKeyGenerator();

    @Override
    public Comparable<?> generateKey() {
        System.out.println("------执行了自定义主键生成器MyLagouId-------");
        return snow.generateKey();
    }

    @Override
    public String getType() {
        return "LOGOKEY";
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
