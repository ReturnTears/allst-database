package com.allst.trino;

import com.allst.trino.config.TrinoDataSourceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 不连接 Trino，只确认数据源和 JdbcTemplate 能装配出来。
 */
@SpringBootTest
class BootTrinoApplicationTests {

    @Autowired
    @Qualifier(TrinoDataSourceConfig.JDBC_TEMPLATE)
    private JdbcTemplate trinoJdbcTemplate;

    @Test
    void contextLoads() {
        assertNotNull(trinoJdbcTemplate);
    }
}
