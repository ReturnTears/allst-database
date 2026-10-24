package com.allst.boot.cassandra.schema;

import com.allst.boot.cassandra.client.CqlGateway;
import com.allst.boot.cassandra.common.CassandraAccessException;
import com.allst.boot.cassandra.common.Rows;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.allst.boot.cassandra.cql.Statements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SchemaService {

    private final CqlGateway gateway;
    private final CassandraProperties properties;

    public SchemaService(CqlGateway gateway, CassandraProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
    }

    public List<Map<String, Object>> init() {
        List<String> statements = statements();
        List<Map<String, Object>> steps = new ArrayList<>();
        for (int i = 0; i < statements.size(); i++) {
            String cql = statements.get(i);
            try {
                gateway.executeDdl(cql);
            } catch (CassandraAccessException ex) {
                throw new CassandraAccessException(ex.isUnavailable(),
                        "第 " + (i + 1) + " 步失败（前面的步骤已经执行，可以重试）: " + head(cql) + " -> " + ex.getMessage(),
                        ex);
            }
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("step", i + 1);
            step.put("cql", head(cql));
            step.put("applied", true);
            steps.add(step);
        }
        return steps;
    }

    public Map<String, Object> replication() {
        CassandraProperties.Replication replication = properties.getReplication();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("create", Statements.createKeyspace(properties.getKeyspace(), replication.getStrategy(),
                replication.getFactor(), replication.getDatacenters()));
        body.put("alter", Statements.alterKeyspace(properties.getKeyspace(), replication.getStrategy(),
                replication.getFactor(), replication.getDatacenters()));
        body.put("note", "IF NOT EXISTS 不会修改已经存在的 keyspace。改副本数要用 alter，然后按节点跑 nodetool repair。单节点保持 SimpleStrategy 和 RF 1。");
        return body;
    }

    public Map<String, Object> tables() {
        return schemaRows("tables", Statements.selectTables());
    }

    public Map<String, Object> columns() {
        return schemaRows("columns", Statements.selectColumns());
    }

    private Map<String, Object> schemaRows(String field, String cql) {
        List<Map<String, Object>> rows = Rows.list(gateway.query(cql,
                builder -> builder.setString("keyspace_name", properties.getKeyspace())), 500);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("keyspace", properties.getKeyspace());
        body.put(field, rows);
        body.put("count", rows.size());
        return body;
    }

    private List<String> statements() {
        CassandraProperties.Replication replication = properties.getReplication();
        return Statements.all(properties.getKeyspace(), replication.getStrategy(), replication.getFactor(),
                replication.getDatacenters());
    }

    private static String head(String cql) {
        String oneLine = cql.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= 96 ? oneLine : oneLine.substring(0, 96) + "...";
    }
}
