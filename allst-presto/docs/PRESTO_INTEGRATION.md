# Spring Boot 集成 Presto

本文记录 `allst-presto` 如何把 PrestoDB 接到 Spring Boot，方便以后对照代码回溯。当前实现是一个可直接改配置、再继续加业务查询的 demo。

## 1. 结果

应用不负责存储数据。它通过 JDBC 把 SQL 发给 Presto 协调节点，由 Presto 去查询底层数据源。Demo 固定了下面几件事：

- 驱动：`com.facebook.presto:presto-jdbc:0.299`
- 框架：Spring Boot `2.7.18`，Java `11`
- 连接池：`spring-boot-starter-jdbc` 自带的 HikariCP
- 访问方式：名为 `prestoJdbcTemplate` 的 `JdbcTemplate`
- 应用端口：`8121`。Presto 协调节点继续使用 `8080`，两边不冲突
- 默认只读，并限制返回行数和超时

Presto 从 `0.295` 起，协调节点和 worker 需要 Java 17。JDBC 客户端仍然兼容 Java 8，所以这个 Spring Boot 模块保持 Java 11。

## 2. 参考

集成方式对齐了官方 JDBC 说明和两个 Spring Boot starter 案例：

- 官方 Java 客户端：[prestodb/presto 的 java.rst](https://github.com/prestodb/presto/blob/master/presto-docs/src/main/sphinx/clients/java.rst)，在线文档见 [Presto JDBC](https://prestodb.io/docs/current/clients/java.html)
- [1900/presto-spring-boot-starter](https://github.com/1900/presto-spring-boot-starter)：单独配置 URL、账号，并暴露 `prestoJdbcTemplate`
- [ifengkou/spring-boot-starter-data-presto](https://github.com/ifengkou/spring-boot-starter-data-presto)：把 Presto 配成独立数据源，业务代码注入 `JdbcTemplate` 后执行 SQL

本模块没有引入这两个 starter。它们都不是 Spring 官方组件，版本也偏旧。这里按同样的结构手写配置，依赖只保留官方 JDBC 驱动和 Spring Boot，后续升级路径更直接。

社区案例里还有一个反复出现的问题：Druid 连接池对 Presto 会抛出 `dbType not support`。因此这里使用 HikariCP，不引入 Druid。

## 3. 目录

```text
allst-presto
├── docker-compose.yml
├── docker/presto
│   ├── config.properties          单节点协调器，同时充当 worker
│   ├── jvm.config
│   └── catalog/tpch.properties    内存 TPC-H 数据，用来验证 SQL
├── docs/PRESTO_INTEGRATION.md
└── src/main/java/com/allst/boot
    ├── PrestoApplication.java
    ├── config
    │   ├── PrestoProperties.java
    │   └── PrestoDataSourceConfig.java
    ├── controller/PrestoController.java
    ├── service/PrestoQueryService.java
    ├── support/PrestoSqlGuard.java
    ├── exception
    └── model
```

示例 SQL 另外放在 `src/main/resources/sql/tpch-samples.sql`。

## 4. 依赖

父工程 `allst-db` 不是 `spring-boot-starter-parent`，所以在本模块的 `pom.xml` 里导入 Spring Boot BOM，避免每个 starter 单独写版本：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

实际增加的依赖：

| 依赖 | 作用 |
| --- | --- |
| `spring-boot-starter-web` | HTTP 接口 |
| `spring-boot-starter-jdbc` | `JdbcTemplate` 和 HikariCP |
| `com.facebook.presto:presto-jdbc` | PrestoDB 官方 JDBC 驱动 |
| `spring-boot-maven-plugin` | `mvn spring-boot:run` 和可执行 jar |

驱动类名是 `com.facebook.presto.jdbc.PrestoDriver`。这是 PrestoDB。同仓库的 `allst-trino` 使用 `io.trino:trino-jdbc`，URL 前缀是 `jdbc:trino`，两者不能混用。

客户端版本尽量和 Presto 服务端靠近。升级时只改 `pom.xml` 里的 `presto.version`。

## 5. 不要走 Spring 默认数据源

`spring-boot-starter-jdbc` 在 classpath 上时，Spring Boot 会按 `spring.datasource.url` 自动创建数据源。本模块的配置前缀是 `presto.jdbc`，没有那个 URL。如果不处理，启动会直接失败。

处理方式有两处，作用相同，保留两处是为了以后读配置或读启动类都能看到原因：

- `PrestoApplication` 上排除 `DataSourceAutoConfiguration`
- `application.yml` 中：

```yaml
spring:
  autoconfigure:
    exclude: org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

以后如果这个服务还要接 MySQL 之类的主库，删掉这两处排除，给主库加 `@Primary` 数据源，Presto 继续使用 `prestoDataSource` 这个 Bean。

## 6. 连接配置

配置在 `src/main/resources/application.yml`。

```yaml
presto:
  jdbc:
    driver-class-name: com.facebook.presto.jdbc.PrestoDriver
    url: jdbc:presto://127.0.0.1:8080/tpch/tiny
    username: presto
    password: ""
    ssl: false
  pool:
    maximum-pool-size: 8
    minimum-idle: 0
    connection-timeout-ms: 30000
    idle-timeout-ms: 600000
    max-lifetime-ms: 1800000
  query:
    timeout-seconds: 60
    max-rows: 500
    read-only: true
```

JDBC URL 模板：

```text
jdbc:presto://host:port
jdbc:presto://host:port/catalog
jdbc:presto://host:port/catalog/schema
```

URL 里的 catalog 和 schema 只是默认值。跨数据源查询时，SQL 仍要写成 `catalog.schema.table`，例如 `tpch.tiny.nation`。

`minimum-idle: 0` 且连接池 `initializationFailTimeout` 设为 `-1`。这样本机没有 Presto 时，Spring Boot 也能启动；第一次查询才建连。连接池在 `PrestoDataSourceConfig` 里手工创建，而不是用 `DataSourceBuilder`，因为下面几个参数需要直接落到 Hikari：

- `jdbcUrl` 必须显式设置。只写 Spring 习惯的 `url`，Hikari 会报 `jdbcUrl is required`
- 启动失败策略
- `SSL`、`SSLTrustStorePath`、`SSLKeyStorePath` 这些 Presto 驱动参数
- `applicationNamePrefix=allst-presto`，查询会出现在 Presto Web UI 的来源里

密码为空时不调用 `setPassword`。很多开发集群没有开启密码认证，用户名仍然会作为 Presto 的查询用户。

开启 TLS 时改这几项：

```yaml
presto:
  jdbc:
    url: jdbc:presto://presto.example.com:8443/hive/default
    ssl: true
    ssl-trust-store-path: D:/certs/presto-truststore.jks
    ssl-trust-store-password: changeit
```

对应驱动参数来自官方 JDBC 文档：`SSL`、`SSLTrustStorePath`、`SSLTrustStorePassword`、`SSLKeyStorePath`、`SSLKeyStorePassword`。

## 7. 数据源和 JdbcTemplate

`PrestoDataSourceConfig` 注册两个 Bean：

| Bean 名 | 类型 | 用途 |
| --- | --- | --- |
| `prestoDataSource` | Hikari `DataSource` | 需要自己管理连接时使用 |
| `prestoJdbcTemplate` | `JdbcTemplate` | 业务查询的默认入口 |

后续业务代码沿用 GitHub 案例里的注入方式：

```java
@Service
public class OrderReportService {

    private final JdbcTemplate prestoJdbcTemplate;

    public OrderReportService(@Qualifier("prestoJdbcTemplate") JdbcTemplate prestoJdbcTemplate) {
        this.prestoJdbcTemplate = prestoJdbcTemplate;
    }

    public List<Map<String, Object>> latestOrders() {
        return prestoJdbcTemplate.queryForList(
                "SELECT orderkey, totalprice FROM tpch.tiny.orders LIMIT 20");
    }
}
```

常量 `PrestoDataSourceConfig.JDBC_TEMPLATE` 就是 `"prestoJdbcTemplate"`，新代码优先用这个常量，避免名字写散。

`JdbcTemplate` 上还设置了全局查询超时和最大行数，数值来自 `presto.query`。单次 HTTP 请求可以再传更小的 `maxRows` 和 `timeoutSeconds`，但不能超过配置上限。

## 8. 查询封装

`PrestoQueryService` 是 demo 的查询入口，做了四件事：

1. 用 `PrestoSqlGuard` 去掉注释、去掉末尾分号，并拒绝一次提交多条 SQL。
2. `presto.query.read-only=true` 时，只允许语句以 `SELECT`、`WITH`、`SHOW`、`DESCRIBE`、`DESC`、`EXPLAIN` 开头。
3. 把 `Timestamp`、`Date`、`Time` 转成字符串，把 `byte[]` 转成 Base64，避免 Jackson 输出不稳定。
4. 多取一行用来判断结果是否被 `maxRows` 截断，响应里的 `truncated` 会标出来。

只读检查是 demo 保护，不能代替 Presto 的系统访问控制和文件权限。生产环境应给应用单独的只读账号。需要写入或 DDL 时，把 `presto.query.read-only` 改为 `false`，并确认账号权限。

catalog、schema 参数只允许字母、数字和下划线。它们会拼进 SQL 标识符，不能用 `PreparedStatement` 占位。表名过滤值使用 `?` 绑定。

## 9. HTTP 接口

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| GET | `/presto/halo` | 存活检查，不访问 Presto |
| GET | `/presto/info` | 当前连接信息，不返回密码 |
| GET | `/presto/samples` | 可直接执行的示例 SQL |
| GET | `/presto/health` | 执行 `SELECT 1` |
| GET | `/presto/catalogs` | `SHOW CATALOGS` |
| GET | `/presto/schemas?catalog=tpch` | 读取 `information_schema.schemata` |
| GET | `/presto/tables?catalog=tpch&schema=tiny` | 读取 `information_schema.tables` |
| POST | `/presto/query` | 执行一条 SQL |

自定义查询：

```bash
curl -X POST http://127.0.0.1:8121/presto/query ^
  -H "Content-Type: application/json" ^
  -d "{\"sql\":\"SELECT nationkey, name FROM tpch.tiny.nation ORDER BY nationkey\",\"maxRows\":25}"
```

成功时返回：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "sql": "SELECT nationkey, name FROM tpch.tiny.nation ORDER BY nationkey",
    "columns": ["nationkey", "name"],
    "rows": [],
    "rowCount": 0,
    "truncated": false,
    "elapsedMs": 0
  }
}
```

错误约定：

| 状态码 | 场景 |
| --- | --- |
| 400 | SQL 为空、多条 SQL、只读限制、参数不合法、JSON 无法解析 |
| 502 | Presto 连不上、SQL 被引擎拒绝、超时 |

使用 JDBC 的账号需要有 `system.jdbc` 的查询权限，否则元数据接口会失败。这是官方驱动的要求。

## 10. 本地启动 Presto

官方单机 Docker 方式见 [Deploy Presto with Docker](https://prestodb.io/docs/current/installation/deploy-docker.html)。本模块把同一套配置收进了 `docker-compose.yml`：协调器同时作为 worker，并挂载 `tpch` catalog。`tpch` 是 Presto 自带的内存数据集，不需要 Hive 或对象存储。

在 `allst-presto` 目录执行：

```bash
docker compose up -d
mvn spring-boot:run
```

确认：

```bash
curl http://127.0.0.1:8121/presto/halo
curl http://127.0.0.1:8121/presto/health
curl http://127.0.0.1:8121/presto/catalogs
curl "http://127.0.0.1:8121/presto/tables?catalog=tpch&schema=tiny"
```

Presto Web UI：`http://127.0.0.1:8080`。日志里出现 `SERVER STARTED` 后，JDBC 才能连上。

已经有集群时，不需要启动 compose，只改 `presto.jdbc.url` 和用户名。例如 Hive：

```yaml
presto:
  jdbc:
    url: jdbc:presto://192.168.1.10:8080/hive/default
    username: hive
```

停止本地容器：

```bash
docker compose down
```

## 11. 验证记录

不依赖 Presto 进程的检查：

```bash
mvn test
```

`PrestoApplicationTests` 只确认 Spring 容器和 `prestoJdbcTemplate` 能创建。`PrestoSqlGuardTest` 覆盖只读语句、多语句、注释和标识符。

连上 Presto 之后，用第 9 节的 `curl` 走一遍 `/health`、`/catalogs` 和 `/query`。`/halo` 与 `/samples` 在 Presto 未启动时也应该返回。

## 12. 常见问题

**启动报缺少 `spring.datasource.url`。**  
`DataSourceAutoConfiguration` 没有排除。检查启动类和 `application.yml` 两处。

**Hikari 报 `jdbcUrl is required`。**  
用 `DataSourceBuilder` 绑定 Hikari 时，属性名要写 `jdbc-url`，不能只写 `url`。当前代码直接调用 `setJdbcUrl`，正常配置不会遇到这个问题。

**Druid 报 `dbType not support`。**  
不要把 Presto 交给 Druid。继续使用这里的 Hikari 数据源。

**应用能启动，但 `/presto/health` 返回 502。**  
Presto 还没就绪，或者地址、端口不对。先看容器日志是否已有 `SERVER STARTED`，再核对 `presto.jdbc.url`。

**`Catalog tpch does not exist`。**  
协调节点没有加载 `docker/presto/catalog/tpch.properties`。确认 compose 挂载还在，然后重启容器。已有集群则改成该集群真实存在的 catalog。

**结果被截断。**  
响应里 `truncated` 为 `true`。调大请求中的 `maxRows`，或者调大 `presto.query.max-rows`。请求值不能超过配置上限。

**中文或时间显示异常。**  
时间类型在服务里转成了字符串。时区要和 Presto 会话一致时，可在 JDBC URL 增加官方参数，例如 `timeZoneId=Asia/Shanghai`。

**端口占用。**  
`8080` 属于 Presto，`8121` 属于本应用。冲突时改 compose 的宿主机端口映射，并同步修改 `presto.jdbc.url`。

## 13. 后续开发时改哪里

| 需求 | 位置 |
| --- | --- |
| 换集群、账号、TLS | `application.yml` 的 `presto.jdbc` |
| 调整超时、行数、只读开关 | `application.yml` 的 `presto.query` |
| 新增一个固定报表 | 新建 Service，注入 `prestoJdbcTemplate` |
| 新增一个对外接口 | `PrestoController` 或新的 Controller |
| 允许写入 | `presto.query.read-only=false`，同时收紧 Presto 账号权限 |
| 增加第二个 Presto 集群 | 再声明一套 Properties、DataSource 和 JdbcTemplate，Bean 名不要复用 |
| 升级驱动 | `pom.xml` 的 `presto.version`，并与服务端版本一起验证 |

业务 SQL 优先使用完全限定名。分页、聚合和过滤放在 Presto 里完成，不要把大结果集拉到应用内存里再处理。`max-rows` 是 demo 的保护线，正式报表应在 SQL 里写自己的 `LIMIT`。
