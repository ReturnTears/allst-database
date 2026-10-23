# 17. DataSource 的加载过程

自动配置的顺序可以收成五步：

1. `DataSourceProperties` 读取 `spring.datasource.url`、用户名、密码、驱动。
2. `DataSourceAutoConfiguration` 在容器里还没有 `DataSource` 时创建 Hikari 连接池。
3. `HibernateJpaAutoConfiguration` 用这个数据源创建 `LocalContainerEntityManagerFactoryBean`。
4. 实体来自 `@EntityScan`。正式启动类只扫 `com.allst.boot.entity` 和 `com.allst.boot.guide.entity`，第二库和分表实体不会进业务持久化单元。
5. `JpaTransactionManager` 从事务里向池借连接，提交或回滚后归还。

`PersistenceUnitFactory` 把第 2 步和第 3 步写成了可以在测试里直接调用的方法，方便多数据源和分表各自建一套。`/guide/infra/datasource` 返回池名称、最大连接数、JDBC URL 和是否打开了 open-in-view，不返回密码。

一旦自己声明了任何一个 `DataSource` Bean，第 2 步就会退出。所以 `multi` 和 `sharding` 两个 profile 都要自己提供 `@Primary` 的业务数据源。
