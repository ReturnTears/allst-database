# Spring Boot 集成 Trino

本文记录 `allst-trino` 如何把 Trino 接到 Spring Boot，方便以后对照代码回溯。当前实现是一个可直接改配置、再继续加业务查询的 demo。

## 1. 结果

应用不负责存储数据。它通过 JDBC 把 SQL 发给 Trino 协调节点，由 Trino 去查询底层数据源。Demo 固定了下面几件事：

- 驱动：`io.trino:trino-jdbc:435`
- 框架：Spring Boot `2.7.18`，Java `11`
- 连接池：`spring-boot-starter-jdbc` 自带的 HikariCP
- 访问方式：名为 `trinoJdbcTemplate` 的 `JdbcTemplate`
- 应用端口：`8122`。Trino 协调节点继续使用 `8080`，两边不冲突
- 默认只读，并限制返回行数和超时

Trino 435 的协调节点运行在镜像自带的 JDK 里。JDBC 客户端按官方要求使用 Java 11，所以这个 Spring Boot 模块保持 Java 11，没有升到 Spring Boot 3。

驱动版本和服务端镜像标签都是 `435`，与 `src/lib/trino-cli-435-executable.jar` 是同一代。JDBC 驱动应与集群相同或更新。升级时同时改 `pom.xml` 的 `trino.version` 和 `docker-compose.yml` 的镜像标签。

## 2. 参考

集成方式对齐官方 JDBC 说明，以及一个直接可用的 Spring Boot 案例：

- 官方 JDBC：[Trino JDBC driver](https://trino.io/docs/current/client/jdbc.html)，源码文档在 [trinodb/trino 的 jdbc.rst](https://github.com/trinodb/trino/blob/master/docs/src/main/sphinx/client/jdbc.rst)
- 官方容器：[Trino Docker 容器](https://trino.io/docs/current/installation/containers.html)，镜像 `trinodb/trino`
- [Angel-Asensio/simple-trino-spring-boot](https://github.com/Angel-Asensio/simple-trino-spring-boot)：`spring-boot-starter-jdbc` + `trino-jdbc`，用 `JdbcTemplate` 执行 `SELECT 1` 验证连接。它的配置是：

```properties
spring.datasource.driver-class-name=io.trino.jdbc.TrinoDriver
spring.datasource.url=jdbc:trino://<TRINO_IP>:8080?user=<TRINO_USER>
```

本模块没有照搬 `spring.datasource`。那个案例只有 Trino 一个库，自动配置足够。这里和同仓库的 `allst-presto` 一样，把连接放在 `trino.jdbc` 下单独建数据源，以后再接 MySQL 等主库时不用拆掉自动配置。社区里把 Trino 当作查询库、把 PostgreSQL 当作写入库时，也是这种多数据源写法；绑定 Hikari 时属性名必须是 `jdbc-url`，只写 `url` 会报 `jdbcUrl is required`。当前代码直接调用 `setJdbcUrl`，避开这个问题。

另外两点来自实际使用，而不是那个示例仓库：

- Trino 语句默认自动提交。不要用 Spring 的 `@Transactional` 期待回滚。写入或 DDL 需要时，把 `trino.query.read-only` 关掉，并确认账号权限。
- 使用 JDBC 的账号必须能查询 `system.jdbc`，否则驱动取元数据会失败。这是官方驱动的要求。

## 3. 目录

```text
allst-trino
├── docker-compose.yml                 官方镜像 trinodb/trino:435，端口 8080
├── docker/trino/catalog
│   └── hive.properties.example        以后接 Hive 时复制使用，默认不挂载
├── docs/TRINO_INTEGRATION.md
├── src/lib
│   ├── trino-cli-435-executable.jar   命令行客户端，应用不加载它
│   └── trino-jdbc-432.jar             早期本地驱动，应用不加载它
└── src/main/java/com/allst/trino
    ├── BootTrinoApplication.java
    ├── config
    │   ├── TrinoProperties.java
    │   └── TrinoDataSourceConfig.java
    ├── controller/TrinoController.java
    ├── service/TrinoQueryService.java
    ├── support/TrinoSqlGuard.java
    ├── exception
    └── model
```

示例 SQL 另外放在 `src/main/resources/sql/tpch-samples.sql`。

应用依赖以 Maven 上的 `io.trino:trino-jdbc:435` 为准。`src/lib` 里的 jar 只留给手工 CLI，不要再加 `system` 作用域依赖。

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
| `io.trino:trino-jdbc` | Trino 官方 JDBC 驱动 |
| `spring-boot-maven-plugin` | `mvn spring-boot:run` 和可执行 jar |

驱动类名是 `io.trino.jdbc.TrinoDriver`。URL 前缀是 `jdbc:trino`。同仓库的 `allst-presto` 使用 `com.facebook.presto.jdbc.PrestoDriver` 和 `jdbc:presto`，两者不能混用。

## 5. 不要走 Spring 默认数据源

`spring-boot-starter-jdbc` 在 classpath 上时，Spring Boot 会按 `spring.datasource.url` 自动创建数据源。本模块的配置前缀是 `trino.jdbc`，没有那个 URL。如果不处理，启动会直接失败。

处理方式有两处，作用相同，保留两处是为了以后读配置或读启动类都能看到原因：

- `BootTrinoApplication` 上排除 `DataSourceAutoConfiguration`
- `application.yml` 中：

```yaml
spring:
  autoconfigure:
    exclude: org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

以后如果这个服务还要接 MySQL 之类的主库，删掉这两处排除，给主库加 `@Primary` 数据源，Trino 继续使用 `trinoDataSource` 这个 Bean。

## 6. 连接配置

配置在 `src/main/resources/application.yml`。

```yaml
trino:
  jdbc:
    driver-class-name: io.trino.jdbc.TrinoDriver
    url: jdbc:trino://127.0.0.1:8080/tpch/tiny
    username: trino
    password: ""
    ssl: false
    ssl-verification: ""
    source: allst-trino
    timezone: ""
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

JDBC URL 模板，与官方文档一致：

```text
jdbc:trino://host:port
jdbc:trino://host:port/catalog
jdbc:trino://host:port/catalog/schema
```

URL 里的 catalog 和 schema 只是默认值。跨数据源查询时，SQL 仍要写成 `catalog.schema.table`，例如 `tpch.tiny.nation`。GitHub 案例把用户名写进 URL 的 `?user=`，这里改成独立的 `username`，密码和 TLS 参数就不散落在 URL 里。

`minimum-idle: 0` 且连接池 `initializationFailTimeout` 设为 `-1`。这样本机没有 Trino 时，Spring Boot 也能启动；第一次查询才建连。连接池在 `TrinoDataSourceConfig` 里手工创建，下面这些参数会交给 Trino 驱动：

| 配置 | 驱动参数 | 作用 |
| --- | --- | --- |
| `username` / `password` | `user` / `password` | 查询身份。密码为空时不调用 `setPassword` |
| `ssl` | `SSL` | `true` 时走 HTTPS |
| `ssl-verification` | `SSLVerification` | `FULL`、`CA`、`NONE`。自签证书联调常用 `NONE` |
| `ssl-trust-store-path` | `SSLTrustStorePath` | 信任库 |
| `ssl-trust-store-password` | `SSLTrustStorePassword` | 信任库密码 |
| `ssl-key-store-path` | `SSLKeyStorePath` | 客户端证书 |
| `ssl-key-store-password` | `SSLKeyStorePassword` | 客户端证书密码 |
| `source` | `source` | Trino Web UI 的 Source 列，默认 `allst-trino` |
| `timezone` | `timezone` | 会话时区，例如 `Asia/Shanghai` |
| `access-token` | `accessToken` | 需要令牌认证时填写，接口不会回显 |
| `extra-credentials` | `extraCredentials` | 形式为 `key1:value1,key2:value2`，接口不会回显 |

另外固定设置了 `applicationNamePrefix=allst-trino`。

开启 TLS 时改这几项：

```yaml
trino:
  jdbc:
    url: jdbc:trino://trino.example.com:8443/hive/sales
    username: test
    password: secret
    ssl: true
    ssl-verification: NONE
```

`ssl-verification: NONE` 只适合联调。正式环境使用 `FULL` 或 `CA`，并配置信任库。

## 7. 数据源和 JdbcTemplate

`TrinoDataSourceConfig` 注册两个 Bean：

| Bean 名 | 类型 | 用途 |
| --- | --- | --- |
| `trinoDataSource` | Hikari `DataSource` | 需要自己管理连接时使用 |
| `trinoJdbcTemplate` | `JdbcTemplate` | 业务查询的默认入口 |

后续业务代码沿用 GitHub 案例里的注入方式：

```java
@Service
public class NationReportService {

    private final JdbcTemplate trinoJdbcTemplate;

    public NationReportService(@Qualifier("trinoJdbcTemplate") JdbcTemplate trinoJdbcTemplate) {
        this.trinoJdbcTemplate = trinoJdbcTemplate;
    }

    public List<Map<String, Object>> nations() {
        return trinoJdbcTemplate.queryForList(
                "SELECT nationkey, name FROM tpch.tiny.nation ORDER BY nationkey");
    }
}
```

常量 `TrinoDataSourceConfig.JDBC_TEMPLATE` 就是 `"trinoJdbcTemplate"`，新代码优先用这个常量，避免名字写散。

`JdbcTemplate` 上还设置了全局查询超时和最大行数，数值来自 `trino.query`。单次 HTTP 请求可以再传更小的 `maxRows` 和 `timeoutSeconds`，但不能超过配置上限。

## 8. 查询封装

`TrinoQueryService` 是 demo 的查询入口，做了四件事：

1. 用 `TrinoSqlGuard` 去掉注释、去掉末尾分号，并拒绝一次提交多条 SQL。
2. `trino.query.read-only=true` 时，只允许语句以 `SELECT`、`WITH`、`SHOW`、`DESCRIBE`、`DESC`、`EXPLAIN` 开头。
3. 把 `Timestamp`、`Date`、`Time` 转成字符串，把 `byte[]` 转成 Base64，避免 Jackson 输出不稳定。
4. 多取一行用来判断结果是否被 `maxRows` 截断，响应里的 `truncated` 会标出来。

只读检查是 demo 保护，不能代替 Trino 的系统访问控制。生产环境应给应用单独的只读账号。

catalog、schema 参数只允许字母、数字和下划线。它们会拼进 SQL 标识符，不能用 `PreparedStatement` 占位。表名过滤值使用 `?` 绑定。

需要在官方镜像自带的 `memory` catalog 里建表时，把只读关掉：

```yaml
trino:
  query:
    read-only: false
```

然后可以执行：

```sql
CREATE TABLE memory.default.demo (id integer, name varchar);
INSERT INTO memory.default.demo VALUES (1, 'trino');
SELECT * FROM memory.default.demo;
```

`memory` 的数据只存在于当前 Trino 进程，容器重建后消失。它适合验证写入路径，不适合当业务库。

## 9. HTTP 接口

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| GET | `/trino/halo` | 存活检查，不访问 Trino |
| GET | `/trino/info` | 当前连接信息，不返回密码和令牌 |
| GET | `/trino/samples` | 可直接执行的示例 SQL |
| GET | `/trino/health` | 执行 `SELECT 1` |
| GET | `/trino/nodes` | 读取 `system.runtime.nodes` |
| GET | `/trino/catalogs` | `SHOW CATALOGS` |
| GET | `/trino/schemas?catalog=tpch` | 读取 `information_schema.schemata` |
| GET | `/trino/tables?catalog=tpch&schema=tiny` | 读取 `information_schema.tables` |
| POST | `/trino/query` | 执行一条 SQL |

自定义查询：

```bash
curl -X POST http://127.0.0.1:8122/trino/query ^
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
| 502 | Trino 连不上、SQL 被引擎拒绝、超时 |

## 10. 本地启动 Trino

官方单机方式：

```bash
docker run -d -p 8080:8080 --name trino-trial trinodb/trino
```

本模块把同一镜像收进了 `docker-compose.yml`，并固定为 `trinodb/trino:435`，与 JDBC 版本一致。镜像已经包含 `tpch`，不需要再挂 catalog。`tpch` 是内存基准数据，不需要 Hive 或对象存储。

在 `allst-trino` 目录执行：

```bash
docker compose up -d
mvn spring-boot:run
```

确认：

```bash
curl http://127.0.0.1:8122/trino/halo
curl http://127.0.0.1:8122/trino/health
curl http://127.0.0.1:8122/trino/catalogs
curl "http://127.0.0.1:8122/trino/tables?catalog=tpch&schema=tiny"
```

Trino Web UI：`http://127.0.0.1:8080`。默认开发镜像不校验密码，用户名填写 `trino` 即可。容器日志出现协调节点就绪后，JDBC 才能连上。

也可以继续用原来的 CLI，不经过本应用：

```bash
java -jar src/lib/trino-cli-435-executable.jar --server 127.0.0.1:8080 --user trino
```

进入 CLI 后：

```sql
select count(*) from tpch.sf1.nation;
```

已经有集群时，不需要启动 compose，只改 `trino.jdbc.url` 和用户名。例如 Hive：

```yaml
trino:
  jdbc:
    url: jdbc:trino://192.168.1.10:8080/hive/sales
    username: test
```

Hive catalog 的示例文件在 `docker/trino/catalog/hive.properties.example`。复制成 `hive.properties` 后，按文件里的注释挂到容器的 `/etc/trino/catalog/hive.properties`。

停止本地容器：

```bash
docker compose down
```

`allst-presto` 的 compose 也占用宿主机 `8080`。两个协调节点不要同时映射到同一个端口。一起开时，把其中一个改成 `8081:8080`，并同步修改对应的 JDBC URL。

## 11. 验证记录

不依赖 Trino 进程的检查：

```bash
mvn test
```

`BootTrinoApplicationTests` 只确认 Spring 容器和 `trinoJdbcTemplate` 能创建。`TrinoSqlGuardTest` 覆盖只读语句、多语句、注释和标识符。

连上 Trino 之后，用第 9 节的 `curl` 走一遍 `/health`、`/catalogs`、`/nodes` 和 `/query`。`/halo`、`/info` 与 `/samples` 在 Trino 未启动时也应该返回。

## 12. 常见问题

**启动报缺少 `spring.datasource.url`。**  
`DataSourceAutoConfiguration` 没有排除。检查启动类和 `application.yml` 两处。

**Hikari 报 `jdbcUrl is required`。**  
用 `DataSourceBuilder` 绑定 Hikari 时，属性名要写 `jdbc-url`，不能只写 `url`。当前代码直接调用 `setJdbcUrl`，正常配置不会遇到这个问题。

**应用能启动，但 `/trino/health` 返回 502。**  
Trino 还没就绪，或者地址、端口、用户名不对。先看容器日志，再核对 `trino.jdbc.url`。Web UI 能打开，只说明 HTTP 端口通了，catalog 仍可能未加载。

**`Catalog tpch does not exist`。**  
当前集群没有 `tpch`。官方 `trinodb/trino` 镜像默认有。自建集群改成真实存在的 catalog，或者在 `/etc/trino/catalog/tpch.properties` 写入 `connector.name=tpch` 后重启协调节点。

**TLS 握手失败。**  
确认 URL 端口是 HTTPS 端口，`ssl: true`。自签证书联调可临时设 `ssl-verification: NONE`。有信任库时改回 `FULL` 或 `CA`，并填写 `ssl-trust-store-path`。

**`@Transactional` 没有回滚。**  
Trino JDBC 按语句自动提交，Spring 事务管不到。查询走 Trino，需要事务的写入留在 MySQL、PostgreSQL 等库。

**结果被截断。**  
响应里 `truncated` 为 `true`。调大请求中的 `maxRows`，或者调大 `trino.query.max-rows`。请求值不能超过配置上限。

**中文或时间显示异常。**  
时间类型在服务里转成了字符串。需要固定时区时设置 `trino.jdbc.timezone`，例如 `Asia/Shanghai`。

**端口占用。**  
`8080` 属于 Trino，`8122` 属于本应用。`8121` 已被 `allst-presto` 使用。冲突时改 compose 的宿主机端口映射，并同步修改 `trino.jdbc.url`。

## 13. 后续开发时改哪里

| 需求 | 位置 |
| --- | --- |
| 换集群、账号、TLS、时区 | `application.yml` 的 `trino.jdbc` |
| 调整超时、行数、只读开关 | `application.yml` 的 `trino.query` |
| 新增一个固定报表 | 新建 Service，注入 `trinoJdbcTemplate` |
| 新增一个对外接口 | `TrinoController` 或新的 Controller |
| 允许写入或 DDL | `trino.query.read-only=false`，同时收紧 Trino 账号权限 |
| 增加第二个 Trino 集群 | 再声明一套 Properties、DataSource 和 JdbcTemplate，Bean 名不要复用 |
| 接 Hive 或其他 catalog | `docker/trino/catalog/*.properties`，并修改 JDBC URL |
| 升级驱动 | `pom.xml` 的 `trino.version`，并与服务端镜像一起验证 |

业务 SQL 优先使用完全限定名。分页、聚合和过滤放在 Trino 里完成，不要把大结果集拉到应用内存里再处理。`max-rows` 是 demo 的保护线，正式报表应在 SQL 里写自己的 `LIMIT`。
