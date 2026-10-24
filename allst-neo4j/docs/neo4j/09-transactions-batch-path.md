# 事务、批量写入和路径

这些是语法和索引之外、业务图里反复出现的三类写法。

## 显式事务

`Neo4jHttpClient.begin()` 对应 `POST /db/neo4j/tx`。同一事务里的语句打到返回的事务地址，`commit()` 打到 `/commit`，没提交就关闭则 `DELETE` 回滚。

```text
curl.exe -s -X POST "http://localhost:8080/api/neo4j/guide/tx/commit-demo?marker=committed"
curl.exe -s -X POST http://localhost:8080/api/neo4j/guide/tx/rollback-demo
```

提交演示在一个事务里先改 `TxProbe`，再把它读回来，两次应该看到同一个 `marker` 和递增后的 `runs`。回滚演示先记下当前值，在事务里改成 `rollback-should-not-persist` 然后关闭，再读一次。`unchanged` 应为 true。

样例灌数也是一个事务：清理和全部 `UNWIND` 要么一起成功，要么一起回到调用前。模式变更不放进这个事务。

不要在 `Neo4jHttpClient` 上加 Spring 的 `@Transactional`。当前没有连接这条 HTTP 调用的事务管理器，注解不会回滚 Neo4j。旧的 `PersonRepository` 走的是另一套 OGM 会话，两套写入也不会互相回滚。

## UNWIND 批量

`GraphSeedStatements` 和 `POST /members/batch` 把行集合作为 `$rows` 一次提交。这比在 Java 里循环单条 `CREATE` 少很多次网络往返，也让唯一约束在同一批里生效。

批量语句保持“一个业务动作一条 Cypher”：部门、成员、关系、任务分开，失败信息能对应到具体动作。行数限制为 200。更大的导入再拆批，仍然使用参数，不拼接文本。

## 部门树和最短路径

`department-members` 从指定部门沿着 `CHILD_OF` 向下 0 到 5 跳，再找 `BELONGS_TO` 成员。默认部门 `RD` 包含研发中心、后端组和前端组，不包含产品中心。

`department-tree` 向上找到没有父部门的根。`RD-BE` 的 `upToRoot` 是 `["RD-BE","RD"]`。

`shortest-path`：

```cypher
MATCH (a:Member {email: $from}), (b:Member {email: $to})
MATCH p = shortestPath((a)-[:KNOWS|MANAGES*1..6]-(b))
RETURN [n IN nodes(p) | n.email] AS emails,
       [r IN relationships(p) | type(r)] AS relationshipTypes,
       length(p) AS hops
```

默认从博文到杜晴，中间可以走熟人或上下级，方向不限，最长 6 跳。没有路径时返回空列表，而不是抛错。起点和终点相同也不会有结果，因为下限是 1 跳。

## 推荐和技能差距

`recommend` 找和自己有共同技能、但还没有 `KNOWS` 的人，用双方技能等级之和排序。默认杜晴会推荐博文：两人共同技能是 Cypher，杜晴和陈曦已经有 `KNOWS`，所以陈曦被排除。

`colleagues` 是同一项目上的其他人，用来解释“为什么推荐他”之外的直接协作关系。

`skill-gap` 把项目 `REQUIRES` 的等级和本人 `HAS_SKILL` 比较。陈曦参与知识图谱平台，但没有 SpringBoot 技能，结果里会留下这个缺口。没有技能时 `coalesce(mine.level, 0)` 按 0 级计算。

`project-risk` 用 `BLOCKS` 列出任务挡住了谁。`T2` 挡住 `T3`。

这些查询的默认参数可以直接调用：

```text
curl.exe -s http://localhost:8080/api/neo4j/guide/query/recommend
curl.exe -s http://localhost:8080/api/neo4j/guide/query/skill-gap
curl.exe -s http://localhost:8080/api/neo4j/guide/query/shortest-path
```
