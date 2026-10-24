package com.allst.db.guide.service;

import com.allst.db.guide.client.CypherRowSet;
import com.allst.db.guide.client.CypherStatement;
import com.allst.db.guide.client.Neo4jHttpClient;
import com.allst.db.guide.support.NotFoundException;
import com.allst.db.guide.support.Numbers;
import com.allst.db.guide.support.Params;
import com.allst.db.guide.web.dto.MemberRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CREATE、MERGE、SET、REMOVE、DELETE、DETACH DELETE 和 UNWIND。
 * 说明见 docs/neo4j/02-statements.md。
 */
@Service
public class MemberGuideService {

    private static final String EMAIL = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final String DATE = "\\d{4}-\\d{2}-\\d{2}";

    private static final String COUNT = String.join("\n",
            "MATCH (m:Member)",
            "WHERE ($city IS NULL OR m.city = $city)",
            "  AND ($active IS NULL OR m.active = $active)",
            "RETURN count(m) AS total");

    private static final String PAGE = String.join("\n",
            "MATCH (m:Member)",
            "WHERE ($city IS NULL OR m.city = $city)",
            "  AND ($active IS NULL OR m.active = $active)",
            "RETURN m.email AS email, m.name AS name, m.title AS title, m.city AS city,",
            "       m.active AS active, toString(m.hiredAt) AS hiredAt",
            "ORDER BY m.email",
            "SKIP $skip LIMIT $limit");

    private static final String GET = String.join("\n",
            "MATCH (m:Member {email: $email})",
            "OPTIONAL MATCH (m)-[:BELONGS_TO]->(d:Department)",
            "OPTIONAL MATCH (m)-[hs:HAS_SKILL]->(s:Skill)",
            "RETURN m.email AS email, m.name AS name, m.title AS title, m.city AS city,",
            "       m.active AS active, toString(m.hiredAt) AS hiredAt, m.remark AS remark,",
            "       [x IN collect(DISTINCT d.code) WHERE x IS NOT NULL] AS departmentCodes,",
            "       [x IN collect(DISTINCT CASE WHEN s IS NULL THEN null ELSE s.name + ':' + toString(hs.level) END)",
            "         WHERE x IS NOT NULL] AS skills");

    private static final String CREATE = String.join("\n",
            "CREATE (m:Member {email: $email, name: $name, active: true, createdAt: datetime()})",
            "SET m.title = $title,",
            "    m.city = $city,",
            "    m.hiredAt = CASE WHEN $hiredAt IS NULL OR $hiredAt = '' THEN null ELSE date($hiredAt) END",
            "RETURN m.email AS email, m.name AS name, m.title AS title, m.city AS city, m.active AS active,",
            "       toString(m.hiredAt) AS hiredAt");

    private static final String BATCH = String.join("\n",
            "UNWIND $rows AS row",
            "MERGE (m:Member {email: row.email})",
            "SET m.name = row.name,",
            "    m.title = row.title,",
            "    m.city = row.city,",
            "    m.active = coalesce(row.active, true),",
            "    m.hiredAt = CASE WHEN row.hiredAt IS NULL OR row.hiredAt = '' THEN m.hiredAt ELSE date(row.hiredAt) END,",
            "    m.updatedAt = datetime()",
            "RETURN count(m) AS affected");

    private static final String UPDATE = String.join("\n",
            "MATCH (m:Member {email: $email})",
            "SET m.name = coalesce($name, m.name),",
            "    m.title = coalesce($title, m.title),",
            "    m.city = coalesce($city, m.city),",
            "    m.active = coalesce($active, m.active),",
            "    m.remark = coalesce($remark, m.remark),",
            "    m.updatedAt = datetime()",
            "RETURN m.email AS email, m.name AS name, m.title AS title, m.city AS city,",
            "       m.active AS active, m.remark AS remark");

    private static final String DEACTIVATE = String.join("\n",
            "MATCH (m:Member {email: $email})",
            "SET m.active = false, m.updatedAt = datetime()",
            "RETURN m.email AS email, m.active AS active");

    private static final String REMOVE_REMARK = String.join("\n",
            "MATCH (m:Member {email: $email})",
            "REMOVE m.remark",
            "SET m.updatedAt = datetime()",
            "RETURN m.email AS email, m.remark AS remark");

    private static final String DETACH_DELETE = "MATCH (m:Member {email: $email}) DETACH DELETE m";

    private final Neo4jHttpClient client;

    public MemberGuideService(Neo4jHttpClient client) {
        this.client = client;
    }

    public Map<String, Object> list(String city, String active, int page, int size) {
        Boolean activeFlag = parseActive(active);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int skip = safePage * safeSize;
        Map<String, Object> filter = Params.of("city", blankToNull(city), "active", activeFlag);
        Map<String, Object> pageParams = new LinkedHashMap<>(filter);
        pageParams.put("skip", skip);
        pageParams.put("limit", safeSize);
        List<CypherRowSet> results = client.commitAll(Arrays.asList(
                new CypherStatement(COUNT, filter),
                new CypherStatement(PAGE, pageParams)));
        long total = results.get(0).getRows().isEmpty() ? 0L : Numbers.asLong(results.get(0).getRows().get(0).get("total"));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("total", total);
        body.put("page", safePage);
        body.put("size", safeSize);
        body.put("items", results.get(1).getRows());
        return body;
    }

    public Map<String, Object> get(String email) {
        return one(client.query(GET, Params.of("email", requireEmail(email))), "成员不存在: " + email);
    }

    public Map<String, Object> create(MemberRequest request) {
        requireText(request.getName(), "name");
        String email = requireEmail(request.getEmail());
        validateDate(request.getHiredAt());
        return one(client.query(CREATE, Params.of(
                "email", email,
                "name", request.getName().trim(),
                "title", blankToNull(request.getTitle()),
                "city", blankToNull(request.getCity()),
                "hiredAt", blankToNull(request.getHiredAt()))), "创建成员失败");
    }

    public Map<String, Object> batch(List<MemberRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("批量成员不能为空");
        }
        if (requests.size() > 200) {
            throw new IllegalArgumentException("单次最多写入 200 个成员");
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MemberRequest request : requests) {
            requireText(request.getName(), "name");
            validateDate(request.getHiredAt());
            rows.add(Params.of(
                    "email", requireEmail(request.getEmail()),
                    "name", request.getName().trim(),
                    "title", blankToNull(request.getTitle()),
                    "city", blankToNull(request.getCity()),
                    "hiredAt", blankToNull(request.getHiredAt()),
                    "active", request.getActive()));
        }
        return one(client.query(BATCH, Params.of("rows", rows)), "批量写入失败");
    }

    public Map<String, Object> update(String email, MemberRequest request) {
        validateDate(request.getHiredAt());
        if (request.getRemark() != null && request.getRemark().length() > 200) {
            throw new IllegalArgumentException("remark 最长 200 个字符");
        }
        return one(client.query(UPDATE, Params.of(
                "email", requireEmail(email),
                "name", blankToNull(request.getName()),
                "title", blankToNull(request.getTitle()),
                "city", blankToNull(request.getCity()),
                "active", request.getActive(),
                "remark", blankToNull(request.getRemark()))), "成员不存在: " + email);
    }

    public Map<String, Object> deactivate(String email) {
        return one(client.query(DEACTIVATE, Params.of("email", requireEmail(email))), "成员不存在: " + email);
    }

    public Map<String, Object> removeRemark(String email) {
        return one(client.query(REMOVE_REMARK, Params.of("email", requireEmail(email))), "成员不存在: " + email);
    }

    public Map<String, Object> detachDelete(String email) {
        String safeEmail = requireEmail(email);
        CypherRowSet result = client.query(DETACH_DELETE, Params.of("email", safeEmail));
        if (Numbers.stat(result.getStats(), "nodes_deleted") < 1) {
            throw new NotFoundException("成员不存在: " + safeEmail);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", safeEmail);
        body.put("nodesDeleted", Numbers.stat(result.getStats(), "nodes_deleted"));
        body.put("relationshipsDeleted", Numbers.stat(result.getStats(), "relationships_deleted"));
        return body;
    }

    private static Map<String, Object> one(CypherRowSet result, String missingMessage) {
        if (result.getRows().isEmpty()) {
            throw new NotFoundException(missingMessage);
        }
        return result.getRows().get(0);
    }

    private static Boolean parseActive(String active) {
        if (active == null || active.trim().isEmpty()) {
            return null;
        }
        String text = active.trim();
        if (!"true".equals(text) && !"false".equals(text)) {
            throw new IllegalArgumentException("active 只能是 true 或 false");
        }
        return Boolean.valueOf(text);
    }

    static String requireEmail(String email) {
        if (email == null || !email.trim().matches(EMAIL)) {
            throw new IllegalArgumentException("email 格式不正确");
        }
        return email.trim();
    }

    private static void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " 不能为空");
        }
        if (value.trim().length() > 40) {
            throw new IllegalArgumentException(field + " 最长 40 个字符");
        }
    }

    private static void validateDate(String value) {
        if (value != null && !value.trim().isEmpty() && !value.trim().matches(DATE)) {
            throw new IllegalArgumentException("日期格式应为 yyyy-MM-dd");
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
