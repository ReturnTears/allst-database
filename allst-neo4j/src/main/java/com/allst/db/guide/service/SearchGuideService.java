package com.allst.db.guide.service;

import com.allst.db.guide.client.CypherRowSet;
import com.allst.db.guide.client.Neo4jHttpClient;
import com.allst.db.guide.support.LuceneQuerySanitizer;
import com.allst.db.guide.support.Params;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 全文索引查询。说明见 docs/neo4j/05-fulltext-index.md。
 */
@Service
public class SearchGuideService {

    private static final String DOCUMENTS = String.join("\n",
            "CALL db.index.fulltext.queryNodes('guide_document_text', $q) YIELD node, score",
            "RETURN node.code AS code, node.title AS title, node.summary AS summary, score",
            "ORDER BY score DESC",
            "LIMIT $limit");

    private static final String KNOWS = String.join("\n",
            "CALL db.index.fulltext.queryRelationships('guide_knows_note', $q) YIELD relationship, score",
            "RETURN startNode(relationship).email AS fromEmail,",
            "       endNode(relationship).email AS toEmail,",
            "       relationship.note AS note,",
            "       score",
            "ORDER BY score DESC",
            "LIMIT $limit");

    private final Neo4jHttpClient client;

    public SearchGuideService(Neo4jHttpClient client) {
        this.client = client;
    }

    public List<Map<String, Object>> documents(String keyword, Integer limit) {
        return search(DOCUMENTS, keyword, limit);
    }

    public List<Map<String, Object>> knows(String keyword, Integer limit) {
        return search(KNOWS, keyword, limit);
    }

    private List<Map<String, Object>> search(String cypher, String keyword, Integer limit) {
        CypherRowSet rows = client.query(cypher, Params.of(
                "q", LuceneQuerySanitizer.escape(keyword),
                "limit", clamp(limit)));
        return rows.getRows();
    }

    private static int clamp(Integer limit) {
        if (limit == null) {
            return 10;
        }
        return Math.min(Math.max(limit, 1), 50);
    }
}
