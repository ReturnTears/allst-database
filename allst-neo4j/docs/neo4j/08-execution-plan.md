# 执行计划

`EXPLAIN` 只生成计划，不访问数据，也没有 `DbHits`。`PROFILE` 会执行语句，并在每个算子上给出实际行数和数据库命中次数。目录里的语句都是只读的，`PlanGuideService` 也只给这些语句加前缀。

```text
curl.exe -s "http://localhost:8080/api/neo4j/guide/plan/seek-member-email/explain"
curl.exe -s "http://localhost:8080/api/neo4j/guide/plan/seek-member-email/profile"
```

返回体里：

| 字段 | 含义 |
| --- | --- |
| cypher | 实际发给 Neo4j 的语句，前面带 `EXPLAIN` 或 `PROFILE` |
| parameters | 合并默认值之后的参数 |
| operators | 从计划树摊平的算子，保留父子顺序的前序 |
| totalDbHits | 各算子 `DbHits` 之和。`EXPLAIN` 时为 null |
| rawPlan | Neo4j 原始计划，字段名以服务器返回为准 |
| rows | `PROFILE` 的结果行。`EXPLAIN` 为空 |

`PlanFormatter` 同时读取算子顶层和 `args` 里的 `Details`、`EstimatedRows`、`DbHits`，兼容 4.3 HTTP 响应的两种摆放方式。

## 常见算子

| 算子 | 说明 |
| --- | --- |
| ProduceResults | 把最终列交出来 |
| NodeByLabelScan | 扫描某个标签下的全部节点 |
| NodeIndexSeek | 普通索引等值或范围查找 |
| NodeUniqueIndexSeek | 唯一约束支持的等值查找 |
| Filter | 索引消化不了的剩余条件 |
| Expand(All) / Expand(Into) | 沿着关系走到相邻节点 |
| VarLengthExpand | 变长路径。细节里能看到跳数上限 |
| CartesianProduct | 两个独立行集合相乘 |
| Apply / SemiApply | 子查询或 `EXISTS` 的相关执行 |
| ProcedureCall | 存储过程，全文检索会走这里 |
| Sort / Skip / Limit / Top | 排序和分页 |
| Eager | 为了正确性把流水线打断。频繁出现时要回头看有没有不必要的全量聚合 |
| NodeByIdSeek | 按内部 id 定位。业务查询不应该依赖它 |

计划文本有时放在 `string-representation`。4.3 的 HTTP 响应不一定带这段文本，这时以 `operators` 和 `rawPlan` 为准。

## 怎么读一次 PROFILE

以邮箱查询为例，期望的核心算子是 `NodeUniqueIndexSeek`，`details` 里出现 `m.email`，`DbHits` 是很小的常数。如果这里变成 `NodeByLabelScan`，按下面的顺序查：

1. `GET /api/neo4j/guide/schema/indexes` 里有没有邮箱唯一约束对应的索引，`state` 是不是 `ONLINE`。
2. 条件是不是写在参数上，而不是一个表达式包住属性，例如 `toLower(m.email) = $email` 会让索引失效。
3. 标签是不是 `Member`。没有标签时优化器无法使用这个约束索引。

城市查询则应该保持标签扫描。给它建索引之前，先确认这个条件出现在真实的高频接口里。样例故意留着这个反例，避免把所有属性都建成索引。
