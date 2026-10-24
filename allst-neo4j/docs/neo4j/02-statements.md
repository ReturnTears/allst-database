# 语句

写入集中在 `MemberGuideService`、`RelationGuideService` 和 `GraphSeedStatements`。读语句不放 `CREATE`、`MERGE`、`SET`、`DELETE`，这样 `PROFILE` 不会改图。

## CREATE 和 MERGE

`POST /api/neo4j/guide/members` 使用 `CREATE`。邮箱上有唯一约束，重复提交返回 409。

```cypher
CREATE (m:Member {email: $email, name: $name, active: true, createdAt: datetime()})
SET m.title = $title,
    m.city = $city,
    m.hiredAt = CASE WHEN $hiredAt IS NULL OR $hiredAt = '' THEN null ELSE date($hiredAt) END
```

`POST /api/neo4j/guide/members/batch` 和样例灌数使用 `MERGE`，按业务键对齐，已存在就更新属性。单次批量最多 200 行。

```cypher
UNWIND $rows AS row
MERGE (m:Member {email: row.email})
SET m.name = row.name, m.updatedAt = datetime()
```

关系同样按两端节点 `MERGE`，避免联调时重复点击产生多条边。`POST /api/neo4j/guide/relations/knows`、`works-on`、`manages` 都是这个形状。

成员通常只属于一个部门。`POST /api/neo4j/guide/relations/belongs-to` 会先 `DELETE` 旧的 `BELONGS_TO`，再 `MERGE` 新部门。项目关系不是互斥的，所以 `WORKS_ON` 只增不删。

## SET、REMOVE、DELETE

| 接口 | 语句 | 业务含义 |
| --- | --- | --- |
| `PATCH /members/{email}` | `SET m.name = coalesce($name, m.name)` | 没传的字段保持原值。`coalesce` 不能把字段改成 null |
| `POST /members/{email}/deactivate` | `SET m.active = false` | 停用，关系和历史都还在 |
| `DELETE /members/{email}/remark` | `REMOVE m.remark` | 删掉一个属性，节点还在 |
| `DELETE /relations/knows?from=&to=` | `DELETE r` | 只删这一条关系 |
| `DELETE /members/{email}` | `DETACH DELETE m` | 节点和它的全部关系一起删掉 |

停用和物理删除分开。业务上的离职用 `active = false`。`DETACH DELETE` 留给确定不要保留协作历史的场景，删完可以用 `POST /seed` 把样例恢复回来。

`DELETE r` 之后不要再读取 `r` 的属性。先 `WITH a, b, r`，删除后返回两端的业务键。

## FOREACH

`POST /api/neo4j/guide/documents/DOC1/tags`，请求体 `{"tags":["索引","复盘"]}`。

```cypher
MATCH (d:Document {code: $code})
FOREACH (tagName IN $tags |
  MERGE (t:Tag {name: tagName})
  MERGE (d)-[:TAGGED]->(t)
)
```

`FOREACH` 里只能做写操作，不能 `MATCH`。需要按条件查找时，改用 `UNWIND` 加普通 `MATCH`。

## 样例重建

`POST /api/neo4j/guide/seed` 在一个事务里先执行：

```cypher
MATCH (n)
WHERE n:Member OR n:Department OR n:Project OR n:Task
   OR n:Document OR n:Skill OR n:Tag OR n:TxProbe
DETACH DELETE n
```

然后再 `UNWIND` 重建。事务失败时，删除和重建一起回滚。`DELETE /api/neo4j/guide/seed` 只做上面的清理。

这两条都不会匹配 `Person` 或 `Book`。
