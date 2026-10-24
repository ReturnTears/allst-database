package com.allst.db.guide.service;

import com.allst.db.guide.client.CypherRowSet;
import com.allst.db.guide.client.Neo4jHttpClient;
import com.allst.db.guide.support.NotFoundException;
import com.allst.db.guide.support.Params;
import com.allst.db.guide.web.dto.BelongsToRequest;
import com.allst.db.guide.web.dto.KnowsRequest;
import com.allst.db.guide.web.dto.ManagesRequest;
import com.allst.db.guide.web.dto.WorksOnRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MERGE 关系、DELETE 关系和 FOREACH 打标签。说明见 docs/neo4j/02-statements.md。
 */
@Service
public class RelationGuideService {

    private static final String DATE = "\\d{4}-\\d{2}-\\d{2}";

    private static final String BELONGS = String.join("\n",
            "MATCH (m:Member {email: $email})",
            "OPTIONAL MATCH (m)-[old:BELONGS_TO]->(:Department)",
            "DELETE old",
            "WITH DISTINCT m",
            "MATCH (d:Department {code: $code})",
            "MERGE (m)-[r:BELONGS_TO]->(d)",
            "SET r.since = CASE WHEN $since IS NULL OR $since = '' THEN r.since ELSE date($since) END",
            "RETURN m.email AS email, d.code AS department, toString(r.since) AS since");

    private static final String WORKS = String.join("\n",
            "MATCH (m:Member {email: $email})",
            "MATCH (p:Project {code: $projectCode})",
            "MERGE (m)-[r:WORKS_ON]->(p)",
            "SET r.role = $role, r.hours = $hours",
            "RETURN m.email AS email, p.code AS project, r.role AS role, r.hours AS hours");

    private static final String KNOWS = String.join("\n",
            "MATCH (a:Member {email: $from})",
            "MATCH (b:Member {email: $to})",
            "MERGE (a)-[r:KNOWS]->(b)",
            "SET r.weight = $weight, r.note = $note,",
            "    r.since = CASE WHEN $since IS NULL OR $since = '' THEN r.since ELSE date($since) END",
            "RETURN a.email AS fromEmail, b.email AS toEmail, r.weight AS weight, r.note AS note");

    private static final String MANAGES = String.join("\n",
            "MATCH (manager:Member {email: $manager})",
            "MATCH (member:Member {email: $member})",
            "MERGE (manager)-[r:MANAGES]->(member)",
            "SET r.since = CASE WHEN $since IS NULL OR $since = '' THEN r.since ELSE date($since) END",
            "RETURN manager.email AS manager, member.email AS member, toString(r.since) AS since");

    private static final String DELETE_KNOWS = String.join("\n",
            "MATCH (a:Member {email: $from})-[r:KNOWS]->(b:Member {email: $to})",
            "WITH a, b, r",
            "DELETE r",
            "RETURN a.email AS fromEmail, b.email AS toEmail");

    private static final String TAGS = String.join("\n",
            "MATCH (d:Document {code: $code})",
            "FOREACH (tagName IN $tags |",
            "  MERGE (t:Tag {name: tagName})",
            "  MERGE (d)-[:TAGGED]->(t)",
            ")",
            "WITH d",
            "MATCH (d)-[:TAGGED]->(t:Tag)",
            "RETURN d.code AS code, collect(DISTINCT t.name) AS tags");

    private final Neo4jHttpClient client;

    public RelationGuideService(Neo4jHttpClient client) {
        this.client = client;
    }

    public Map<String, Object> belongsTo(BelongsToRequest request) {
        validateDate(request.getSince());
        return one(client.query(BELONGS, Params.of(
                "email", MemberGuideService.requireEmail(request.getEmail()),
                "code", requireCode(request.getDepartmentCode()),
                "since", blank(request.getSince()))), "成员或部门不存在");
    }

    public Map<String, Object> worksOn(WorksOnRequest request) {
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("role 不能为空");
        }
        if (request.getHours() < 0 || request.getHours() > 80) {
            throw new IllegalArgumentException("hours 需在 0 到 80 之间");
        }
        return one(client.query(WORKS, Params.of(
                "email", MemberGuideService.requireEmail(request.getEmail()),
                "projectCode", requireCode(request.getProjectCode()),
                "role", request.getRole().trim(),
                "hours", request.getHours())), "成员或项目不存在");
    }

    public Map<String, Object> knows(KnowsRequest request) {
        if (request.getWeight() < 1 || request.getWeight() > 10) {
            throw new IllegalArgumentException("weight 需在 1 到 10 之间");
        }
        if (request.getNote() == null || request.getNote().trim().isEmpty()) {
            throw new IllegalArgumentException("note 不能为空");
        }
        validateDate(request.getSince());
        String from = MemberGuideService.requireEmail(request.getFrom());
        String to = MemberGuideService.requireEmail(request.getTo());
        if (from.equals(to)) {
            throw new IllegalArgumentException("不能和自己建立 KNOWS");
        }
        return one(client.query(KNOWS, Params.of(
                "from", from,
                "to", to,
                "weight", request.getWeight(),
                "note", request.getNote().trim(),
                "since", blank(request.getSince()))), "成员不存在");
    }

    public Map<String, Object> manages(ManagesRequest request) {
        validateDate(request.getSince());
        String manager = MemberGuideService.requireEmail(request.getManager());
        String member = MemberGuideService.requireEmail(request.getMember());
        if (manager.equals(member)) {
            throw new IllegalArgumentException("不能管理自己");
        }
        return one(client.query(MANAGES, Params.of(
                "manager", manager,
                "member", member,
                "since", blank(request.getSince()))), "成员不存在");
    }

    public Map<String, Object> deleteKnows(String from, String to) {
        return one(client.query(DELETE_KNOWS, Params.of(
                "from", MemberGuideService.requireEmail(from),
                "to", MemberGuideService.requireEmail(to))), "KNOWS 关系不存在");
    }

    public Map<String, Object> tagDocument(String code, List<String> tags) {
        List<String> cleaned = cleanTags(tags);
        return one(client.query(TAGS, Params.of("code", requireCode(code), "tags", cleaned)), "文档不存在: " + code);
    }

    private static List<String> cleanTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("tags 不能为空");
        }
        if (tags.size() > 20) {
            throw new IllegalArgumentException("单次最多 20 个标签");
        }
        List<String> cleaned = new ArrayList<>();
        for (String tag : tags) {
            if (tag == null || tag.trim().isEmpty()) {
                throw new IllegalArgumentException("标签不能为空");
            }
            String value = tag.trim();
            if (value.length() > 30) {
                throw new IllegalArgumentException("标签最长 30 个字符");
            }
            cleaned.add(value);
        }
        return cleaned;
    }

    private static Map<String, Object> one(CypherRowSet result, String missingMessage) {
        if (result.getRows().isEmpty()) {
            throw new NotFoundException(missingMessage);
        }
        return result.getRows().get(0);
    }

    private static String requireCode(String code) {
        if (code == null || !code.trim().matches("[A-Za-z0-9][A-Za-z0-9_-]{0,39}")) {
            throw new IllegalArgumentException("编码格式不正确");
        }
        return code.trim();
    }

    private static void validateDate(String value) {
        if (value != null && !value.trim().isEmpty() && !value.trim().matches(DATE)) {
            throw new IllegalArgumentException("日期格式应为 yyyy-MM-dd");
        }
    }

    private static String blank(String value) {
        return value == null ? "" : value.trim();
    }
}
