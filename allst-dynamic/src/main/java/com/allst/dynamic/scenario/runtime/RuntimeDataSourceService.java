package com.allst.dynamic.scenario.runtime;

import com.allst.dynamic.scenario.common.Checks;
import com.allst.dynamic.scenario.common.DataSourceKeys;
import com.allst.dynamic.scenario.common.ScenarioException;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.hikari.HikariCpConfig;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.TreeSet;

/**
 * 运行时增减数据源。新租户、新分片上线时用，不需要重启。
 * 名字不能带下划线，否则会被 dynamic-datasource 当成一个负载均衡组。
 */
@Service
public class RuntimeDataSourceService {

    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

    private final DataSource dataSource;
    private final DefaultDataSourceCreator dataSourceCreator;

    public RuntimeDataSourceService(DataSource dataSource, DefaultDataSourceCreator dataSourceCreator) {
        this.dataSource = dataSource;
        this.dataSourceCreator = dataSourceCreator;
    }

    public DataSourceSnapshot snapshot() {
        DynamicRoutingDataSource routing = routing();
        DataSourceSnapshot snapshot = new DataSourceSnapshot();
        snapshot.setDataSources(new TreeSet<>(routing.getDataSources().keySet()));
        snapshot.setGroups(new TreeSet<>(routing.getGroupDataSources().keySet()));
        return snapshot;
    }

    public void register(String key, String url, String username, String password, String driverClassName) {
        String normalized = Checks.requireText(key, "dataSource", 64);
        if (normalized.indexOf('_') >= 0) {
            throw new ScenarioException(400, "运行时数据源名称不能包含下划线");
        }
        if (DataSourceKeys.protectedKey(normalized)) {
            throw new ScenarioException(400, "内置数据源不能覆盖");
        }
        String jdbcUrl = Checks.requireText(url, "url", 500);
        if (!jdbcUrl.startsWith("jdbc:")) {
            throw new ScenarioException(400, "url 必须以 jdbc: 开头");
        }
        DynamicRoutingDataSource routing = routing();
        if (routing.getDataSources().containsKey(normalized)) {
            throw new ScenarioException(400, "数据源 " + normalized + " 已存在，先摘除再注册");
        }
        DataSourceProperty property = new DataSourceProperty();
        property.setPoolName(normalized);
        property.setUrl(jdbcUrl);
        property.setUsername(Checks.requireText(username, "username", 64));
        property.setPassword(password == null ? "" : password);
        property.setDriverClassName(driverClassName == null || driverClassName.isBlank() ? MYSQL_DRIVER : driverClassName.trim());
        property.setLazy(Boolean.TRUE);
        HikariCpConfig hikari = new HikariCpConfig();
        hikari.setInitializationFailTimeout(-1L);
        hikari.setMinIdle(0);
        hikari.setMaxPoolSize(5);
        hikari.setConnectionTimeout(3000L);
        property.setHikari(hikari);
        routing.addDataSource(normalized, dataSourceCreator.createDataSource(property));
    }

    public void remove(String key) {
        if (DataSourceKeys.protectedKey(key)) {
            throw new ScenarioException(400, "内置数据源不能摘除");
        }
        DynamicRoutingDataSource routing = routing();
        if (!routing.getDataSources().containsKey(key)) {
            throw new ScenarioException(404, "数据源 " + key + " 不存在");
        }
        routing.removeDataSource(key);
    }

    public boolean exists(String key) {
        return routing().getDataSources().containsKey(key);
    }

    private DynamicRoutingDataSource routing() {
        if (dataSource instanceof DynamicRoutingDataSource routing) {
            return routing;
        }
        throw new IllegalStateException("当前 DataSource 不是 DynamicRoutingDataSource");
    }
}
