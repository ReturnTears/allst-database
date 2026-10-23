# 20. Hibernate 加载过程与配置项

`LocalContainerEntityManagerFactoryBean` 启动时会：

1. 读取 `spring.jpa.*` 和 `spring.jpa.properties.hibernate.*`。
2. 扫描 `@EntityScan` 指定的实体。
3. 用命名策略算出没有 `@Column` 的列名。
4. 按 `ddl-auto` 建表或更新表。练习项目是 `update`，生产应改为 `validate`，并用迁移脚本管理表。

已经显式固定的配置：

| 配置 | 作用 |
| --- | --- |
| `SpringPhysicalNamingStrategy` | `createdAt` 变成 `created_at` |
| `SpringImplicitNamingStrategy` | 关联列默认命名 |
| `jdbc.batch_size=20` | 批量插入、更新 |
| `order_inserts` / `order_updates` | 让批量按表排好，批处理才真正合并 |
| `batch_versioned_data` | 带版本号的批量更新也可以批处理 |
| `in_clause_parameter_padding` | IN 参数个数补齐，减少查询计划分裂 |
| `query.plan_cache_max_size` | 查询计划缓存上限 |
| `generate_statistics` | 主配置关闭，测试打开，用来观察 N+1 和计划缓存 |

个别列不走策略。员工姓名是 `@Column(name = "emp_name")`，薪资列是 `salary_amount`。物理名和属性名不一致时，原生 SQL 必须用物理名。

方言不写死成 MySQL。连 MySQL 时 Hibernate 会选 MySQL 方言，测试换成 H2 时用 `H2Dialect`，避免一套方言打两边。
