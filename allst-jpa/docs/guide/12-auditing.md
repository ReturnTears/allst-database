# 12. Auditing

`JpaAuditingConfig` 打开 `@EnableJpaAuditing(auditorAwareRef = "auditorAware")`。

`BaseAuditableEntity` 上的四个字段：

- `@CreatedDate` / `@LastModifiedDate`：`LocalDateTime`，创建时间列 `updatable = false`
- `@CreatedBy` / `@LastModifiedBy`：操作人

实体还要挂 `AuditingEntityListener`。监听器写在 `@MappedSuperclass` 上，子类会继承。

操作人来自 `AuditorAwareImpl`，优先级是：

1. 测试或异步代码写入的 `AuditorAwareImpl.CURRENT`
2. 当前请求头 `X-Auditor`
3. 默认值 `system`

这不是安全上下文。没有 Spring Security 时用请求头只适合练习和内部网关已经鉴权过的服务。对外接口应改成从认证信息里取用户。

Auditing 在 `persist` / `update` 时写字段，批量 `@Query` 更新不会触发它。
