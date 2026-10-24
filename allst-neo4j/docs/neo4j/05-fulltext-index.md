# 全文索引

B 树索引服务等值和范围。`CONTAINS`、跨字段搜索和中文检索走全文索引。初始化时会创建三条，分析器都是 `cjk`：

```cypher
CREATE FULLTEXT INDEX guide_document_text IF NOT EXISTS
FOR (n:Document) ON EACH [n.title, n.summary, n.content]
OPTIONS {indexConfig: {`fulltext.analyzer`: 'cjk'}}

CREATE FULLTEXT INDEX guide_task_text IF NOT EXISTS
FOR (n:Task) ON EACH [n.title, n.description]
OPTIONS {indexConfig: {`fulltext.analyzer`: 'cjk'}}

CREATE FULLTEXT INDEX guide_knows_note IF NOT EXISTS
FOR ()-[r:KNOWS]-() ON EACH [r.note]
OPTIONS {indexConfig: {`fulltext.analyzer`: 'cjk'}}
```

`cjk` 按二元组切中文，不需要额外分词插件。英文标准分析器对“索引”“慢查询”这类词几乎不可用，所以业务字段直接指定分析器，不依赖默认值。

## 查询

```text
curl.exe -s "http://localhost:8080/api/neo4j/guide/search/documents?q=索引"
curl.exe -s "http://localhost:8080/api/neo4j/guide/search/knows?q=慢查询"
```

文档查询：

```cypher
CALL db.index.fulltext.queryNodes('guide_document_text', $q) YIELD node, score
RETURN node.code AS code, node.title AS title, node.summary AS summary, score
ORDER BY score DESC
LIMIT $limit
```

关系查询使用 `db.index.fulltext.queryRelationships`，再取 `startNode` 和 `endNode` 的邮箱。`guide_knows_note` 能命中“一起排查过慢查询”。

目录里的 `fulltext-document` 是同一类查询，方便和 `filter-contains` 做 `PROFILE` 对比：

```text
curl.exe -s "http://localhost:8080/api/neo4j/guide/plan/compare?left=filter-contains&right=fulltext-document"
```

## 输入处理

全文查询字符串是 Lucene 语法，`+`、`-`、`:`、引号等会改变含义。`LuceneQuerySanitizer` 会转义这些字符，中文原样保留。这是业务搜索框的默认策略。需要手工写 Lucene 高级语法时，放到 cypher-shell 里执行，不从接口透传。

`q` 不能为空，最长 80 个字符。`limit` 限制在 1 到 50。

索引不存在时，Neo4j 会返回找不到全文索引。先调用 `POST /api/neo4j/guide/schema/init`，并确认 `state` 已经是 `ONLINE`。
