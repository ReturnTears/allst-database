package com.allst.db.guide.seed;

import com.allst.db.guide.client.CypherStatement;
import com.allst.db.guide.support.Params;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 协作图谱样例。重复调用会先清掉规范示例标签，再整图重建。
 * 说明见 docs/neo4j/09-transactions-batch-path.md。
 */
public final class GraphSeedStatements {

    private GraphSeedStatements() {
    }

    public static List<CypherStatement> build() {
        List<CypherStatement> statements = new ArrayList<>();
        statements.add(new CypherStatement(GuideLabels.PURGE, Params.of()));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MERGE (d:Department {code: row.code})",
                "SET d.name = row.name, d.city = row.city"),
                Params.of("rows", departments())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MATCH (child:Department {code: row.child})",
                "MATCH (parent:Department {code: row.parent})",
                "MERGE (child)-[:CHILD_OF]->(parent)"),
                Params.of("rows", departmentLinks())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MERGE (m:Member {email: row.email})",
                "SET m.name = row.name, m.title = row.title, m.city = row.city,",
                "    m.active = true, m.hiredAt = date(row.hiredAt)",
                "WITH m, row",
                "MATCH (d:Department {code: row.department})",
                "MERGE (m)-[:BELONGS_TO]->(d)"),
                Params.of("rows", members())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MATCH (manager:Member {email: row.manager})",
                "MATCH (member:Member {email: row.member})",
                "MERGE (manager)-[r:MANAGES]->(member)",
                "SET r.since = date(row.since)"),
                Params.of("rows", manages())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MATCH (a:Member {email: row.from})",
                "MATCH (b:Member {email: row.to})",
                "MERGE (a)-[r:KNOWS]->(b)",
                "SET r.weight = row.weight, r.note = row.note, r.since = date(row.since)"),
                Params.of("rows", knows())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MERGE (p:Project {code: row.code})",
                "SET p.name = row.name, p.status = row.status, p.budget = row.budget"),
                Params.of("rows", projects())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MATCH (source:Project {code: row.from})",
                "MATCH (target:Project {code: row.to})",
                "MERGE (source)-[r:DEPENDS_ON]->(target)",
                "SET r.reason = row.reason"),
                Params.of("rows", dependencies())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MATCH (m:Member {email: row.email})",
                "MATCH (p:Project {code: row.project})",
                "MERGE (m)-[r:WORKS_ON]->(p)",
                "SET r.role = row.role, r.hours = row.hours"),
                Params.of("rows", worksOn())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MERGE (s:Skill {name: row.name})"),
                Params.of("rows", skills())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MATCH (m:Member {email: row.email})",
                "MATCH (s:Skill {name: row.skill})",
                "MERGE (m)-[r:HAS_SKILL]->(s)",
                "SET r.level = row.level"),
                Params.of("rows", hasSkill())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MATCH (p:Project {code: row.project})",
                "MATCH (s:Skill {name: row.skill})",
                "MERGE (p)-[r:REQUIRES]->(s)",
                "SET r.level = row.level"),
                Params.of("rows", requires())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MERGE (t:Task {code: row.code})",
                "SET t.title = row.title, t.status = row.status, t.priority = row.priority,",
                "    t.projectCode = row.project, t.description = row.description",
                "WITH t, row",
                "MATCH (p:Project {code: row.project})",
                "MERGE (t)-[:OF_PROJECT]->(p)"),
                Params.of("rows", tasks())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MATCH (m:Member {email: row.email})",
                "MATCH (t:Task {code: row.task})",
                "MERGE (m)-[r:ASSIGNED]->(t)",
                "SET r.assignedAt = date(row.assignedAt)"),
                Params.of("rows", assignments())));
        statements.add(new CypherStatement(q(
                "MATCH (blocker:Task {code: $from})",
                "MATCH (blocked:Task {code: $to})",
                "MERGE (blocker)-[:BLOCKS]->(blocked)"),
                Params.of("from", "T2", "to", "T3")));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MERGE (d:Document {code: row.code})",
                "SET d.title = row.title, d.summary = row.summary, d.content = row.content,",
                "    d.updatedAt = datetime()",
                "WITH d, row",
                "MATCH (author:Member {email: row.author})",
                "MATCH (p:Project {code: row.project})",
                "MERGE (author)-[:AUTHORED]->(d)",
                "MERGE (d)-[:ABOUT]->(p)"),
                Params.of("rows", documents())));
        statements.add(new CypherStatement(q(
                "UNWIND $rows AS row",
                "MATCH (d:Document {code: row.code})",
                "MERGE (t:Tag {name: row.tag})",
                "MERGE (d)-[:TAGGED]->(t)"),
                Params.of("rows", tags())));
        statements.add(new CypherStatement(q(
                "MERGE (n:TxProbe {key: 'guide'})",
                "SET n.marker = 'seed', n.runs = coalesce(n.runs, 0)"),
                Params.of()));
        return statements;
    }

    private static List<Map<String, Object>> departments() {
        return Arrays.asList(
                Params.of("code", "RD", "name", "研发中心", "city", "上海"),
                Params.of("code", "RD-BE", "name", "后端组", "city", "上海"),
                Params.of("code", "RD-FE", "name", "前端组", "city", "上海"),
                Params.of("code", "PD", "name", "产品中心", "city", "杭州"),
                Params.of("code", "QA", "name", "质量组", "city", "上海"));
    }

    private static List<Map<String, Object>> departmentLinks() {
        return Arrays.asList(
                Params.of("child", "RD-BE", "parent", "RD"),
                Params.of("child", "RD-FE", "parent", "RD"));
    }

    private static List<Map<String, Object>> members() {
        return Arrays.asList(
                member("anna@allst.dev", "安娜", "技术总监", "上海", "2018-03-01", "RD"),
                member("bo@allst.dev", "博文", "后端工程师", "上海", "2020-07-15", "RD-BE"),
                member("chen@allst.dev", "陈曦", "图数据库工程师", "上海", "2021-04-01", "RD-BE"),
                member("du@allst.dev", "杜晴", "前端工程师", "杭州", "2019-11-20", "RD-FE"),
                member("eva@allst.dev", "伊芙", "产品经理", "杭州", "2022-01-10", "PD"),
                member("fang@allst.dev", "方宁", "测试工程师", "上海", "2023-05-06", "RD-BE"));
    }

    private static Map<String, Object> member(String email, String name, String title, String city,
                                              String hiredAt, String department) {
        return Params.of("email", email, "name", name, "title", title, "city", city,
                "hiredAt", hiredAt, "department", department);
    }

    private static List<Map<String, Object>> manages() {
        return Arrays.asList(
                Params.of("manager", "anna@allst.dev", "member", "bo@allst.dev", "since", "2020-07-15"),
                Params.of("manager", "anna@allst.dev", "member", "du@allst.dev", "since", "2019-11-20"),
                Params.of("manager", "anna@allst.dev", "member", "eva@allst.dev", "since", "2022-01-10"));
    }

    private static List<Map<String, Object>> knows() {
        return Arrays.asList(
                Params.of("from", "bo@allst.dev", "to", "chen@allst.dev", "weight", 5,
                        "note", "一起排查过慢查询", "since", "2021-06-01"),
                Params.of("from", "chen@allst.dev", "to", "du@allst.dev", "weight", 2,
                        "note", "跨端联调知识图谱页面", "since", "2022-03-18"),
                Params.of("from", "eva@allst.dev", "to", "du@allst.dev", "weight", 4,
                        "note", "需求评审频繁协作", "since", "2022-02-01"),
                Params.of("from", "bo@allst.dev", "to", "fang@allst.dev", "weight", 3,
                        "note", "接口回归搭档", "since", "2023-05-20"));
    }

    private static List<Map<String, Object>> projects() {
        return Arrays.asList(
                Params.of("code", "KG", "name", "知识图谱平台", "status", "ACTIVE", "budget", 800000),
                Params.of("code", "REC", "name", "同事推荐", "status", "ACTIVE", "budget", 300000),
                Params.of("code", "PORTAL", "name", "数据门户", "status", "PLANNING", "budget", 500000));
    }

    private static List<Map<String, Object>> dependencies() {
        return Arrays.asList(
                Params.of("from", "REC", "to", "KG", "reason", "推荐结果要挂到图谱实体上"),
                Params.of("from", "PORTAL", "to", "KG", "reason", "门户展示图谱里的项目和文档"));
    }

    private static List<Map<String, Object>> worksOn() {
        return Arrays.asList(
                work("anna@allst.dev", "KG", "负责人", 8),
                work("bo@allst.dev", "KG", "开发", 40),
                work("chen@allst.dev", "KG", "开发", 40),
                work("du@allst.dev", "KG", "开发", 16),
                work("du@allst.dev", "REC", "开发", 24),
                work("eva@allst.dev", "REC", "负责人", 20),
                work("eva@allst.dev", "PORTAL", "负责人", 12),
                work("fang@allst.dev", "KG", "测试", 20));
    }

    private static Map<String, Object> work(String email, String project, String role, int hours) {
        return Params.of("email", email, "project", project, "role", role, "hours", hours);
    }

    private static List<Map<String, Object>> skills() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String name : Arrays.asList("Java", "SpringBoot", "Cypher", "Neo4j", "Vue", "产品分析")) {
            rows.add(Params.of("name", name));
        }
        return rows;
    }

    private static List<Map<String, Object>> hasSkill() {
        return Arrays.asList(
                skill("anna@allst.dev", "Java", 4),
                skill("anna@allst.dev", "SpringBoot", 4),
                skill("anna@allst.dev", "Neo4j", 3),
                skill("bo@allst.dev", "Java", 5),
                skill("bo@allst.dev", "SpringBoot", 5),
                skill("bo@allst.dev", "Cypher", 3),
                skill("chen@allst.dev", "Neo4j", 5),
                skill("chen@allst.dev", "Cypher", 5),
                skill("chen@allst.dev", "Java", 3),
                skill("du@allst.dev", "Vue", 5),
                skill("du@allst.dev", "Cypher", 2),
                skill("eva@allst.dev", "产品分析", 5),
                skill("fang@allst.dev", "Java", 3),
                skill("fang@allst.dev", "SpringBoot", 2));
    }

    private static Map<String, Object> skill(String email, String name, int level) {
        return Params.of("email", email, "skill", name, "level", level);
    }

    private static List<Map<String, Object>> requires() {
        return Arrays.asList(
                require("KG", "Neo4j", 4),
                require("KG", "Cypher", 3),
                require("KG", "Java", 3),
                require("KG", "SpringBoot", 3),
                require("REC", "Cypher", 3),
                require("REC", "Java", 3),
                require("PORTAL", "Vue", 3),
                require("PORTAL", "产品分析", 3));
    }

    private static Map<String, Object> require(String project, String skill, int level) {
        return Params.of("project", project, "skill", skill, "level", level);
    }

    private static List<Map<String, Object>> tasks() {
        return Arrays.asList(
                task("T1", "设计成员模型", "DONE", 1, "KG", "确定 Member 与 Department 的关系"),
                task("T2", "补齐全文索引", "DOING", 2, "KG", "为文档和任务建立全文索引"),
                task("T3", "接入执行计划页面", "TODO", 3, "KG", "用 EXPLAIN 和 PROFILE 看索引是否命中"),
                task("T4", "推荐接口联调", "TODO", 2, "REC", "联调共同技能推荐接口"));
    }

    private static Map<String, Object> task(String code, String title, String status, int priority,
                                            String project, String description) {
        return Params.of("code", code, "title", title, "status", status, "priority", priority,
                "project", project, "description", description);
    }

    private static List<Map<String, Object>> assignments() {
        return Arrays.asList(
                Params.of("email", "bo@allst.dev", "task", "T1", "assignedAt", "2024-01-08"),
                Params.of("email", "chen@allst.dev", "task", "T2", "assignedAt", "2024-02-12"),
                Params.of("email", "fang@allst.dev", "task", "T3", "assignedAt", "2024-03-01"),
                Params.of("email", "du@allst.dev", "task", "T4", "assignedAt", "2024-03-15"));
    }

    private static List<Map<String, Object>> documents() {
        return Arrays.asList(
                Params.of("code", "DOC1", "title", "查询调优备忘",
                        "summary", "索引、参数化和 PROFILE",
                        "content", "成员按邮箱查询应走唯一约束产生的索引。城市条件目前没有索引，执行计划里会看到标签扫描和过滤。上线前用 PROFILE 对比改写。",
                        "author", "chen@allst.dev", "project", "KG"),
                Params.of("code", "DOC2", "title", "全文检索说明",
                        "summary", "文档标题与正文的检索",
                        "content", "文档标题、摘要和正文进入全文索引。业务检索不要用 CONTAINS 扫全表，调用 db.index.fulltext.queryNodes。",
                        "author", "chen@allst.dev", "project", "KG"),
                Params.of("code", "DOC3", "title", "推荐规则",
                        "summary", "共同技能与最短协作路径",
                        "content", "推荐同事时先找共同技能，再排除已经存在 KNOWS 关系的人，并用最短路径解释协作距离。",
                        "author", "eva@allst.dev", "project", "REC"));
    }

    private static List<Map<String, Object>> tags() {
        return Arrays.asList(
                Params.of("code", "DOC1", "tag", "索引"),
                Params.of("code", "DOC1", "tag", "调优"),
                Params.of("code", "DOC2", "tag", "检索"),
                Params.of("code", "DOC3", "tag", "推荐"));
    }

    private static String q(String... lines) {
        return String.join("\n", lines);
    }
}
