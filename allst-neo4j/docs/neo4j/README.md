# Neo4j 业务开发规范

这套示例用一张协作图谱覆盖日常最常写的 Cypher：语法、写入语句、函数、索引、全文索引、约束、查询调优和执行计划。旧的 `Person` 练习还在 `com.allst.db.bean`、`repository`、`service`，这里不改它的模型。

规范实现在 `com.allst.db.guide`。它通过 Neo4j 4.3 的 HTTP 事务接口执行 Cypher。当前工程的 Spring Data Neo4j 还带着 Bolt 驱动 1.7，不能再放进 Java Driver 4，否则旧练习无法编译。取舍写在 [Spring 集成](10-spring-integration.md)。

## 图里有什么

| 标签 | 业务键 | 关系 |
| --- | --- | --- |
| Member | email | BELONGS_TO、MANAGES、KNOWS、WORKS_ON、HAS_SKILL、ASSIGNED、AUTHORED |
| Department | code | CHILD_OF 指向父部门 |
| Project | code | DEPENDS_ON、REQUIRES |
| Task | code | OF_PROJECT、BLOCKS。`projectCode` 是给复合索引用的冗余字段 |
| Document | code | ABOUT、TAGGED |
| Skill / Tag | name | 被上面的关系引用 |
| TxProbe | key = guide | 只给事务提交和回滚演示用 |

样例人物是安娜、博文、陈曦、杜晴、伊芙、方宁。质量组 `QA` 故意没有成员，用来看 `OPTIONAL MATCH`。

## 启动顺序

1. 启动 Neo4j 4.3 或 4.4，打开 `7474`。账号沿用 `application.properties` 里的 `neo4j` / `hadoop`。
2. 启动本应用。旧的 Person 练习如果连不上 Bolt，控制台会打印一行原因，`/api/neo4j/guide` 继续可用。
3. 初始化模式，再灌入样例。重复灌入会先删掉上表里的标签，不会删除 `Person`、`Book`。

```text
curl.exe -s http://localhost:8080/api/neo4j/guide/health
curl.exe -s -X POST http://localhost:8080/api/neo4j/guide/schema/init
curl.exe -s -X POST http://localhost:8080/api/neo4j/guide/seed
curl.exe -s http://localhost:8080/api/neo4j/guide/query
```

索引刚建好时 `SHOW INDEXES` 的 `state` 可能还是 `POPULATING`。变成 `ONLINE` 后再看执行计划。

## 文档索引

| 文档 | 场景 | 代码 |
| --- | --- | --- |
| [基本语法](01-basic-syntax.md) | MATCH、WHERE、OPTIONAL MATCH、WITH、UNION、UNWIND、CASE、子查询、分页 | `CypherCatalog` 中 `syntax-*` |
| [语句](02-statements.md) | CREATE、MERGE、SET、REMOVE、DELETE、DETACH DELETE、FOREACH | `MemberGuideService`、`RelationGuideService` |
| [函数](03-functions.md) | 字符串、列表、谓词、聚合、时间、路径函数 | `function-*` |
| [索引](04-indexes.md) | 单属性、复合、关系属性、Lookup | `SchemaScripts` |
| [全文索引](05-fulltext-index.md) | 节点和关系全文索引，CJK 分析器 | `SearchGuideService` |
| [约束](06-constraints.md) | 唯一约束；存在约束只在企业版生效 | `GraphSchemaService` |
| [查询调优](07-query-tuning.md) | 参数化、索引选择、笛卡尔积、变长路径、CONTAINS | `tuning` 分类 |
| [执行计划](08-execution-plan.md) | EXPLAIN、PROFILE、算子和 DbHits | `PlanGuideService` |
| [事务、批量与路径](09-transactions-batch-path.md) | 提交、回滚、UNWIND、最短路径、推荐 | `GraphSeedStatements`、`TransactionGuideService` |
| [Spring 集成](10-spring-integration.md) | 和旧 SDN 代码的边界、驱动选择、事务边界 | `Neo4jHttpClient` |

查询目录本身就是接口：`GET /api/neo4j/guide/query` 返回每条语句的名字、分类、Cypher 和默认参数。`GET /api/neo4j/guide/query/{name}` 用默认参数执行，也可以用同名查询参数覆盖。
