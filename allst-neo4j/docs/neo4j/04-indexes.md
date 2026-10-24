# 索引

初始化语句在 `SchemaScripts`，由 `POST /api/neo4j/guide/schema/init` 逐条提交。模式语句各自独立提交：Neo4j 不允许把多条模式变更和数据写入塞进同一个事务。语句都带 `IF NOT EXISTS`，重复调用不会因为同名规则报错。

查看当前库：

```text
curl.exe -s http://localhost:8080/api/neo4j/guide/schema/indexes
```

`state` 为 `ONLINE` 之后，优化器才会稳定地选用它。`populationPercent` 还没到 100 时，`PROFILE` 可能仍显示标签扫描。

## 建了哪些索引

唯一约束会自带支持索引，所以邮箱、各类 `code`、技能名、标签名不再单独 `CREATE INDEX`。额外的索引只有过滤或排序真正用得到的字段：

| 名称 | 语句要点 | 给谁用 |
| --- | --- | --- |
| guide_member_name | `FOR (m:Member) ON (m.name)` | 按姓名等值查找 |
| guide_project_status | `FOR (p:Project) ON (p.status)` | 按项目状态筛选 |
| guide_task_project_status | `FOR (t:Task) ON (t.projectCode, t.status)` | 项目加状态 |
| guide_document_updated | `FOR (d:Document) ON (d.updatedAt)` | 更新时间范围 |
| guide_works_on_role | `FOR ()-[r:WORKS_ON]-() ON (r.role)` | 按参与角色过滤关系 |
| guide_node_label_lookup | `FOR (n) ON EACH labels(n)` | 加速按标签取节点 |
| guide_rel_type_lookup | `FOR ()-[r]-() ON EACH type(r)` | 加速按关系类型扫描 |

`city` 故意没有索引，用来和邮箱索引对照，见 [查询调优](07-query-tuning.md)。

## 复合索引

```cypher
CREATE INDEX guide_task_project_status IF NOT EXISTS
FOR (t:Task) ON (t.projectCode, t.status)
```

能用上它的条件是从左到右的等值前缀。`projectCode` 和 `status` 都等值，走 `seek-task-composite`。只有 `status` 时用不上这条索引，对应 `scan-task-status`。

`projectCode` 和 `OF_PROJECT` 关系是重复信息。关系负责回答“任务属于哪个项目”，属性负责让列表筛选不必先展开关系。同步写入放在同一条 `UNWIND` 里，避免只改一边。

## Lookup 索引

Neo4j 4.3 可以为“所有标签”和“所有关系类型”建 Lookup 索引。图里标签变多以后，`MATCH (n:Member)` 这种纯标签查询会受益。名称固定成 `guide_node_label_lookup` 和 `guide_rel_type_lookup`，删除时只删这两个名字。如果库里已经有别人建的同模式 Lookup 索引，`IF NOT EXISTS` 不会再新建，也不会把别人的索引删掉。

## 提示

`hint-member-name` 写了 `USING INDEX m:Member(name)`。优化器已经会选择这条索引时，提示没有收益。只在 `PROFILE` 证明选错计划、并且索引确实存在时才保留提示。

Neo4j 5 把这里的 B 树索引换成了 RANGE 索引，`CREATE INDEX ... FOR (n:Label) ON (n.prop)` 这句写法仍然可用。示例不写 `BTREE` 关键字，避免升到 5.x 之后语句失效。
