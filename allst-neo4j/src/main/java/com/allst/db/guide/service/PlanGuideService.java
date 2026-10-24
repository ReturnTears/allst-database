package com.allst.db.guide.service;

import com.allst.db.guide.catalog.NamedReadQuery;
import com.allst.db.guide.catalog.QueryParameters;
import com.allst.db.guide.client.CypherRowSet;
import com.allst.db.guide.client.Neo4jHttpClient;
import com.allst.db.guide.support.PlanFormatter;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EXPLAIN 只看计划，PROFILE 会真正执行只读语句。说明见 docs/neo4j/08-execution-plan.md。
 */
@Service
public class PlanGuideService {

    private final QueryGuideService queryGuideService;
    private final Neo4jHttpClient client;

    public PlanGuideService(QueryGuideService queryGuideService, Neo4jHttpClient client) {
        this.queryGuideService = queryGuideService;
        this.client = client;
    }

    public Map<String, Object> explain(String name, Map<String, String> raw) {
        return plan("EXPLAIN ", name, raw, true);
    }

    public Map<String, Object> profile(String name, Map<String, String> raw) {
        return plan("PROFILE ", name, raw, true);
    }

    public Map<String, Object> compare(String left, String right) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("left", compact(profile(left, Collections.emptyMap())));
        body.put("right", compact(profile(right, Collections.emptyMap())));
        return body;
    }

    private Map<String, Object> plan(String prefix, String name, Map<String, String> raw, boolean includeRawPlan) {
        NamedReadQuery query = queryGuideService.require(name);
        Map<String, Object> params = QueryParameters.merge(query, raw);
        QueryParameters.sanitizeFulltext(query, params);
        CypherRowSet result = client.query(prefix + query.getCypher(), params);
        List<Map<String, Object>> operators = PlanFormatter.operators(result.getPlan());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", query.getName());
        body.put("title", query.getTitle());
        body.put("cypher", prefix + query.getCypher());
        body.put("parameters", params);
        body.put("rows", result.getRows());
        body.put("elapsedMs", result.getElapsedMs());
        body.put("planText", PlanFormatter.planText(result.getPlan()));
        body.put("operators", operators);
        body.put("totalDbHits", PlanFormatter.totalDbHits(operators));
        if (includeRawPlan) {
            body.put("rawPlan", result.getPlan());
        }
        return body;
    }

    private static Map<String, Object> compact(Map<String, Object> plan) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", plan.get("name"));
        body.put("cypher", plan.get("cypher"));
        body.put("parameters", plan.get("parameters"));
        body.put("elapsedMs", plan.get("elapsedMs"));
        body.put("totalDbHits", plan.get("totalDbHits"));
        body.put("operators", plan.get("operators"));
        Object rows = plan.get("rows");
        body.put("rowCount", rows instanceof List ? ((List<?>) rows).size() : 0);
        return body;
    }
}
