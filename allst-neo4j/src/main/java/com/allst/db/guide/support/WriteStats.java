package com.allst.db.guide.support;

import com.allst.db.guide.client.CypherRowSet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WriteStats {

    private WriteStats() {
    }

    public static Map<String, Object> summarize(List<CypherRowSet> results) {
        long nodesCreated = 0L;
        long nodesDeleted = 0L;
        long relationshipsCreated = 0L;
        long relationshipsDeleted = 0L;
        long propertiesSet = 0L;
        long labelsAdded = 0L;
        for (CypherRowSet result : results) {
            nodesCreated += Numbers.stat(result.getStats(), "nodes_created");
            nodesDeleted += Numbers.stat(result.getStats(), "nodes_deleted");
            relationshipsCreated += Numbers.stat(result.getStats(), "relationships_created");
            relationshipsDeleted += Numbers.stat(result.getStats(), "relationships_deleted");
            propertiesSet += Numbers.stat(result.getStats(), "properties_set");
            labelsAdded += Numbers.stat(result.getStats(), "labels_added");
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("statements", results.size());
        summary.put("nodesCreated", nodesCreated);
        summary.put("nodesDeleted", nodesDeleted);
        summary.put("relationshipsCreated", relationshipsCreated);
        summary.put("relationshipsDeleted", relationshipsDeleted);
        summary.put("propertiesSet", propertiesSet);
        summary.put("labelsAdded", labelsAdded);
        summary.put("elapsedMs", results.isEmpty() ? 0L : results.get(0).getElapsedMs());
        return summary;
    }
}
