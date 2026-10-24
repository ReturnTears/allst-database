# 约束

社区版示例创建的是唯一约束。名字和业务键一一对应：

| 约束 | 作用 |
| --- | --- |
| guide_member_email_unique | 成员邮箱 |
| guide_department_code_unique | 部门编码 |
| guide_project_code_unique | 项目编码 |
| guide_task_code_unique | 任务编码 |
| guide_document_code_unique | 文档编码 |
| guide_skill_name_unique | 技能名 |
| guide_tag_name_unique | 标签名 |

Neo4j 4.3 的写法：

```cypher
CREATE CONSTRAINT guide_member_email_unique IF NOT EXISTS
ON (m:Member) ASSERT m.email IS UNIQUE
```

Neo4j 5 去掉了 `ASSERT`，`SchemaScripts` 在主版本大于等于 5 时改写为：

```cypher
CREATE CONSTRAINT guide_member_email_unique IF NOT EXISTS
FOR (m:Member) REQUIRE m.email IS UNIQUE
```

唯一约束会创建同名支持索引。按邮箱查找成员时期望看到 `NodeUniqueIndexSeek`，而不是普通的 `NodeIndexSeek`。因此这些字段不再另建一条 B 树索引。

`POST /api/neo4j/guide/members` 遇到重复邮箱时，HTTP 状态是 409，响应里带 Neo4j 的 `ConstraintValidationFailed`。批量接口用 `MERGE`，重复邮箱会更新原节点，不会触发这个错误。

## 企业版才有的约束

属性存在约束和节点键只在企业版提供。初始化会尝试一条，失败就记入 `skipped`，不影响前面的唯一约束：

```cypher
CREATE CONSTRAINT guide_member_name_exists IF NOT EXISTS
ON (m:Member) ASSERT m.name IS NOT NULL
```

社区版的报错大意是存在约束需要 Enterprise Edition。应用层仍然要校验 `name` 非空，不能把这个约束当成社区版的防线。

节点键 `IS NODE KEY` 同时要求属性存在并且组合唯一，也是企业版能力。邮箱已经有唯一约束，示例不再对同一属性建节点键，避免两条规则抢同一个模式。

## 删除

`POST /api/neo4j/guide/schema/drop` 只删除 `guide_` 开头、由本示例创建的约束和索引：

```cypher
DROP CONSTRAINT guide_member_email_unique IF EXISTS
DROP INDEX guide_member_name IF EXISTS
```

它不会清空数据，也不会碰旧练习留下的索引。删掉约束之后，`CREATE` 成员不再拦截重复邮箱，需要数据正确性时重新 `schema/init`。
