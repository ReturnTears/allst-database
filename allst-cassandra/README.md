# Cassandra

Spring Boot 3 使用 Cassandra 4.1 的规范示例。业务是设备遥测：按设备和日期写入，再按设备、区域、指标查询，做聚合、分页、小时降采样、轻量事务、计数器和备份。

说明文档在 [docs/cassandra-scenarios](docs/cassandra-scenarios/README.md)。

## 环境

- Java 17
- Spring Boot 3.1.5
- DataStax Java Driver 4.15（由 `spring-boot-starter-data-cassandra` 带入）
- Cassandra 4.1

服务端跑在 Docker 里，自带运行时。应用是 Java 17，不要求本机安装 Cassandra 的 JDK。

Cassandra 没有 InfluxDB 那种 `CREATE CONTINUOUS QUERY`。连续查询在这里分成三件能落地的事：分区内的 pagingState 续查、全表连续扫描、应用侧小时降采样。4.1 的二级索引用普通 `CREATE INDEX`。`StorageAttachedIndex` 是 5.0 的能力，这个模块不创建它。

## 启动

```bat
docker compose -f deploy/docker-compose.yml up -d
mvn spring-boot:run
```

接口在 `http://127.0.0.1:8100`。默认不需要账号。集群开启密码后，用 `CASSANDRA_USERNAME` 和 `CASSANDRA_PASSWORD` 覆盖。

PowerShell 里用 `curl.exe`。`curl` 是 `Invoke-WebRequest` 的别名。

```bat
curl.exe http://127.0.0.1:8100/api/cassandra/catalog
curl.exe http://127.0.0.1:8100/api/cassandra/tuning/profile
curl.exe -X POST http://127.0.0.1:8100/api/cassandra/schema/init
curl.exe -X POST http://127.0.0.1:8100/api/cassandra/readings/seed
curl.exe "http://127.0.0.1:8100/api/cassandra/readings/latest?deviceId=dev-001&day=2026-10-24&metricType=temperature"
```

`day` 换成上海时区的当天。`/health` 和业务接口在 Cassandra 没启动时返回 503。`/catalog`、`/tuning/profile`、`/schema/model`、`/governance/advice`、`/backup/plan` 不连数据库。

## 测试

```bat
mvn test
```

测试不连接 Cassandra。覆盖日桶、主键、CQL 白名单、分页令牌、降采样分桶、备份文件名，以及目录里的文档是否都在。
