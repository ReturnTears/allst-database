package com.allst.boot.catalog;

import com.allst.boot.cassandra.client.CqlGateway;
import com.allst.boot.cassandra.common.ApiResponse;
import com.allst.boot.cassandra.common.Rows;
import com.allst.boot.cassandra.cql.Statements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cassandra")
public class CatalogController {

    private final CqlGateway gateway;

    public CatalogController(CqlGateway gateway) {
        this.gateway = gateway;
    }

    @GetMapping("/catalog")
    public ApiResponse<List<Map<String, String>>> catalog() {
        return ApiResponse.ok(entries());
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> local = Rows.first(gateway.query(Statements.selectLocal(), builder -> {
        }));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("up", true);
        if (local != null) {
            body.put("clusterName", local.get("cluster_name"));
            body.put("releaseVersion", local.get("release_version"));
            body.put("dataCenter", local.get("data_center"));
            body.put("rack", local.get("rack"));
            body.put("partitioner", local.get("partitioner"));
        }
        return ApiResponse.ok(body);
    }

    public static List<Map<String, String>> entries() {
        List<Map<String, String>> entries = new ArrayList<>();
        entries.add(entry("write", "基本写入", "/api/cassandra/readings", "docs/cassandra-scenarios/01-基本写入.md"));
        entries.add(entry("query", "基本查询", "/api/cassandra/readings", "docs/cassandra-scenarios/02-基本查询.md"));
        entries.add(entry("function", "函数与运算", "/api/cassandra/analytics", "docs/cassandra-scenarios/03-函数与运算.md"));
        entries.add(entry("continuous", "连续查询", "/api/cassandra/continuous", "docs/cassandra-scenarios/04-连续查询.md"));
        entries.add(entry("tuning", "性能调优", "/api/cassandra/tuning/profile", "docs/cassandra-scenarios/05-性能调优.md"));
        entries.add(entry("cluster", "集群安装配置", "deploy/cluster", "docs/cassandra-scenarios/06-集群安装配置.md"));
        entries.add(entry("backup", "数据备份与恢复", "/api/cassandra/backup", "docs/cassandra-scenarios/07-数据备份与恢复.md"));
        entries.add(entry("model", "数据模型规范", "/api/cassandra/schema/model", "docs/cassandra-scenarios/08-数据模型规范.md"));
        entries.add(entry("mutation", "轻量事务、TTL、计数器与集合", "/api/cassandra/mutations", "docs/cassandra-scenarios/09-轻量事务TTL计数器与集合.md"));
        entries.add(entry("index", "二级索引与物化视图", "/api/cassandra/indexes", "docs/cassandra-scenarios/10-二级索引与物化视图.md"));
        entries.add(entry("tombstone", "删除与墓碑", "/api/cassandra/governance/advice", "docs/cassandra-scenarios/11-删除与墓碑.md"));
        return entries;
    }

    private static Map<String, String> entry(String id, String name, String api, String doc) {
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("id", id);
        entry.put("name", name);
        entry.put("api", api);
        entry.put("doc", doc);
        return entry;
    }
}
