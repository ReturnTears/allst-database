package com.allst.db.guide.schema;

import com.allst.db.guide.client.CypherRowSet;
import com.allst.db.guide.client.Neo4jException;
import com.allst.db.guide.client.Neo4jHttpClient;
import com.allst.db.guide.support.Params;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 索引、全文索引和约束的幂等初始化和删除。说明见 docs/neo4j/04-indexes.md、05-fulltext-index.md、06-constraints.md。
 */
@Service
public class GraphSchemaService {

    private static final String SERVER = "CALL dbms.components() YIELD name, versions, edition "
            + "WHERE name = 'Neo4j Kernel' RETURN versions[0] AS version, edition AS edition";

    private final Neo4jHttpClient client;

    public GraphSchemaService(Neo4jHttpClient client) {
        this.client = client;
    }

    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reachable", true);
        body.putAll(serverInfo());
        return body;
    }

    public Map<String, Object> init() {
        Map<String, Object> server = serverInfo();
        int major = majorVersion(server.get("version"));
        if (major > 0 && major < 4) {
            throw new IllegalArgumentException("规范示例需要 Neo4j 4.3 或更高版本，当前是 " + server.get("version"));
        }
        return apply(SchemaScripts.initCommands(major), server);
    }

    public Map<String, Object> drop() {
        Map<String, Object> server = serverInfo();
        int major = majorVersion(server.get("version"));
        if (major > 0 && major < 4) {
            throw new IllegalArgumentException("规范示例需要 Neo4j 4.3 或更高版本，当前是 " + server.get("version"));
        }
        return apply(SchemaScripts.dropCommands(), server);
    }

    public List<Map<String, Object>> indexes() {
        return client.query("SHOW INDEXES", Collections.emptyMap()).getRows();
    }

    public List<Map<String, Object>> constraints() {
        return client.query("SHOW CONSTRAINTS", Collections.emptyMap()).getRows();
    }

    public static int majorVersion(Object version) {
        if (version == null) {
            return 0;
        }
        String text = String.valueOf(version);
        int dot = text.indexOf('.');
        String major = dot < 0 ? text : text.substring(0, dot);
        try {
            return Integer.parseInt(major);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private Map<String, Object> apply(List<SchemaCommand> commands, Map<String, Object> server) {
        List<String> applied = new ArrayList<>();
        List<Map<String, Object>> skipped = new ArrayList<>();
        for (SchemaCommand command : commands) {
            try {
                client.query(command.getCypher(), Collections.emptyMap());
                applied.add(command.getName());
            } catch (Neo4jException ex) {
                if (alreadyExists(ex)) {
                    applied.add(command.getName());
                } else if (!command.isRequired()) {
                    skipped.add(Params.of("name", command.getName(), "reason", ex.getMessage()));
                } else {
                    throw ex;
                }
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("version", server.get("version"));
        body.put("edition", server.get("edition"));
        body.put("applied", applied);
        body.put("skipped", skipped);
        body.put("indexes", indexes());
        body.put("constraints", constraints());
        return body;
    }

    private Map<String, Object> serverInfo() {
        CypherRowSet rows = client.query(SERVER, Collections.emptyMap());
        if (rows.getRows().isEmpty()) {
            return Params.of("version", "unknown", "edition", "unknown");
        }
        return rows.getRows().get(0);
    }

    private static boolean alreadyExists(Neo4jException ex) {
        String code = ex.getCode() == null ? "" : ex.getCode();
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        return code.contains("EquivalentSchemaRuleAlreadyExists")
                || message.contains("already exists")
                || message.contains("equivalent");
    }
}
