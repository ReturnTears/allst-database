# 18. 多数据源

业务库继续用 `spring.datasource.*`。报表放在另一个库，实体是 `com.allst.boot.guide.secondary.ReportRecord`。

`MultiDataSourceConfig` 只在 `multi` profile 生效，它声明：

- `@Primary dataSource` 和 `entityManagerFactory`、`transactionManager`
- `secondaryDataSource`、`secondaryEntityManagerFactory`、`secondaryTransactionManager`
- `@EnableJpaRepositories` 只把 `guide.secondary` 绑到第二套单元

`application-multi.properties` 会关掉 Boot 自己的数据源和 JPA 自动配置，否则两套 `entityManagerFactory` 会撞名。这个 profile 文件会覆盖主配置里的 `spring.autoconfigure.exclude`，所以 Redis 和 Elasticsearch 的排除项也写进去了。

第二套单元的仓库方法要使用 `transactionManagerRef` 对应的事务。只在业务服务上写 `@Transactional` 不会管到报表库。

`MultiDataSourceTest` 用两个内存库验证：部门写在主库，报表写在从库，两边的表结构互不包含。练习配置里的从库是 H2。生产环境把 `PersistenceUnitFactory.h2` 换成第二套 MySQL 的 URL、账号和连接池上限即可，包扫描和事务管理器不用改。
