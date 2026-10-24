package com.allst.db.guide.seed;

import com.allst.db.guide.client.CypherRowSet;
import com.allst.db.guide.client.CypherStatement;
import com.allst.db.guide.client.Neo4jHttpClient;
import com.allst.db.guide.support.WriteStats;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 样例数据写入。说明见 docs/neo4j/02-statements.md 和 docs/neo4j/09-transactions-batch-path.md。
 */
@Service
public class GraphSeedService {

    private final Neo4jHttpClient client;

    public GraphSeedService(Neo4jHttpClient client) {
        this.client = client;
    }

    public Map<String, Object> seed() {
        List<CypherRowSet> results = client.commitAll(GraphSeedStatements.build());
        return WriteStats.summarize(results);
    }

    public Map<String, Object> purge() {
        List<CypherRowSet> results = client.commitAll(Collections.singletonList(
                new CypherStatement(GuideLabels.PURGE, Collections.emptyMap())));
        return WriteStats.summarize(results);
    }
}
