package com.allst.boot.influx;

import com.allst.boot.influx.common.InfluxAccessException;
import com.allst.boot.influx.common.InfluxResults;
import com.allst.boot.influx.common.TimeRanges;
import com.allst.boot.influx.schema.InfluxSchema;
import com.allst.boot.influx.sql.AnalyticsStatements;
import com.allst.boot.influx.sql.InfluxQl;
import com.allst.boot.influx.sql.MetricStatements;
import com.allst.boot.influx.sql.SchemaStatements;
import com.allst.boot.metric.MetricRules;
import com.allst.boot.metric.MetricWriteCommand;
import com.allst.boot.metric.MetricWriteService;
import com.allst.boot.ops.BackupPaths;
import com.allst.boot.ops.LogicalBackup;
import com.allst.boot.ops.PortableBackupCommands;
import org.influxdb.dto.QueryResult;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InfluxSpecTest {

    @Test
    public void timestampWithoutZoneUsesShanghai() {
        Instant instant = TimeRanges.parseInstant("2026-10-24 08:00:00");
        assertEquals("2026-10-24T00:00:00.000Z", InfluxQl.format(instant));
    }

    @Test
    public void rangeIsHalfOpenAndBounded() {
        var range = TimeRanges.parse(null, "2026-10-24 08:00:00", "2026-10-24 09:00:00", Duration.ofDays(7));
        assertEquals("time >= '2026-10-24T00:00:00.000Z' AND time < '2026-10-24T01:00:00.000Z'", range.predicate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectMixedTimeArguments() {
        TimeRanges.parse("1h", "2026-10-24 08:00:00", null, Duration.ofDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectInjectionInDuration() {
        InfluxQl.parseDuration("1h; DROP DATABASE allst_iot");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectTagQuote() {
        MetricRules.requiredTag("dev' OR '1'='1", "deviceId");
    }

    @Test
    public void aggregateRequiresMetricTypeAndWhitelistedFunction() {
        String predicate = "time >= '2026-10-24T00:00:00.000Z' AND time < '2026-10-24T01:00:00.000Z'";
        String sql = AnalyticsStatements.aggregate("rp_raw_7d", "mean", null, predicate,
                "dev-001", "cn-east", "temperature", "1m", "none", 100);
        assertTrue(sql.contains("SELECT mean(\"value\") AS \"value\""));
        assertTrue(sql.contains("\"metric_type\" = 'temperature'"));
        assertTrue(sql.contains("FILL(none) LIMIT 100 SLIMIT 20"));
        assertFalse(sql.toLowerCase().contains("drop"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectUnknownFunction() {
        AnalyticsStatements.aggregate("rp_raw_7d", "mean();delete", null,
                "time >= '2026-10-24T00:00:00.000Z' AND time < '2026-10-24T01:00:00.000Z'",
                null, null, "temperature", "1m", "none", 10);
    }

    @Test
    public void deleteNamesTheRetentionPolicy() {
        String sql = MetricStatements.delete("rp_raw_7d",
                "time >= '2026-10-24T00:00:00.000Z' AND time < '2026-10-24T01:00:00.000Z'",
                "dev-001", null, "temperature");
        assertTrue(sql.startsWith("DELETE FROM \"rp_raw_7d\".\"device_metric\""));
    }

    @Test
    public void descendingCursorUsesExclusiveUpperBound() {
        assertEquals("time < '2026-10-24T00:06:00.000Z'",
                MetricStatements.cursorPredicate(false, "2026-10-24T00:06:00.000Z"));
    }

    @Test
    public void schemaFileMatchesStatements() throws Exception {
        String iql = Files.readString(Path.of("deploy/init/schema.iql"));
        assertTrue(iql.contains(SchemaStatements.minuteContinuousQuery("allst_iot")));
        assertTrue(iql.contains(SchemaStatements.hourContinuousQuery("allst_iot")));
        for (InfluxSchema.RetentionSpec spec : InfluxSchema.retentions()) {
            assertTrue(iql.contains(SchemaStatements.createRetention("allst_iot", spec, 1)));
        }
    }

    @Test
    public void seedTemperatureFollowsTheWave() {
        assertEquals(26.0, MetricWriteService.seedValue("temperature", 0), 0.001);
    }

    @Test
    public void rowsKeepTagsAndColumns() {
        QueryResult.Series series = new QueryResult.Series();
        series.setName("device_metric");
        series.setColumns(Arrays.asList("time", "value"));
        series.setTags(Collections.singletonMap("device_id", "dev-001"));
        series.setValues(Collections.singletonList(Arrays.asList("2026-10-24T00:00:00Z", 26.5)));
        QueryResult.Result result = new QueryResult.Result();
        result.setSeries(Collections.singletonList(series));
        QueryResult queryResult = new QueryResult();
        queryResult.setResults(Collections.singletonList(result));
        Map<String, Object> row = InfluxResults.rows(queryResult).get(0);
        assertEquals("dev-001", row.get("device_id"));
        assertEquals(26.5, row.get("value"));
    }

    @Test(expected = InfluxAccessException.class)
    public void queryErrorIsNotSuccess() {
        QueryResult.Result result = new QueryResult.Result();
        result.setError("database not found");
        QueryResult queryResult = new QueryResult();
        queryResult.setResults(Collections.singletonList(result));
        InfluxResults.requireSuccess(queryResult);
    }

    @Test
    public void lineProtocolKeepsIntegerStatus() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("time", "2026-10-24T00:00:00Z");
        row.put("device_id", "dev-001");
        row.put("region", "cn-east");
        row.put("metric_type", "temperature");
        row.put("value", 26.5);
        row.put("status", 1.0);
        String line = LogicalBackup.line(row);
        assertTrue(line.startsWith("device_metric,"));
        assertTrue(line.contains("device_id=dev-001"));
        assertTrue(line.contains("status=1i"));
        LogicalBackup.checkLine(line);
    }

    @Test(expected = IllegalArgumentException.class)
    public void backupFileCannotEscape() {
        BackupPaths.resolveFile(Path.of("target"), "../secret.lp");
    }

    @Test
    public void portableRestoreRefusesTheLiveDatabase() {
        try {
            PortableBackupCommands.restore("influxd", "127.0.0.1:8088",
                    "allst_iot", "rp_raw_7d", "allst_iot", Path.of("target"));
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("不允许覆盖"));
            return;
        }
        throw new AssertionError("应该拒绝覆盖正在使用的库");
    }

    @Test
    public void writeRulesRejectNonFiniteValue() {
        MetricWriteCommand command = new MetricWriteCommand();
        command.setDeviceId("dev-001");
        command.setRegion("cn-east");
        command.setMetricType("temperature");
        command.setValue(Double.NaN);
        try {
            MetricRules.validate(command);
        } catch (IllegalArgumentException ex) {
            return;
        }
        throw new AssertionError("NaN 应该被拒绝");
    }
}
