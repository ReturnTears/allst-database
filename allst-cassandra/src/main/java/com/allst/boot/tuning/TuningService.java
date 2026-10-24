package com.allst.boot.tuning;

import com.allst.boot.cassandra.client.CqlGateway;
import com.allst.boot.cassandra.common.CassandraAccessException;
import com.allst.boot.cassandra.common.Limits;
import com.allst.boot.cassandra.common.Rows;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.allst.boot.cassandra.cql.Statements;
import com.allst.boot.governance.GovernanceService;
import com.allst.boot.reading.ReadingRules;
import com.allst.boot.reading.ReadingWriteCommand;
import com.allst.boot.reading.ReadingWriteService;
import com.allst.boot.reading.ValidReading;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TuningService {

    static final String BENCH_DEVICE = "bench-tuning";
    static final String BENCH_REGION = "bench";

    private final CqlGateway gateway;
    private final CassandraProperties properties;
    private final ReadingWriteService writeService;
    private final GovernanceService governanceService;
    private final ZoneId zone;

    public TuningService(CqlGateway gateway, CassandraProperties properties, ReadingWriteService writeService,
                         GovernanceService governanceService) {
        this.gateway = gateway;
        this.properties = properties;
        this.writeService = writeService;
        this.governanceService = governanceService;
        this.zone = ZoneId.of(properties.getZone());
    }

    public Map<String, Object> profile() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contactPoints", properties.getContactPoints());
        body.put("localDatacenter", properties.getLocalDatacenter());
        body.put("keyspace", properties.getKeyspace());
        body.put("authentication", properties.getUsername() != null && !properties.getUsername().isBlank());
        body.put("consistency", properties.getRequest().getConsistency());
        body.put("timeout", properties.getRequest().getTimeout().toString());
        body.put("pageSize", properties.getRequest().getPageSize());
        body.put("poolLocalSize", properties.getPool().getLocalSize());
        body.put("poolRemoteSize", properties.getPool().getRemoteSize());
        body.put("zone", properties.getZone());
        body.put("checklist", TuningNotes.checklist());
        return body;
    }

    public Map<String, Object> runtime() {
        Map<String, Object> local = Rows.first(gateway.query(Statements.selectLocal(), builder -> {
        }));
        List<Map<String, Object>> peers = Rows.list(gateway.query(Statements.selectPeers(), builder -> {
        }), 32);
        for (Map<String, Object> peer : peers) {
            Object address = peer.get("peer");
            if (address instanceof InetAddress inetAddress) {
                peer.put("peer", inetAddress.getHostAddress());
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("local", local);
        body.put("peers", peers);
        body.put("peerCount", peers.size());
        try {
            List<Map<String, Object>> rows = Rows.list(gateway.query(Statements.selectSettings(), builder -> {
            }), 800);
            body.put("settings", filterSettings(rows));
        } catch (CassandraAccessException ex) {
            body.put("settingsError", ex.getMessage());
        }
        body.put("hint", "system.local 是本协调节点，system.peers 是其他节点。应用配置的 local-datacenter 必须和 data_center 一致。");
        return body;
    }

    public Map<String, Object> benchmark(Integer rows) {
        int count = Limits.limit(rows, Limits.BENCHMARK_DEFAULT, Limits.BENCHMARK_MAX, "rows");
        Instant recordedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        List<ReadingWriteCommand> commands = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ReadingWriteCommand command = new ReadingWriteCommand();
            command.setDeviceId(BENCH_DEVICE);
            command.setRegion(BENCH_REGION);
            command.setMetricType("temperature");
            command.setValue(i + 1d);
            command.setStatus(1);
            command.setTime(recordedAt.toString());
            command.setReadingId(new UUID(0L, i + 1L).toString());
            commands.add(command);
        }
        List<ValidReading> readings = ReadingRules.validateBatch(commands, zone, Instant.now());
        long started = System.nanoTime();
        writeService.writeValidated(readings, true);
        long elapsedMs = Duration.ofNanos(System.nanoTime() - started).toMillis();
        String day = readings.get(0).dayBucket().toString();
        try {
            governanceService.deletePartition(BENCH_DEVICE, day);
            governanceService.deleteRegionPartition(BENCH_REGION, day);
        } catch (RuntimeException ex) {
            Map<String, Object> body = baseBench(count, elapsedMs, day);
            body.put("cleaned", false);
            body.put("cleanupError", ex.getMessage());
            return body;
        }
        Map<String, Object> body = baseBench(count, elapsedMs, day);
        body.put("cleaned", true);
        return body;
    }

    private static Map<String, Object> baseBench(int count, long elapsedMs, String day) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deviceId", BENCH_DEVICE);
        body.put("day", day);
        body.put("rows", count);
        body.put("elapsedMs", elapsedMs);
        body.put("hint", "这是同一个分区里的 UNLOGGED BATCH，用来看协调节点确认一批写入要多久。不要把这个接口当压测器，上限 100 行。");
        return body;
    }

    private static List<Map<String, Object>> filterSettings(List<Map<String, Object>> rows) {
        List<String> wanted = TuningNotes.settings();
        List<Map<String, Object>> matched = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object name = row.get("name");
            if (name != null && wanted.contains(name.toString())) {
                matched.add(row);
            }
        }
        return matched;
    }
}
