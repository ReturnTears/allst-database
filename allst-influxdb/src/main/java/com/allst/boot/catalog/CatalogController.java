package com.allst.boot.catalog;

import com.allst.boot.influx.client.InfluxGateway;
import com.allst.boot.influx.common.ApiResponse;
import org.influxdb.dto.Pong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/influx")
public class CatalogController {

    private final InfluxGateway gateway;

    public CatalogController(InfluxGateway gateway) {
        this.gateway = gateway;
    }

    @GetMapping("/catalog")
    public ApiResponse<List<Map<String, String>>> catalog() {
        return ApiResponse.ok(entries());
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Pong pong = gateway.ping();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("up", true);
        body.put("version", pong.getVersion());
        body.put("pingMs", pong.getResponseTime());
        return ApiResponse.ok(body);
    }

    static List<Map<String, String>> entries() {
        List<Map<String, String>> entries = new ArrayList<>();
        entries.add(entry("write", "基本写入", "/api/influx/metrics", "docs/influx-scenarios/01-基本写入.md"));
        entries.add(entry("query", "基本查询", "/api/influx/metrics", "docs/influx-scenarios/02-基本查询.md"));
        entries.add(entry("function", "函数与运算", "/api/influx/analytics", "docs/influx-scenarios/03-函数与运算.md"));
        entries.add(entry("continuous-query", "连续查询", "/api/influx/continuous-queries", "docs/influx-scenarios/04-连续查询.md"));
        entries.add(entry("tuning", "性能调优", "/api/influx/tuning/profile", "docs/influx-scenarios/05-性能调优.md"));
        entries.add(entry("cluster", "集群安装配置", "deploy/cluster", "docs/influx-scenarios/06-集群安装配置.md"));
        entries.add(entry("backup", "数据备份与恢复", "/api/influx/backup", "docs/influx-scenarios/07-数据备份与恢复.md"));
        entries.add(entry("retention", "保留策略与降采样", "/api/influx/schema/init", "docs/influx-scenarios/08-保留策略与降采样.md"));
        entries.add(entry("model", "数据模型", "/api/influx/schema/fields", "docs/influx-scenarios/09-数据模型规范.md"));
        entries.add(entry("governance", "删除与基数治理", "/api/influx/governance/cardinality", "docs/influx-scenarios/10-删除与基数治理.md"));
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
