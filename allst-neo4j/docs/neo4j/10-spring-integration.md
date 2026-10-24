# Spring 集成

## 两套访问方式并存

| | 旧练习 | 本规范 |
| --- | --- | --- |
| 包 | `com.allst.db.bean`、`repository`、`service` | `com.allst.db.guide` |
| 访问方式 | Spring Data Neo4j 5 的 `Neo4jRepository` 和 OGM | Neo4j 4.3 HTTP 事务 API |
| 连接 | `spring.data.neo4j.uri=bolt://127.0.0.1:7687` | `allst.neo4j.http-uri=http://127.0.0.1:7474` |
| 图模型 | `Person` | `Member`、`Department`、`Project` 等 |

OGM 3.2 依赖 `org.neo4j.driver:neo4j-java-driver:1.7`，包名是 `org.neo4j.driver.v1`。Driver 4 使用同一个 Maven 坐标和包名 `org.neo4j.driver`，Maven 只能留一个版本。引入 Driver 4 会让旧仓库在运行时找不到 `v1` 包。所以新代码用 HTTP，旧代码保持原样。

应用启动时仍会尝试原来的 Person 保存。Bolt 驱动 1.7 连不上 Neo4j 4 时，`AllstNeo4jApplication` 捕获异常并继续启动，避免规范接口被旧练习挡住。`@EntityScan("com.allst.db.bean")` 让 OGM 只扫描旧的 `Person`。OGM 3.2 扫到规范代码里的 `List<Map<String, Object>>` 会在建 `SessionFactory` 时出现空指针。

## 新代码怎么组织

- `GuideNeo4jConfig` 提供单独的 `RestTemplate`，超时和 UTF-8 都只作用于 Neo4j。
- `Neo4jHttpClient` 负责提交、开事务、回滚，并把 `errors` 转成 `Neo4jException`。Cypher 错误经常是 HTTP 200 加错误数组，不能只看状态码。
- 写入服务把 Cypher 和参数放在一起。控制器只做路径和请求体。
- `CypherCatalog` 是只读白名单。没有开放任意 Cypher 的接口。
- `GuideExceptionHandler` 只包 `com.allst.db.guide` 的控制器：参数错误 400，找不到 404，唯一约束冲突 409，连不上数据库 503。

数据库名只允许字母、数字和 `._-`，避免配置被拼进 URL 路径。

## 生产工程如果可以换 Boot

Spring Boot 2.4 及以上的 Spring Data Neo4j 6 直接使用 Java Driver 4，那时应把 HTTP 客户端换成会话，而不是继续保留两套驱动。Driver 4.4 的形状是：

```java
try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
     Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
    return session.readTransaction(tx -> tx.run(
            "MATCH (m:Member) WHERE m.email = $email RETURN m.name AS name",
            Values.parameters("email", email)).list());
}
```

写入用 `session.writeTransaction`。Driver 5 把这两个方法改名为 `executeRead` 和 `executeWrite`。语句、参数和事务边界与现在的目录一致，不需要改 Cypher。

Java Driver 自带连接池。当前 `RestTemplate` 使用 JDK 的 `HttpURLConnection`，没有连接池，只适合这套单机示例。Neo4j 5.26 也开始用 Query API 替代事务 HTTP API，升级到那一版时直接换 Driver，不再扩展这个客户端。

## 和业务相关的边界

- 一个成员的创建用 `CREATE` 加唯一约束；导入和样例用 `MERGE`。
- 读目录和写服务分开，`PROFILE` 不会误伤数据。
- 清理样例只匹配规范标签。旧的 `Person` 节点留在库里。
- 邮箱路径关闭了 Spring Boot 2.2 默认的后缀匹配，避免 `chen@allst.dev` 的 `.dev` 被截掉。
- 中文 JSON 使用 UTF-8。全文检索的特殊字符在进入 Lucene 之前转义。
