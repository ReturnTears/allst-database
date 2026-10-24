# Cassandra 业务规范

这个模块约定 Spring Boot 3 里怎么使用 Cassandra 4.1。示例业务是设备遥测：温度、湿度、电压按设备和日期写入，再按设备、区域和指标查询。库存那一张表用来演示轻量事务，不和遥测混在一张表里。

运行环境：Java 17、Spring Boot 3.1.5、Java Driver 4.15、Cassandra 4.1。驱动版本跟着 Spring Boot 3.1.5 的依赖走。会话在第一次访问时才创建，所以 Cassandra 没启动时应用也能起来。

## 场景

| 场景 | 入口 | 文档 |
| --- | --- | --- |
| 基本写入 | `/api/cassandra/readings` | [01-基本写入](01-基本写入.md) |
| 基本查询 | `/api/cassandra/readings` | [02-基本查询](02-基本查询.md) |
| 函数与运算 | `/api/cassandra/analytics` | [03-函数与运算](03-函数与运算.md) |
| 连续查询 | `/api/cassandra/continuous` | [04-连续查询](04-连续查询.md) |
| 性能调优 | `/api/cassandra/tuning/profile` | [05-性能调优](05-性能调优.md) |
| 集群安装配置 | `deploy/cluster` | [06-集群安装配置](06-集群安装配置.md) |
| 数据备份与恢复 | `/api/cassandra/backup` | [07-数据备份与恢复](07-数据备份与恢复.md) |
| 数据模型 | `/api/cassandra/schema/model` | [08-数据模型规范](08-数据模型规范.md) |
| 轻量事务、TTL、计数器与集合 | `/api/cassandra/mutations` | [09-轻量事务TTL计数器与集合](09-轻量事务TTL计数器与集合.md) |
| 二级索引与物化视图 | `/api/cassandra/indexes` | [10-二级索引与物化视图](10-二级索引与物化视图.md) |
| 删除与墓碑 | `/api/cassandra/governance/advice` | [11-删除与墓碑](11-删除与墓碑.md) |

`GET http://127.0.0.1:8100/api/cassandra/catalog` 返回同一份目录。

## 先把库准备好

1. `docker compose -f deploy/docker-compose.yml up -d`
2. 等 `cqlsh` 能连上，第一次大约一两分钟
3. 启动本模块，端口 `8100`
4. `POST /api/cassandra/schema/init` 创建 keyspace、表、索引和物化视图
5. `POST /api/cassandra/readings/seed` 写入最近 60 分钟的样例

`cassandra.initialize-on-startup=true` 才会在启动时建表。单节点的副本策略是 `SimpleStrategy`、RF `1`。三节点的改法在 [06-集群安装配置](06-集群安装配置.md)。

PowerShell 里用 `curl.exe`。

## 代码怎么分层

- `com.allst.boot.cassandra`：驱动会话、配置、CQL 和结果读取
- `com.allst.boot.reading`：原始遥测的写入和查询
- `com.allst.boot.analytics`：聚合、标量函数和固定运算
- `com.allst.boot.continuous`：分页续查、全表扫描和小时降采样
- `com.allst.boot.tuning`：驱动参数、运行时信息和一个小批量写入
- `com.allst.boot.ops`：逻辑导出和 `nodetool snapshot` 命令
- `com.allst.boot.mutation`：轻量事务、计数器、集合
- `com.allst.boot.indexview`：二级索引和物化视图
- `com.allst.boot.governance`：删除和墓碑

值都用预编译语句绑定。函数名、排序、keyspace、快照名先过白名单。接口不接受一段 CQL。

没有使用 Spring Data Repository。Repository 会在启动时就要一个已经连上的 `CqlSession`，keyspace 不存在时应用起不来。业务项目如果集群一直在，可以再包一层 Repository；CQL 和分区键的约束仍然以这里为准。

## 参考

- [Cassandra 4.1 文档](https://cassandra.apache.org/doc/4.1/)
- [CQL 函数](https://cassandra.apache.org/doc/4.1/cassandra/cql/functions.html)
- [算术运算](https://cassandra.apache.org/doc/4.1/cassandra/cql/operators.html)
- [二级索引](https://cassandra.apache.org/doc/4.1/cassandra/cql/indexes.html)
- [Java Driver 4.15](https://docs.datastax.com/en/developer/java-driver/4.15/)
- [cassandra-java-driver](https://github.com/apache/cassandra-java-driver)
- [Medusa](https://github.com/thelastpickle/cassandra-medusa)
