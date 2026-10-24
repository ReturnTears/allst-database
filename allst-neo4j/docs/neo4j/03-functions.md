# 函数

函数示例是只读查询，默认成员是 `chen@allst.dev`。

```text
curl.exe -s "http://localhost:8080/api/neo4j/guide/query/function-showcase"
curl.exe -s "http://localhost:8080/api/neo4j/guide/query/function-aggregation"
curl.exe -s "http://localhost:8080/api/neo4j/guide/query/function-relationship"
```

`id()` 在结果里叫 `internalId`。它是存储内部编号，会随备份和导入改变。业务关联用 `email`、`code`、`name`。Neo4j 5 起更常见的是 `elementId()`，4.3 没有这个函数，所以示例不调用它。

## 标量和图函数

`function-showcase` 返回一行，里面包括：

| 函数 | 用途 |
| --- | --- |
| `labels`、`keys` | 节点的标签和属性名 |
| `id` | 内部编号，只用于排查，不入库到其他系统 |
| `coalesce` | 备注为空时显示“无备注” |
| `toString`、`toInteger`、`toFloat`、`toBoolean` | 和接口、CSV 之间做类型转换 |

属性是否存在用 `m.email IS NOT NULL`。旧的 `exists(m.email)` 已经不作为写法。

## 字符串

同一行里还有 `toLower`、`toUpper`、`left`、`size`、`trim`、`replace`、`substring`、`split`、`reverse`。`substring` 的下标从 0 开始。`size` 用于字符串和列表；路径长度用 `length(path)`，不要混用。

## 列表和谓词

技能先被收成 `skillNames` 和 `levels`，再演示：

- `head`、`tail`、`last`、`size`
- `range`、`reduce`
- `[lv IN levels WHERE lv >= 4]`
- `all`、`any`、`none`、`single`

陈曦的 Neo4j 和 Cypher 都是 5 级，所以 `anyExpert` 为 true，`exactlyOneExpert` 为 false。空列表上 `all` 为 true、`any` 为 false，这是谓词的定义，不是数据错误。

`EXISTS { MATCH (m)-[:HAS_SKILL]->(:Skill {name: 'Neo4j'}) }` 只判断有没有这种关系，不把技能行乘到结果里。

## 时间和数值

`date`、`datetime`、`duration.between` 用来算入职时长。接口层统一 `toString`，避免调用方解析驱动自己的时间结构。

数值部分是 `abs`、`round(value, precision)`、`ceil`、`floor`、`sign`。带精度的 `round` 从 Neo4j 4.2 开始可用，和本仓库要求的 4.3 一致。

## 聚合

`function-aggregation` 对三笔项目预算计算 `count`、`sum`、`avg`、`min`、`max`、`stDev`、`percentileCont`，并用 `collect` 带回项目编码。`count(p)` 不计算空值；`OPTIONAL MATCH` 没有匹配到时，`count` 得到 0，这是 `syntax-optional` 里部门人数的算法。

## 关系函数

`function-relationship`：

```cypher
MATCH ()-[r:KNOWS]->()
RETURN startNode(r).email AS fromEmail,
       endNode(r).email AS toEmail,
       type(r) AS relType,
       keys(r) AS relKeys,
       properties(r) AS relProps
LIMIT 5
```

路径函数 `nodes`、`relationships`、`length`、`shortestPath` 放在 [事务、批量与路径](09-transactions-batch-path.md)，因为它们直接服务于协作距离，而不是单行换算。
