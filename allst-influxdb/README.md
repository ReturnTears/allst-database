# InfluxDB

Spring Boot 2 使用 InfluxDB 1.8 的规范示例。业务场景是设备指标的写入、查询、聚合、连续查询、降采样、性能参数、集群配置和备份恢复。

说明文档在 [docs/influx-scenarios](docs/influx-scenarios/README.md)。

## 环境

- Java 11
- Spring Boot 2.7.18
- influxdb-java 2.24
- InfluxDB 1.8.10

2.x 的 bucket、Task 和 Flux 不是这一套客户端。连续查询和 `influxd backup` 以 1.8 为准。

## 启动

```bat
docker compose -f deploy/docker-compose.yml up -d
mvn spring-boot:run
```

接口在 `http://127.0.0.1:8091`。本地库账号是 `admin` / `admin123`，用环境变量 `INFLUX_URL`、`INFLUX_USERNAME`、`INFLUX_PASSWORD` 覆盖。

PowerShell 里用 `curl.exe`。

```bat
curl.exe http://127.0.0.1:8091/api/influx/catalog
curl.exe http://127.0.0.1:8091/api/influx/health
curl.exe -X POST http://127.0.0.1:8091/api/influx/schema/init
curl.exe -X POST http://127.0.0.1:8091/api/influx/metrics/seed
curl.exe "http://127.0.0.1:8091/api/influx/metrics/latest?deviceId=dev-001"
curl.exe "http://127.0.0.1:8091/api/influx/analytics/aggregate?fn=mean&metricType=temperature&deviceId=dev-001&range=2h&window=1m"
```

`/health` 和业务接口在 InfluxDB 没启动时会返回 503。`/api/influx/tuning/profile` 仍然返回客户端配置。

## 测试

```bat
mvn test
```

测试不连接 InfluxDB。覆盖时间解析、InfluxQL 白名单、line protocol、备份路径和 `schema.iql` 是否与代码一致。
