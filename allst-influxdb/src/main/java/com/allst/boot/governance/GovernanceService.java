package com.allst.boot.governance;

import com.allst.boot.influx.client.InfluxGateway;
import com.allst.boot.influx.common.InfluxLimits;
import com.allst.boot.influx.common.InfluxResults;
import com.allst.boot.influx.common.TimeRanges;
import com.allst.boot.influx.sql.InfluxQl;
import com.allst.boot.influx.sql.SchemaStatements;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GovernanceService {

    private final InfluxGateway gateway;

    public GovernanceService(InfluxGateway gateway) {
        this.gateway = gateway;
    }

    public Map<String, Object> cardinality() {
        String sql = SchemaStatements.showSeriesCardinality();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("influxql", sql);
        body.put("rows", InfluxResults.rows(gateway.command(sql)));
        body.put("hint", "series 数量接近数据节点上限前就要拆 measurement 或收紧 tag，不要等写入开始失败");
        return body;
    }

    public Map<String, Object> tagValues(String key, String range) {
        String safeKey = InfluxQl.blankToNull(key);
        if (safeKey == null) {
            throw new IllegalArgumentException("key 不能为空");
        }
        String predicate = TimeRanges.parse(range == null || range.isBlank() ? "7d" : range,
                null, null, InfluxLimits.RAW_MAX_SPAN).predicate();
        String sql = SchemaStatements.showTagValues(safeKey, predicate);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("influxql", sql);
        body.put("rows", InfluxResults.rows(gateway.command(sql)));
        return body;
    }
}
