package com.allst.db.guide.service;

import com.allst.db.guide.client.CypherRowSet;
import com.allst.db.guide.client.Neo4jHttpClient;
import com.allst.db.guide.client.Neo4jTransaction;
import com.allst.db.guide.support.Params;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 显式提交和回滚。Spring 的 @Transactional 不会管理这条 HTTP 连接。
 * 说明见 docs/neo4j/09-transactions-batch-path.md 和 docs/neo4j/10-spring-integration.md。
 */
@Service
public class TransactionGuideService {

    private static final String ENSURE = String.join("\n",
            "MERGE (n:TxProbe {key: 'guide'})",
            "ON CREATE SET n.marker = 'seed', n.runs = 0",
            "RETURN n.marker AS marker, n.runs AS runs");

    private static final String READ = "MATCH (n:TxProbe {key: 'guide'}) RETURN n.marker AS marker, n.runs AS runs";

    private static final String WRITE = String.join("\n",
            "MATCH (n:TxProbe {key: 'guide'})",
            "SET n.marker = $marker, n.runs = coalesce(n.runs, 0) + 1, n.updatedAt = datetime()",
            "RETURN n.marker AS marker, n.runs AS runs");

    private final Neo4jHttpClient client;

    public TransactionGuideService(Neo4jHttpClient client) {
        this.client = client;
    }

    public Map<String, Object> commitDemo(String marker) {
        String safeMarker = marker == null || marker.trim().isEmpty() ? "committed" : marker.trim();
        if (safeMarker.length() > 40) {
            throw new IllegalArgumentException("marker 最长 40 个字符");
        }
        client.query(ENSURE, Collections.emptyMap());
        try (Neo4jTransaction tx = client.begin()) {
            CypherRowSet written = tx.run(WRITE, Params.of("marker", safeMarker));
            CypherRowSet read = tx.run(READ, Collections.emptyMap());
            tx.commit();
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("committed", true);
            body.put("written", first(written));
            body.put("readInSameTransaction", first(read));
            return body;
        }
    }

    public Map<String, Object> rollbackDemo() {
        client.query(ENSURE, Collections.emptyMap());
        Map<String, Object> before = first(client.query(READ, Collections.emptyMap()));
        try (Neo4jTransaction tx = client.begin()) {
            tx.run(WRITE, Params.of("marker", "rollback-should-not-persist"));
        }
        Map<String, Object> after = first(client.query(READ, Collections.emptyMap()));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("rolledBack", true);
        body.put("before", before);
        body.put("after", after);
        body.put("unchanged", before.equals(after));
        return body;
    }

    private static Map<String, Object> first(CypherRowSet result) {
        if (result.getRows().isEmpty()) {
            return Collections.emptyMap();
        }
        return result.getRows().get(0);
    }
}
