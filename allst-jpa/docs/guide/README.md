# Spring Data JPA 指南

旧练习代码仍在 `com.allst.boot.entity`、`com.allst.boot.repository`。本目录对应 `com.allst.boot.guide` 里的规范用法。表名前缀是 `g_`，避免和原来的 `person`、`users` 混在一起。

本地验证不需要 MySQL、Mongo、Redis 或 Elasticsearch：

```text
mvn test -Dtest=com.allst.boot.guide.**
```

启动正式应用时，`spring.jpa.hibernate.ddl-auto=update` 会在练习库里补齐指南表。也可以手工执行 `sql/guide.sql`。

| 编号 | 场景 | 文档 |
| --- | --- | --- |
| 1 | Repository 与自定义 Repository | [01](01-repository.md) |
| 2 | 方法名语法与参数 | [02](02-query-method.md) |
| 3 | 用返回值解决实际问题 | [03](03-return-types.md) |
| 4 | `@Query` | [04](04-query-annotation.md) |
| 5 | `@Entity` 与多态 | [05](05-entity-polymorphism.md) |
| 6 | 关联关系 | [06](06-associations.md) |
| 7 | Jackson | [07](07-jackson.md) |
| 8 | Query by Example | [08](08-query-by-example.md) |
| 9 | JpaSpecificationExecutor | [09](09-specification.md) |
| 10 | Querydsl | [10](10-querydsl.md) |
| 11 | 自定义 Repository 基类与片段 | [11](11-custom-repository.md) |
| 12 | Auditing | [12](12-auditing.md) |
| 13 | 实体回调与监听器 | [13](13-entity-callback.md) |
| 14 | 乐观锁与重试 | [14](14-optimistic-lock.md) |
| 15 | 对 WebMVC 的支持 | [15](15-webmvc.md) |
| 16 | 自定义参数解析器 | [16](16-argument-resolver.md) |
| 17 | DataSource 加载过程 | [17](17-datasource.md) |
| 18 | 多数据源 | [18](18-multi-datasource.md) |
| 19 | 事务与连接池 | [19](19-transaction-pool.md) |
| 20 | Hibernate 加载与配置 | [20](20-hibernate-bootstrap.md) |
| 21 | Persistence Context | [21](21-persistence-context.md) |
| 22 | open-in-view | [22](22-open-in-view.md) |
| 23 | 异步线程里使用 JPA | [23](23-async-jpa.md) |
| 24 | LazyInitializationException | [24](24-lazy-exception.md) |
| 25 | N+1 | [25](25-n-plus-one.md) |
| 26 | SpEL | [26](26-spel.md) |
| 27 | QueryPlanCache | [27](27-query-plan-cache.md) |
| 28 | Redis 与 JPA | [28](28-redis.md) |
| 29 | Spring Data REST | [29](29-data-rest.md) |
| 30 | 单元测试与集成测试 | [30](30-testing.md) |
| 31 | EsRepository 与 JpaRepository | [31](31-es-and-jpa.md) |
| 32 | ShardingSphere 分表 | [32](32-shardingsphere.md) |
| 33 | 分页、排序与投影 | [33](33-page-sort-projection.md) |
| 34 | 事务传播 | [34](34-transaction-propagation.md) |

可以同时打开的开关：

| Profile | 作用 |
| --- | --- |
| `redis` | 创建 Lettuce 连接和 `StringRedisTemplate` |
| `elasticsearch` | 创建 ES 客户端，并和 JPA 同步员工文档 |
| `multi` | 业务库 + H2 第二库，见 `application-multi.properties` |
| `sharding` | 业务库保持原样，另外挂一套分表持久化单元 |
