package org.example.provider;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JdbcTemplateCacheProvider {

    private final DataSource dataSource;

    /** 缓存 DynamicRoutingDataSource 实例，避免重复 instanceof 检查 */
    private volatile DynamicRoutingDataSource cachedRoutingDataSource;

    /** 缓存 JdbcTemplate，按 dsKey 区分 */
    private final ConcurrentHashMap<String, JdbcTemplate> jdbcTemplateCache = new ConcurrentHashMap<>();

    /** 缓存 TransactionTemplate，按 dsKey 区分 */
    private final ConcurrentHashMap<String, TransactionTemplate> transactionTemplateCache = new ConcurrentHashMap<>();

    public JdbcTemplateCacheProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Set<String> dataSourceKeys() {
        return routingDataSource().getDataSources().keySet();
    }

    /**
     * ✅ 核心改动：JdbcTemplate 按 dsKey 缓存复用
     * JdbcTemplate 是线程安全的，可以安全共享
     */
    public JdbcTemplate jdbcTemplate(String dsKey) {
        return jdbcTemplateCache.computeIfAbsent(dsKey, key ->
                new JdbcTemplate(dataSource(key))
        );
    }

    /**
     * ✅ 核心改动：TransactionTemplate 按 dsKey 缓存复用
     * TransactionTemplate 是线程安全的，可以安全共享
     */
    public TransactionTemplate transactionTemplate(String dsKey) {
        return transactionTemplateCache.computeIfAbsent(dsKey, key ->
                new TransactionTemplate(new DataSourceTransactionManager(dataSource(key)))
        );
    }

    public DataSource dataSource(String dsKey) {
        DataSource target = routingDataSource().getDataSource(dsKey);
        if (target == null) {
            throw new RuntimeException("未注册数据源: " + dsKey);
        }
        return target;
    }

    /**
     * ✅ 缓存 routingDataSource 转换结果
     * DCL 保证线程安全 + 只检查一次 instanceof
     */
    public DynamicRoutingDataSource routingDataSource() {
        if (cachedRoutingDataSource == null) {
            synchronized (this) {
                if (cachedRoutingDataSource == null) {
                    if (!(dataSource instanceof DynamicRoutingDataSource drds)) {
                        throw new RuntimeException("当前数据源不是dynamic-datasource");
                    }
                    cachedRoutingDataSource = drds;
                }
            }
        }
        return cachedRoutingDataSource;
    }
}
