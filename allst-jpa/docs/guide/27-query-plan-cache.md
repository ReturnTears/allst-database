# 27. QueryPlanCache

Hibernate 会缓存 JPQL / SQL 解析后的计划。缓存的是计划，不是查询结果。相同 SQL、不同参数应命中同一份计划。

相关配置：

```properties
spring.jpa.properties.hibernate.query.plan_cache_max_size=2048
spring.jpa.properties.hibernate.query.plan_parameter_metadata_max_size=128
spring.jpa.properties.hibernate.query.in_clause_parameter_padding=true
```

`IN` 的参数个数一变，SQL 文本就变，计划就多一份。参数补齐把个数抬到 2 的幂，计划数量稳定下来。`EmployeeRepository.findByIdIn` 用来走这条路径。

`QueryPlanCacheService` 从 `SessionFactory.getStatistics()` 读取命中和未命中。统计默认关闭，测试配置打开了 `generate_statistics`。生产不要长期打开，计数本身有开销。

观察方式：连续两次执行 `findByStatus(status, sort)`，第二次应增加 `queryPlanCacheHitCount`。如果 SQL 被字符串拼出了不同文本，命中不会增加，这时应改回参数绑定。
