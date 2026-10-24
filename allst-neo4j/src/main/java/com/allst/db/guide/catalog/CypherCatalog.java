package com.allst.db.guide.catalog;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 只读查询目录。写入语句在各个 GuideService 里，避免 PROFILE 误执行写操作。
 * 说明见 docs/neo4j/01-basic-syntax.md、03-functions.md、07-query-tuning.md、09-transactions-batch-path.md。
 */
@Component
public class CypherCatalog {

    private final List<NamedReadQuery> queries;

    public CypherCatalog() {
        List<NamedReadQuery> built = build();
        Set<String> names = new HashSet<>();
        for (NamedReadQuery query : built) {
            if (!names.add(query.getName())) {
                throw new IllegalStateException("重复查询名: " + query.getName());
            }
        }
        this.queries = Collections.unmodifiableList(built);
    }

    public List<NamedReadQuery> all() {
        return queries;
    }

    public Optional<NamedReadQuery> find(String name) {
        for (NamedReadQuery query : queries) {
            if (query.getName().equals(name)) {
                return Optional.of(query);
            }
        }
        return Optional.empty();
    }

    private static List<NamedReadQuery> build() {
        List<NamedReadQuery> queries = new ArrayList<>();
        queries.add(syntaxWhere());
        queries.add(syntaxDistinct());
        queries.add(syntaxOptional());
        queries.add(syntaxWith());
        queries.add(syntaxUnion());
        queries.add(syntaxUnwind());
        queries.add(syntaxCase());
        queries.add(syntaxSubquery());
        queries.add(syntaxPattern());
        queries.add(syntaxPage());
        queries.add(syntaxKeyset());
        queries.add(functionShowcase());
        queries.add(functionAggregation());
        queries.add(functionRelationship());
        queries.add(departmentMembers());
        queries.add(departmentTree());
        queries.add(colleagues());
        queries.add(shortestPath());
        queries.add(recommend());
        queries.add(skillGap());
        queries.add(projectRisk());
        queries.add(seekMemberEmail());
        queries.add(scanMemberCity());
        queries.add(seekMemberName());
        queries.add(hintMemberName());
        queries.add(seekTaskComposite());
        queries.add(scanTaskStatus());
        queries.add(rangeDocumentUpdated());
        queries.add(cartesianBad());
        queries.add(cartesianGood());
        queries.add(pathDepth2());
        queries.add(pathDepth6());
        queries.add(filterContains());
        queries.add(fulltextDocument());
        queries.add(semiApplySkill());
        return queries;
    }

    private static NamedReadQuery syntaxWhere() {
        return NamedReadQuery.builder("syntax-where", "syntax", "MATCH、WHERE、RETURN、ORDER BY")
                .cypher(q(
                        "MATCH (m:Member)",
                        "WHERE m.active = $active AND m.city = $city",
                        "RETURN m.email AS email, m.name AS name, m.title AS title",
                        "ORDER BY email"))
                .param("active", true)
                .param("city", "上海")
                .build();
    }

    private static NamedReadQuery syntaxDistinct() {
        return NamedReadQuery.builder("syntax-distinct", "syntax", "DISTINCT 去重")
                .cypher(q(
                        "MATCH (m:Member)",
                        "RETURN DISTINCT m.city AS city",
                        "ORDER BY city"))
                .build();
    }

    private static NamedReadQuery syntaxOptional() {
        return NamedReadQuery.builder("syntax-optional", "syntax", "OPTIONAL MATCH 保留没有成员的部门")
                .cypher(q(
                        "MATCH (d:Department)",
                        "OPTIONAL MATCH (d)<-[:BELONGS_TO]-(m:Member)",
                        "RETURN d.code AS code, d.name AS name, count(m) AS members",
                        "ORDER BY code"))
                .build();
    }

    private static NamedReadQuery syntaxWith() {
        return NamedReadQuery.builder("syntax-with", "syntax", "WITH 聚合后再过滤")
                .cypher(q(
                        "MATCH (m:Member)-[:HAS_SKILL]->(s:Skill)",
                        "WITH s.name AS skill, count(m) AS people",
                        "WHERE people >= $minPeople",
                        "RETURN skill, people",
                        "ORDER BY people DESC, skill ASC"))
                .param("minPeople", 2)
                .build();
    }

    private static NamedReadQuery syntaxUnion() {
        return NamedReadQuery.builder("syntax-union", "syntax", "UNION 合并项目名和部门名")
                .cypher(q(
                        "MATCH (p:Project)",
                        "RETURN p.name AS name, 'project' AS kind",
                        "UNION",
                        "MATCH (d:Department)",
                        "RETURN d.name AS name, 'department' AS kind"))
                .build();
    }

    private static NamedReadQuery syntaxUnwind() {
        return NamedReadQuery.builder("syntax-unwind", "syntax", "collect 后再 UNWIND 展开城市")
                .cypher(q(
                        "MATCH (m:Member)",
                        "WITH collect(m.city) AS cities",
                        "UNWIND cities AS city",
                        "RETURN DISTINCT city",
                        "ORDER BY city"))
                .build();
    }

    private static NamedReadQuery syntaxCase() {
        return NamedReadQuery.builder("syntax-case", "syntax", "CASE 把状态码转成中文")
                .cypher(q(
                        "MATCH (p:Project)",
                        "RETURN p.code AS code, p.name AS name,",
                        "       CASE p.status",
                        "         WHEN 'ACTIVE' THEN '进行中'",
                        "         WHEN 'PLANNING' THEN '规划中'",
                        "         WHEN 'DONE' THEN '已完成'",
                        "         ELSE '未知'",
                        "       END AS statusText",
                        "ORDER BY code"))
                .build();
    }

    private static NamedReadQuery syntaxSubquery() {
        return NamedReadQuery.builder("syntax-subquery", "syntax", "CALL 子查询先聚合再排序")
                .cypher(q(
                        "CALL {",
                        "  MATCH (m:Member)-[:BELONGS_TO]->(d:Department)",
                        "  RETURN d.name AS department, count(*) AS members",
                        "}",
                        "RETURN department, members",
                        "ORDER BY members DESC, department ASC"))
                .build();
    }

    private static NamedReadQuery syntaxPattern() {
        return NamedReadQuery.builder("syntax-pattern", "syntax", "模式推导收集技能")
                .cypher(q(
                        "MATCH (m:Member)",
                        "RETURN m.email AS email,",
                        "       [(m)-[r:HAS_SKILL]->(s:Skill) | s.name + ':' + toString(r.level)] AS skills",
                        "ORDER BY email"))
                .build();
    }

    private static NamedReadQuery syntaxPage() {
        return NamedReadQuery.builder("syntax-page", "syntax", "ORDER BY 之后 SKIP、LIMIT")
                .cypher(q(
                        "MATCH (m:Member)",
                        "RETURN m.email AS email, m.name AS name",
                        "ORDER BY m.name",
                        "SKIP $skip LIMIT $limit"))
                .param("skip", 0)
                .param("limit", 3)
                .build();
    }

    private static NamedReadQuery syntaxKeyset() {
        return NamedReadQuery.builder("syntax-keyset", "syntax", "按邮箱做键集分页")
                .cypher(q(
                        "MATCH (m:Member)",
                        "WHERE $after = '' OR m.email > $after",
                        "RETURN m.email AS email, m.name AS name",
                        "ORDER BY m.email",
                        "LIMIT $limit"))
                .param("after", "")
                .param("limit", 2)
                .build();
    }

    private static NamedReadQuery functionShowcase() {
        return NamedReadQuery.builder("function-showcase", "function", "字符串、列表、谓词、时间和数值函数")
                .cypher(q(
                        "MATCH (m:Member {email: $email})",
                        "OPTIONAL MATCH (m)-[hs:HAS_SKILL]->(s:Skill)",
                        "WITH m,",
                        "     [x IN collect(DISTINCT s.name) WHERE x IS NOT NULL] AS skillNames,",
                        "     [x IN collect(DISTINCT hs.level) WHERE x IS NOT NULL] AS levels",
                        "RETURN",
                        "  m.name AS name,",
                        "  labels(m) AS labels,",
                        "  keys(m) AS propertyKeys,",
                        "  id(m) AS internalId,",
                        "  coalesce(m.remark, '无备注') AS remarkOrDefault,",
                        "  toLower(m.email) AS emailLower,",
                        "  toUpper(left(m.name, 1)) AS nameInitial,",
                        "  size(m.name) AS nameLength,",
                        "  trim(m.city) AS cityTrimmed,",
                        "  replace(m.email, '@allst.dev', '@example.com') AS replacedEmail,",
                        "  substring(m.email, 0, 4) AS emailPrefix,",
                        "  split(m.email, '@')[0] AS mailbox,",
                        "  reverse(m.city) AS cityReversed,",
                        "  skillNames AS skills,",
                        "  head(skillNames) AS firstSkill,",
                        "  tail(skillNames) AS restSkills,",
                        "  last(skillNames) AS lastSkill,",
                        "  size(skillNames) AS skillCount,",
                        "  range(1, size(levels)) AS levelRanks,",
                        "  reduce(total = 0, lv IN levels | total + lv) AS levelSum,",
                        "  [lv IN levels WHERE lv >= 4] AS highLevels,",
                        "  all(lv IN levels WHERE lv >= 1) AS allAtLeastOne,",
                        "  any(lv IN levels WHERE lv >= 5) AS anyExpert,",
                        "  none(lv IN levels WHERE lv < 1) AS noneBelowOne,",
                        "  single(lv IN levels WHERE lv >= 5) AS exactlyOneExpert,",
                        "  EXISTS { MATCH (m)-[:HAS_SKILL]->(:Skill {name: 'Neo4j'}) } AS knowsNeo4j,",
                        "  m.email IS NOT NULL AS emailPresent,",
                        "  toString(datetime()) AS now,",
                        "  toString(date()) AS today,",
                        "  toString(m.hiredAt) AS hiredAt,",
                        "  toString(duration.between(m.hiredAt, date())) AS tenure,",
                        "  toInteger('42') AS intValue,",
                        "  toFloat('3.5') AS floatValue,",
                        "  toBoolean('true') AS boolValue,",
                        "  abs(-8) AS absValue,",
                        "  round(3.14159, 2) AS rounded,",
                        "  ceil(1.2) AS ceiled,",
                        "  floor(1.8) AS floored,",
                        "  sign(size(m.name) - 2) AS signValue"))
                .param("email", "chen@allst.dev")
                .build();
    }

    private static NamedReadQuery functionAggregation() {
        return NamedReadQuery.builder("function-aggregation", "function", "count、sum、avg、min、max、stDev、百分位")
                .cypher(q(
                        "MATCH (p:Project)",
                        "RETURN count(p) AS projects,",
                        "       sum(p.budget) AS budgetSum,",
                        "       avg(p.budget) AS budgetAvg,",
                        "       min(p.budget) AS budgetMin,",
                        "       max(p.budget) AS budgetMax,",
                        "       stDev(p.budget) AS budgetStdev,",
                        "       percentileCont(p.budget, 0.5) AS budgetMedian,",
                        "       collect(p.code) AS codes"))
                .build();
    }

    private static NamedReadQuery functionRelationship() {
        return NamedReadQuery.builder("function-relationship", "function", "type、keys、properties、startNode、endNode")
                .cypher(q(
                        "MATCH ()-[r:KNOWS]->()",
                        "RETURN startNode(r).email AS fromEmail,",
                        "       endNode(r).email AS toEmail,",
                        "       type(r) AS relType,",
                        "       keys(r) AS relKeys,",
                        "       properties(r) AS relProps,",
                        "       id(r) AS relId",
                        "LIMIT 5"))
                .build();
    }

    private static NamedReadQuery departmentMembers() {
        return NamedReadQuery.builder("department-members", "business", "部门及其下级部门的成员")
                .cypher(q(
                        "MATCH (root:Department {code: $code})",
                        "MATCH (d:Department)-[:CHILD_OF*0..5]->(root)",
                        "MATCH (m:Member)-[:BELONGS_TO]->(d)",
                        "RETURN d.code AS department, m.email AS email, m.name AS name",
                        "ORDER BY department, email"))
                .param("code", "RD")
                .build();
    }

    private static NamedReadQuery departmentTree() {
        return NamedReadQuery.builder("department-tree", "business", "沿 CHILD_OF 找到根部门")
                .cypher(q(
                        "MATCH (d:Department {code: $code})",
                        "OPTIONAL MATCH path = (d)-[:CHILD_OF*1..5]->(root:Department)",
                        "WHERE NOT (root)-[:CHILD_OF]->(:Department)",
                        "RETURN d.code AS code,",
                        "       d.name AS name,",
                        "       CASE WHEN root IS NULL THEN d.code ELSE root.code END AS rootCode,",
                        "       CASE WHEN path IS NULL THEN [d.code] ELSE [n IN nodes(path) | n.code] END AS upToRoot"))
                .param("code", "RD-BE")
                .build();
    }

    private static NamedReadQuery colleagues() {
        return NamedReadQuery.builder("colleagues", "business", "同一个项目上的同事")
                .cypher(q(
                        "MATCH (me:Member {email: $email})-[:WORKS_ON]->(p:Project)<-[w:WORKS_ON]-(other:Member)",
                        "RETURN p.code AS project, p.name AS projectName,",
                        "       other.email AS email, other.name AS name, w.role AS role",
                        "ORDER BY project, email"))
                .param("email", "bo@allst.dev")
                .build();
    }

    private static NamedReadQuery shortestPath() {
        return NamedReadQuery.builder("shortest-path", "business", "熟人与管理关系上的最短路径")
                .cypher(q(
                        "MATCH (a:Member {email: $from}), (b:Member {email: $to})",
                        "MATCH p = shortestPath((a)-[:KNOWS|MANAGES*1..6]-(b))",
                        "RETURN [n IN nodes(p) | n.email] AS emails,",
                        "       [r IN relationships(p) | type(r)] AS relationshipTypes,",
                        "       length(p) AS hops"))
                .param("from", "bo@allst.dev")
                .param("to", "du@allst.dev")
                .build();
    }

    private static NamedReadQuery recommend() {
        return NamedReadQuery.builder("recommend", "business", "按共同技能推荐还没有 KNOWS 的同事")
                .cypher(q(
                        "MATCH (me:Member {email: $email})-[mine:HAS_SKILL]->(s:Skill)<-[their:HAS_SKILL]-(other:Member)",
                        "WHERE other.email <> me.email",
                        "  AND NOT (me)-[:KNOWS]-(other)",
                        "WITH other, collect(DISTINCT s.name) AS sharedSkills, sum(mine.level + their.level) AS score",
                        "RETURN other.email AS email, other.name AS name, sharedSkills, score",
                        "ORDER BY score DESC, email ASC",
                        "LIMIT $limit"))
                .param("email", "du@allst.dev")
                .param("limit", 5)
                .build();
    }

    private static NamedReadQuery skillGap() {
        return NamedReadQuery.builder("skill-gap", "business", "项目要求的技能高于本人等级")
                .cypher(q(
                        "MATCH (m:Member {email: $email})-[:WORKS_ON]->(p:Project)-[req:REQUIRES]->(s:Skill)",
                        "OPTIONAL MATCH (m)-[mine:HAS_SKILL]->(s)",
                        "WITH p, s, req.level AS requiredLevel, coalesce(mine.level, 0) AS myLevel",
                        "WHERE myLevel < requiredLevel",
                        "RETURN p.code AS project, s.name AS skill, requiredLevel, myLevel",
                        "ORDER BY project, skill"))
                .param("email", "chen@allst.dev")
                .build();
    }

    private static NamedReadQuery projectRisk() {
        return NamedReadQuery.builder("project-risk", "business", "任务阻塞关系")
                .cypher(q(
                        "MATCH (t:Task)-[:OF_PROJECT]->(p:Project)",
                        "OPTIONAL MATCH (t)-[:BLOCKS]->(blocked:Task)",
                        "RETURN p.code AS project, t.code AS code, t.title AS title,",
                        "       t.status AS status, t.priority AS priority,",
                        "       [x IN collect(blocked.code) WHERE x IS NOT NULL] AS blocks",
                        "ORDER BY priority DESC, code ASC"))
                .build();
    }

    private static NamedReadQuery seekMemberEmail() {
        return NamedReadQuery.builder("seek-member-email", "tuning", "邮箱等值应走唯一约束索引")
                .cypher(q(
                        "MATCH (m:Member)",
                        "WHERE m.email = $email",
                        "RETURN m.email AS email, m.name AS name, m.title AS title"))
                .param("email", "chen@allst.dev")
                .build();
    }

    private static NamedReadQuery scanMemberCity() {
        return NamedReadQuery.builder("scan-member-city", "tuning", "城市没有索引，只能标签扫描后过滤")
                .cypher(q(
                        "MATCH (m:Member)",
                        "WHERE m.city = $city",
                        "RETURN m.email AS email, m.name AS name, m.city AS city"))
                .param("city", "上海")
                .build();
    }

    private static NamedReadQuery seekMemberName() {
        return NamedReadQuery.builder("seek-member-name", "tuning", "姓名等值走单属性索引")
                .cypher(q(
                        "MATCH (m:Member)",
                        "WHERE m.name = $name",
                        "RETURN m.email AS email, m.name AS name"))
                .param("name", "陈曦")
                .build();
    }

    private static NamedReadQuery hintMemberName() {
        return NamedReadQuery.builder("hint-member-name", "tuning", "USING INDEX 只在计划选错时使用")
                .cypher(q(
                        "MATCH (m:Member)",
                        "USING INDEX m:Member(name)",
                        "WHERE m.name = $name",
                        "RETURN m.email AS email, m.name AS name"))
                .param("name", "陈曦")
                .build();
    }

    private static NamedReadQuery seekTaskComposite() {
        return NamedReadQuery.builder("seek-task-composite", "tuning", "复合索引需要左侧属性等值")
                .cypher(q(
                        "MATCH (t:Task)",
                        "WHERE t.projectCode = $projectCode AND t.status = $status",
                        "RETURN t.code AS code, t.title AS title, t.priority AS priority"))
                .param("projectCode", "KG")
                .param("status", "DOING")
                .build();
    }

    private static NamedReadQuery scanTaskStatus() {
        return NamedReadQuery.builder("scan-task-status", "tuning", "只用复合索引的右侧属性，用不上该索引")
                .cypher(q(
                        "MATCH (t:Task)",
                        "WHERE t.status = $status",
                        "RETURN t.code AS code, t.projectCode AS projectCode, t.title AS title"))
                .param("status", "DOING")
                .build();
    }

    private static NamedReadQuery rangeDocumentUpdated() {
        return NamedReadQuery.builder("range-document-updated", "tuning", "时间范围走属性索引")
                .cypher(q(
                        "MATCH (d:Document)",
                        "WHERE d.updatedAt >= datetime($from)",
                        "RETURN d.code AS code, d.title AS title",
                        "ORDER BY d.updatedAt DESC"))
                .param("from", "2000-01-01T00:00:00Z")
                .build();
    }

    private static NamedReadQuery cartesianBad() {
        return NamedReadQuery.builder("cartesian-bad", "tuning", "两个不相连的 MATCH 会形成笛卡尔积")
                .cypher(q(
                        "MATCH (m:Member), (d:Department)",
                        "RETURN m.email AS email, d.code AS department",
                        "LIMIT 100"))
                .build();
    }

    private static NamedReadQuery cartesianGood() {
        return NamedReadQuery.builder("cartesian-good", "tuning", "用关系把成员和部门连起来")
                .cypher(q(
                        "MATCH (m:Member)-[:BELONGS_TO]->(d:Department)",
                        "RETURN m.email AS email, d.code AS department",
                        "LIMIT 100"))
                .build();
    }

    private static NamedReadQuery pathDepth2() {
        return NamedReadQuery.builder("path-depth-2", "tuning", "变长路径限制在 2 跳")
                .cypher(q(
                        "MATCH (d:Department {code: $code})",
                        "MATCH path = (child:Department)-[:CHILD_OF*1..2]->(d)",
                        "RETURN child.code AS child, length(path) AS depth"))
                .param("code", "RD")
                .build();
    }

    private static NamedReadQuery pathDepth6() {
        return NamedReadQuery.builder("path-depth-6", "tuning", "同样的路径放到 6 跳，计划里的上限会变大")
                .cypher(q(
                        "MATCH (d:Department {code: $code})",
                        "MATCH path = (child:Department)-[:CHILD_OF*1..6]->(d)",
                        "RETURN child.code AS child, length(path) AS depth"))
                .param("code", "RD")
                .build();
    }

    private static NamedReadQuery filterContains() {
        return NamedReadQuery.builder("filter-contains", "tuning", "CONTAINS 不能用 B 树索引")
                .cypher(q(
                        "MATCH (d:Document)",
                        "WHERE d.content CONTAINS $keyword OR d.title CONTAINS $keyword",
                        "RETURN d.code AS code, d.title AS title"))
                .param("keyword", "索引")
                .build();
    }

    private static NamedReadQuery fulltextDocument() {
        return NamedReadQuery.builder("fulltext-document", "tuning", "全文索引检索文档")
                .cypher(q(
                        "CALL db.index.fulltext.queryNodes('guide_document_text', $keyword) YIELD node, score",
                        "RETURN node.code AS code, node.title AS title, score",
                        "ORDER BY score DESC",
                        "LIMIT $limit"))
                .param("keyword", "索引")
                .param("limit", 10)
                .fulltextParam("keyword")
                .build();
    }

    private static NamedReadQuery semiApplySkill() {
        return NamedReadQuery.builder("semi-apply-skill", "tuning", "EXISTS 子查询只判断有没有技能")
                .cypher(q(
                        "MATCH (m:Member)",
                        "WHERE EXISTS {",
                        "  MATCH (m)-[:HAS_SKILL]->(:Skill {name: $skill})",
                        "}",
                        "RETURN m.email AS email, m.name AS name",
                        "ORDER BY email"))
                .param("skill", "Neo4j")
                .build();
    }

    private static String q(String... lines) {
        return String.join("\n", lines);
    }
}
