# 基本语法

对应目录分类 `syntax`，实现在 `CypherCatalog`。执行方式：

```text
curl.exe -s "http://localhost:8080/api/neo4j/guide/query/syntax-where"
curl.exe -s "http://localhost:8080/api/neo4j/guide/query/syntax-where?city=杭州"
```

先 `POST /api/neo4j/guide/seed`。覆盖参数时，空字符串会保留默认值，目录里没有的参数会被丢掉，避免把无关查询串拼进 Cypher。

## 约定

- 业务条件全部用 `$参数`，Java 侧放进参数 Map。
- 返回列使用 `AS` 起稳定的名字，接口按列名组装 Map，不把整颗节点直接扔给调用方。
- `MATCH` 一定带标签。需要业务键时再写属性条件。
- 排序字段放在 `SKIP` / `LIMIT` 前面。

## 语句对照

| 名字 | 在业务里做什么 |
| --- | --- |
| syntax-where | 在职且城市为上海的成员。等值条件写成 `m.city = $city` |
| syntax-distinct | 成员出现过哪些城市 |
| syntax-optional | 部门人数。质量组没有成员，`OPTIONAL MATCH` 仍返回该部门，`count(m)` 为 0 |
| syntax-with | 先按技能计数，再用 `WHERE people >= $minPeople` 去掉只出现一次的技能 |
| syntax-union | 项目名和部门名合成一份名单。两边的列名必须一致 |
| syntax-unwind | `collect` 成列表后再展开，适合把聚合结果重新变成行 |
| syntax-case | 项目状态 `ACTIVE` / `PLANNING` 转成中文 |
| syntax-subquery | `CALL { ... }` 先在子查询里聚合，外层再排序 |
| syntax-pattern | `[(m)-[r:HAS_SKILL]->(s) \| s.name + ':' + toString(r.level)]` 直接收集技能 |
| syntax-page | `ORDER BY m.name SKIP $skip LIMIT $limit` |
| syntax-keyset | `WHERE $after = '' OR m.email > $after`，按邮箱向后翻页 |

`UNION` 会去重，`UNION ALL` 保留重复行。这份样例的项目名和部门名没有交集，两条结果一样。列类型也要兼容，不能一边返回字符串、另一边返回节点。

## 分页怎么选

`syntax-page` 的 `SKIP` 会把前面的行读出来再丢掉，页码越大越慢。列表接口 `GET /api/neo4j/guide/members` 用它，是因为样例数据很小，调用方也习惯页码。

数据量上来以后改用 `syntax-keyset`：记住上一页最后一条 `email`，用 `m.email > $after` 配合 `guide_member_email_unique` 的索引。邮箱是唯一业务键，适合做游标。

## 模式里容易写错的地方

`OPTIONAL MATCH` 后面的 `WHERE` 过滤的是可选部分。把过滤写到后面的 `WITH` 上，会把外层行一起删掉，可选匹配就失效了。

变长路径必须写上限，目录里的部门查询用的是 `[:CHILD_OF*0..5]`。`*0` 表示包含起点自己，这样根部门也能匹配到直属成员。无上限的 `[*]` 不放进业务接口。
