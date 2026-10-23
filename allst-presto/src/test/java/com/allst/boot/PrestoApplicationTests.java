package com.allst.boot;

import com.allst.boot.config.PrestoDataSourceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 不连接 Presto，只确认数据源和 JdbcTemplate 能装配出来。
 */
@SpringBootTest
class PrestoApplicationTests {

    @Autowired
    @Qualifier(PrestoDataSourceConfig.JDBC_TEMPLATE)
    private JdbcTemplate prestoJdbcTemplate;

    @Test
    void contextLoads() {
        assertNotNull(prestoJdbcTemplate);
    }
}
