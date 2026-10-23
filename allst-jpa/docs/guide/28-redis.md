# 28. Redis 与 JPA

JPA 仍是数据来源。Redis 只做 Cache-Aside，不替代事务。

`EmployeeCacheService.findSummary`：

1. 有 `StringRedisTemplate` 时先读 `guide:employee:{id}`。
2. 命中则直接返回 `EmployeeSummary`，不再访问仓库。
3. 未命中则走 JPA，再按 10 分钟 TTL 回填。
4. 值是 `id|name|email`，不缓存实体和懒加载代理。

默认排除了 Redis 自动配置，没有 Redis 时 `ObjectProvider.getIfAvailable()` 为空，查询直接走数据库。需要连接时：

```text
--spring.profiles.active=redis
```

`RedisGuideConfig` 按 `spring.redis.host` 和 `spring.redis.port` 创建 Lettuce。更新或删除员工后调用 `evict`，避免缓存盖住数据库。

`RedisCacheAsideTest` 用假的模板验证命中路径。它不要求本机启动 Redis。
