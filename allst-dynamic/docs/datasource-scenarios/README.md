# 多数据源与动态数据源规范

这个模块约定 Spring Boot 里怎么使用多数据源和动态数据源。旧的 City 练习（`/city`、`master` / `slave`、`doc/edit.sql`）保留不动。新场景都在 `com.allst.dynamic.scenario`，库名以 `dyn_` 开头。

运行环境：Java 17、Spring Boot 2.7.18、MyBatis-Plus 3.5.1、dynamic-datasource 3.5.1、MySQL 8。默认端口 `8000`，上下文路径 `/dynamic`。

两类用法都落在 baomidou dynamic-datasource 上，因为项目已经引入它，而且它就是 Spring Boot 里切换数据源的常用做法：

- **多数据源**：数据源在 `application.yml` 里声明好，运行期只是按注解或固定算法选择。读写分离、分库分表属于这一类。
- **动态数据源**：数据源键由请求上下文决定，集合还可以在运行中增加。租户隔离、灰度发布、数据分片属于这一类。

dynamic-datasource 只负责切换库。分表由 MyBatis-Plus 的动态表名完成。仓库里的 `allst-sharding-proxy` 是另一条路（ShardingSphere Proxy），不要和这里的应用内路由混在一个数据源上。

## 场景一览

| 场景 | 类别 | 接口前缀 | 文档 |
| --- | --- | --- | --- |
| 读写分离 | 多数据源 | `/scenario/rw` | [01-读写分离](01-读写分离.md) |
| 分库分表 | 多数据源 | `/scenario/sharding` | [02-分库分表](02-分库分表.md) |
| 租户隔离 | 动态数据源 | `/scenario/tenant` | [03-租户隔离](03-租户隔离.md) |
| 灰度发布 | 动态数据源 | `/scenario/gray` | [04-灰度发布](04-灰度发布.md) |
| 数据分片 | 动态数据源 | `/scenario/shard` | [05-数据分片](05-数据分片.md) |
| 主从粘滞、多数据源事务 | 补充 | `/scenario/rw`、`/scenario/tenant/transfer` | [06-主从粘滞与多数据源事务](06-主从粘滞与多数据源事务.md) |

`GET http://127.0.0.1:8000/dynamic/scenario` 返回同一份目录。`GET /dynamic/scenario/datasources` 返回当前已注册的数据源和分组。

## 开始之前

1. MySQL 账号沿用原来的 `root`、空密码。先执行 [sql/mysql-init.sql](sql/mysql-init.sql)。
2. 场景数据源设置了 `lazy: true`，并且连接池 `initialization-fail-timeout` 为 `-1`。库还没建好时应用可以启动；第一次访问该场景会返回 HTTP 503，消息里带有脚本路径。
3. `master`、`slave` 仍是 City 练习用的库，不要改名，也不要加下划线。`rwSlave_1` 这种名字会被分成组 `rwSlave`。
4. PowerShell 里用 `curl.exe`。`curl` 是 `Invoke-WebRequest` 的别名。

```bat
curl.exe http://127.0.0.1:8000/dynamic/scenario
```

## 共同约定

1. 路由放在 Service 的公开方法上，通过 Spring 代理生效。同一个类里的 `this.xxx()` 不会触发 `@DS`、`@ReadRoute`、`@DSTransactional`。
2. 切换数据源的切面必须包在事务外面。连接在事务开始时取走，事务里面再切换不会改变这条连接。
3. 用 `DynamicDataSourceContextHolder.push` / `poll` 手动切换时必须成对出现，放在 `finally` 里 `poll`。请求结束由 `RequestRouteCleanupFilter` 再清一次，避免线程池串库。
4. 成功响应 `code` 为 0。参数错误返回 HTTP 400，找不到租户或数据返回 404，数据源不可用返回 503。异常处理只覆盖 `com.allst.dynamic.scenario`。
5. 响应里的 `dataSource` 是逻辑键，`database` 是这条连接上 `SELECT DATABASE()` 的结果，用来核对实际打到了哪个库。
6. 全局 `strict` 保持 `false`，避免旧的 City 接口因为找不到数据源而失败。场景代码自己检查键是否存在，不会悄悄落到 `master`。

## 代码怎么分层

`com.allst.dynamic.scenario.common` 放返回体、数据源键、`push/poll` 和请求清理。每个场景自己的实体、Mapper、Service、Controller 放在对应包里。运行时注册数据源在 `scenario.runtime`。

路由算法（分库分表、灰度判断、槽位）不依赖 Spring，单测在 `RouteRuleTest`。带 H2 的整条链路在 `ScenarioFlowTest`，不要求本机 MySQL。

## 参考来源

- [baomidou/dynamic-datasource 3.5.1](https://github.com/baomidou/dynamic-datasource/tree/v3.5.1)：`@DS`、`@DSTransactional`、从库分组、运行时 `addDataSource`
- [MyBatis-Plus 3.5.1 动态表名](https://github.com/baomidou/mybatis-plus/blob/v3.5.1/mybatis-plus-extension/src/main/java/com/baomidou/mybatisplus/extension/plugins/inner/DynamicTableNameInnerInterceptor.java)
