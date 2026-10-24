package com.allst.db.guide.service;

import com.allst.db.guide.catalog.CypherCatalog;
import com.allst.db.guide.catalog.NamedReadQuery;
import com.allst.db.guide.catalog.QueryParameters;
import com.allst.db.guide.client.CypherRowSet;
import com.allst.db.guide.client.Neo4jHttpClient;
import com.allst.db.guide.support.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 按目录执行只读 Cypher。说明见 docs/neo4j/01-basic-syntax.md 和 docs/neo4j/03-functions.md。
 */
@Service
public class QueryGuideService {

    private final CypherCatalog catalog;
    private final Neo4jHttpClient client;

    public QueryGuideService(CypherCatalog catalog, Neo4jHttpClient client) {
        this.catalog = catalog;
        this.client = client;
    }

    public List<Map<String, Object>> list(String category) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (NamedReadQuery query : catalog.all()) {
            if (category != null && !category.isEmpty() && !category.equals(query.getCategory())) {
                continue;
            }
            items.add(describe(query));
        }
        return items;
    }

    public Map<String, Object> run(String name, Map<String, String> raw) {
        NamedReadQuery query = require(name);
        Map<String, Object> params = QueryParameters.merge(query, raw);
        QueryParameters.sanitizeFulltext(query, params);
        CypherRowSet result = client.query(query.getCypher(), params);
        Map<String, Object> body = describe(query);
        body.put("parameters", params);
        body.put("columns", result.getColumns());
        body.put("rows", result.getRows());
        body.put("elapsedMs", result.getElapsedMs());
        return body;
    }

    NamedReadQuery require(String name) {
        return catalog.find(name).orElseThrow(() -> new NotFoundException("没有这个查询: " + name));
    }

    private static Map<String, Object> describe(NamedReadQuery query) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", query.getName());
        item.put("category", query.getCategory());
        item.put("title", query.getTitle());
        item.put("cypher", query.getCypher());
        item.put("defaults", query.getDefaultParams());
        item.put("fulltextParam", query.getFulltextParam());
        return item;
    }
}
