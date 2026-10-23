# 19. 事务和连接池

事务不占用一条“专用连接”到业务结束之后。`JpaTransactionManager` 在事务真正需要 SQL 时向 Hikari 借连接，提交、回滚或事务挂起时归还。

连接池参数在 `application.properties`：

- `maximum-pool-size=10`
- `minimum-idle=2`
- `connection-timeout=30000`
- `idle-timeout`、`max-lifetime`
- `spring.transaction.default-timeout=30`

池子大小按数据库允许的连接数来，不按线程数无限放大。事务时间应短于借连接的超时，否则线程会在拿连接时失败，而不是在 SQL 上失败。

`hibernate.connection.provider_disables_autocommit` 没有强制打开。Hikari 默认自动提交，事务管理器会在事务开始时关掉它，结束时恢复。不要再自己从 `DataSource.getConnection()` 取连接去做 JPA 操作，那样会绕开当前事务。

传播行为对连接的影响见 [34](34-transaction-propagation.md)。`REQUIRES_NEW` 会挂起外层事务并再借一条连接，所以外层未提交的数据，内层默认看不见。
