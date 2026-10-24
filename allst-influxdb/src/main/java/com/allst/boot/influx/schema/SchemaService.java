package com.allst.boot.influx.schema;

import com.allst.boot.cq.ContinuousQueryService;
import com.allst.boot.influx.client.InfluxGateway;
import com.allst.boot.influx.common.InfluxAccessException;
import com.allst.boot.influx.common.InfluxResults;
import com.allst.boot.influx.config.InfluxProperties;
import com.allst.boot.influx.sql.SchemaStatements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SchemaService {

    private final InfluxGateway gateway;
    private final InfluxProperties properties;
    private final ContinuousQueryService continuousQueryService;

    public SchemaService(InfluxGateway gateway, InfluxProperties properties,
                         ContinuousQueryService continuousQueryService) {
        this.gateway = gateway;
        this.properties = properties;
        this.continuousQueryService = continuousQueryService;
    }

    public List<SchemaStep> init() {
        List<SchemaStep> steps = new ArrayList<>();
        String database = properties.getDatabase();
        if (gateway.databaseExists(database)) {
            steps.add(new SchemaStep("database", "skipped", database + " 已存在"));
        } else {
            gateway.createDatabase(database);
            steps.add(new SchemaStep("database", "applied", SchemaStatements.createDatabase(database)));
        }
        Set<String> policies = new HashSet<>(InfluxResults.column(
                gateway.command(SchemaStatements.showRetentionPolicies(database)), "name"));
        List<InfluxSchema.RetentionSpec> ordered = new ArrayList<>();
        for (InfluxSchema.RetentionSpec spec : InfluxSchema.retentions()) {
            if (!spec.isDefaultPolicy()) {
                ordered.add(spec);
            }
        }
        for (InfluxSchema.RetentionSpec spec : InfluxSchema.retentions()) {
            if (spec.isDefaultPolicy()) {
                ordered.add(spec);
            }
        }
        for (InfluxSchema.RetentionSpec spec : ordered) {
            boolean exists = policies.contains(spec.getName());
            String sql = exists
                    ? SchemaStatements.alterRetention(database, spec, properties.getReplicationFactor())
                    : SchemaStatements.createRetention(database, spec, properties.getReplicationFactor());
            gateway.command(sql);
            steps.add(new SchemaStep(spec.getName(), exists ? "altered" : "applied", sql));
        }
        try {
            String dropAutogen = SchemaStatements.dropRetention(database, "autogen");
            gateway.command(dropAutogen);
            steps.add(new SchemaStep("autogen", "applied", "已删除自动创建的 autogen"));
        } catch (InfluxAccessException ex) {
            steps.add(new SchemaStep("autogen", "warning", ex.getMessage()));
        }
        steps.addAll(continuousQueryService.ensure());
        return steps;
    }

    public List<java.util.Map<String, Object>> retentionPolicies() {
        return InfluxResults.rows(gateway.command(SchemaStatements.showRetentionPolicies(properties.getDatabase())));
    }

    public List<java.util.Map<String, Object>> fieldKeys() {
        return InfluxResults.rows(gateway.command(SchemaStatements.showFieldKeys()));
    }
}
