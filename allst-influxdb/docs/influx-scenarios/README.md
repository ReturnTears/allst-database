# InfluxDB 业务规范

这个模块约定 Spring Boot 2 里怎么使用 InfluxDB。示例业务是设备指标：温度、湿度、电压按时间写入，再按设备和指标类型查询、聚合、降采样。

运行环境：Java 11、Spring Boot 2.7.18、[influxdb-java 2.24](https://github.com/influxdata/influxdb-java/tree/influxdb-java-2.24)、InfluxDB 1.8。选择 1.8 是因为连续查询、保留策略和 `influxd backup` 都是这一代的用法。2.x 把 database 换成 bucket，把连续查询换成 Task，把 InfluxQL 换成 Flux，客户端也不是这一个包。

## 场景

| 场景 | 入口 | 文档 |
| --- | --- | --- |
| 基本写入 | `/api/influx/metrics` | [01-基本写入](01-基本写入.md) |
| 基本查询 | `/api/influx/metrics` | [02-基本查询](02-基本查询.md) |
| 函数与运算 | `/api/influx/analytics` | [03-函数与运算](03-函数与运算.md) |
| 连续查询 | `/api/influx/continuous-queries` | [04-连续查询](04-连续查询.md) |
| 性能调优 | `/api/influx/tuning/profile` | [05-性能调优](05-性能调优.md) |
| 集群安装配置 | `deploy/cluster` | [06-集群安装配置](06-集群安装配置.md) |
| 数据备份与恢复 | `/api/influx/backup` | [07-数据备份与恢复](07-数据备份与恢复.md) |
| 保留策略与降采样 | `/api/influx/schema/init` | [08-保留策略与降采样](08-保留策略与降采样.md) |
| 数据模型 | `/api/influx/schema/fields` | [09-数据模型规范](09-数据模型规范.md) |
| 删除与基数治理 | `/api/influx/governance/cardinality` | [10-删除与基数治理](10-删除与基数治理.md) |

`GET http://127.0.0.1:8091/api/influx/catalog` 返回同一份目录。

## 先把库准备好

1. `docker compose -f deploy/docker-compose.yml up -d`
2. 启动本模块，端口 `8091`
3. `POST /api/influx/schema/init` 创建保留策略和连续查询
4. `POST /api/influx/metrics/seed` 写入最近 60 分钟的样例

应用默认可以在 InfluxDB 没启动时起来。`influx.initialize-on-startup=true` 才会在启动时建库。本地账号是 `admin` / `admin123`，用 `INFLUX_USERNAME` 和 `INFLUX_PASSWORD` 覆盖。

PowerShell 里用 `curl.exe`。`curl` 是 `Invoke-WebRequest` 的别名。

## 代码怎么分层

- `com.allst.boot.influx`：客户端、配置、InfluxQL 拼接和结果读取
- `com.allst.boot.metric`：原始点的写入、查询、删除
- `com.allst.boot.analytics`：聚合、变换和固定运算
- `com.allst.boot.cq`：连续查询和历史回填
- `com.allst.boot.tuning`：客户端参数和写入压测
- `com.allst.boot.ops`：逻辑导出和 `influxd backup`
- `com.allst.boot.governance`：series 基数和 tag 取值

外部传入的函数名、窗口、tag 和时间都先经过白名单，再拼进 InfluxQL。不要把前端传来的字符串直接接到 `SELECT` 后面。

语句使用 POST。`influxdb-java` 的默认查询是 GET，语句一长就会顶到 URL 长度。

## 参考

- [InfluxDB 1.8 文档](https://docs.influxdata.com/influxdb/v1.8/)
- [InfluxQL 连续查询](https://docs.influxdata.com/influxdb/v1.8/query_language/continuous_queries/)
- [备份与恢复](https://docs.influxdata.com/influxdb/v1.8/administration/backup_and_restore/)
- [InfluxDB Relay](https://github.com/influxdata/influxdb-relay)
- [Enterprise 1.8 安装](https://docs.influxdata.com/enterprise_influxdb/v1.8/introduction/installation/)
