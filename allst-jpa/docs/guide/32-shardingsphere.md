# 32. ShardingSphere 与 JPA 分表

逻辑表是 `g_shard_order`，物理表是同一库里的 `g_shard_order_0` 和 `g_shard_order_1`。分片键是 `user_id`，算法是 `user_id % 2`。

`ShardingSphereExampleFactory` 负责建物理表并创建 ShardingSphere 数据源。JPA 只认识逻辑表：`ShardingOrder` 的 `@Table(name = "g_shard_order")`。EntityManager 透过 ShardingSphere 的 JDBC 驱动发出 SQL，驱动再改写到物理表。

主键使用应用分配的 id，不用 `IDENTITY`。跨物理表回填自增主键时，Hibernate 和分片驱动会不一致。

`ShardingJpaConfig` 在 `sharding` profile 下把 `guide.sharding` 这个包单独做成持久化单元，`ddl-auto` 为 `none`，因为物理表已经由工厂建好。业务库的 `@Primary DataSource` 也要在这个 profile 里自己声明，否则容器里一出现分表数据源，Boot 就不再创建业务数据源。

测试 `ShardingSphereJpaTest` 写入 `userId` 1 和 2，再直接统计两张物理表，各有一行，同时 JPQL 查询逻辑表能得到两行。

SnakeYAML 固定到 1.33。ShardingSphere 5.2 会调用 `LoaderOptions.setCodePointLimit`，Boot 2.7 自带的旧版本没有这个方法。
