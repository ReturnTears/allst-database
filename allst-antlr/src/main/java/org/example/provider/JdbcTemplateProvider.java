package org.example.provider;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Set;

@Component
public class JdbcTemplateProvider {
    private final DataSource dataSource;
    public JdbcTemplateProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Set<String> dataSourceKeys() {
        return routingDataSource().getDataSources().keySet();
    }

    public JdbcTemplate jdbcTemplate(String dsKey) {
        return new JdbcTemplate(dataSource(dsKey));
    }

    public TransactionTemplate transactionTemplate(String dsKey) {
        return new TransactionTemplate(new DataSourceTransactionManager(dataSource(dsKey)));
    }

    public DataSource dataSource(String dsKey) {
        DataSource target = routingDataSource().getDataSource(dsKey);
        if (target == null) {
            throw  new RuntimeException("未注册数据源");
        }
        return target;
    }

    public DynamicRoutingDataSource routingDataSource() {
        if (!(dataSource instanceof DynamicRoutingDataSource routingDataSource)) {
            throw new RuntimeException("当前数据源不是dynamic-datasource");
        }
        return routingDataSource;
    }
}
