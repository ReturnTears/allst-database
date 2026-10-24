# 查询调优

调优查询都在目录分类 `tuning`。对比两组计划：

```text
curl.exe -s "http://localhost:8080/api/neo4j/guide/plan/compare?left=scan-member-city&right=seek-member-email"
curl.exe -s "http://localhost:8080/api/neo4j/guide/plan/compare?left=scan-task-status&right=seek-task-composite"
curl.exe -s "http://localhost:8080/api/neo4j/guide/plan/compare?left=cartesian-bad&right=cartesian-good"
curl.exe -s "http://localhost:8080/api/neo4j/guide/plan/compare?left=filter-contains&right=fulltext-document"
```

`compare` 使用每条查询的默认参数，并且真正执行 `PROFILE`。样例图很小，笛卡尔积查询也带了 `LIMIT 100`。生产图上不要对无界笛卡尔积做 `PROFILE`。

## 先保证优化器有机会选对

1. 参数化。把邮箱拼进字符串会让计划缓存失效，也有注入风险。目录测试会检查每条只读语句里的 `$参数` 都有默认值。
2. `MATCH` 带标签，过滤尽量写在展开大路径之前。
3. 等值和范围才考虑 B 树或 RANGE 索引。`CONTAINS`、`STARTS WITH` 的中间匹配走全文索引。
4. 复合索引从左侧属性开始给等值。只给右侧属性时，计划回到标签扫描。这就是 `scan-task-status` 和 `seek-task-composite` 的差别。
5. 变长路径写死上限。`path-depth-2` 和 `path-depth-6` 的结果在这张小图上接近，计划里的最大跳数不同。无上限路径不提供接口。
6. 分页优先键集，而不是大 `SKIP`。见 [基本语法](01-basic-syntax.md)。

## 这张图上的对照

| 查询 | 期望看到的访问方式 |
| --- | --- |
| seek-member-email | `NodeUniqueIndexSeek`，谓词是 `m.email` |
| seek-member-name | `NodeIndexSeek`，走 `guide_member_name` |
| scan-member-city | `NodeByLabelScan` 加 `Filter`。城市没有索引 |
| seek-task-composite | 复合索引等值查找 |
| scan-task-status | 标签扫描。`status` 不是复合索引的左前缀 |
| range-document-updated | `updatedAt` 的范围条件 |
| cartesian-bad | `CartesianProduct`。成员和部门没有连起来 |
| cartesian-good | `Expand`。`(m)-[:BELONGS_TO]->(d)` |
| filter-contains | 标签扫描后过滤正文 |
| fulltext-document | `ProcedureCall`，进入 `guide_document_text` |
| semi-apply-skill | `EXISTS` 子查询，只判断有没有技能，不把技能行乘开 |

`hint-member-name` 演示 `USING INDEX`。计划已经是索引查找时，删掉提示，语句更不容易在索引改名后失效。

## 写入侧配合

- 列表要按 `projectCode + status` 查任务时，创建任务的同一条语句里写上 `projectCode`，不要只连 `OF_PROJECT` 再临时遍历。
- 一个成员只保留一条 `BELONGS_TO`，否则“我的部门”会变成多行，调用方还得去重。
- 推荐和技能差距用关系表达，不把同事列表存成数组属性。数组适合返回给接口，不适合作为可遍历的图。

改完语句后看 `totalDbHits` 和算子类型，而不是只看样例数据上的毫秒数。样例只有几十个节点，耗时差别说明不了问题，算子类型可以。
