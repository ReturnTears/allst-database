package com.allst.boot.cassandra;

import com.allst.boot.cassandra.common.ContactPoints;
import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.common.Instants;
import com.allst.boot.cassandra.common.PageTokens;
import com.allst.boot.cassandra.cql.Statements;
import com.allst.boot.cassandra.schema.DataModel;
import com.allst.boot.catalog.CatalogController;
import com.allst.boot.continuous.HourlyRollup;
import com.allst.boot.governance.GovernanceNotes;
import com.allst.boot.ops.BackupCommands;
import com.allst.boot.ops.BackupPaths;
import com.allst.boot.reading.DayBuckets;
import com.allst.boot.reading.ReadingIds;
import com.allst.boot.reading.ReadingRules;
import com.allst.boot.reading.ReadingWriteCommand;
import com.allst.boot.reading.SeedPlan;
import com.allst.boot.reading.ValidReading;
import com.allst.boot.tuning.TuningNotes;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CassandraSpecTest {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Instant NOON = Instant.parse("2026-10-24T04:00:00Z");

    @Test
    void shanghaiLocalTimeAndDayBucket() {
        Instant instant = Instants.parse("2026-10-24 08:00:00", SHANGHAI);
        assertEquals(Instant.parse("2026-10-24T00:00:00Z"), instant);
        assertEquals(LocalDate.of(2026, 10, 25), DayBuckets.of(Instant.parse("2026-10-24T16:30:00Z"), SHANGHAI));
    }

    @Test
    void timeRangeMustStayInsideTheDayBucket() {
        LocalDate day = LocalDate.of(2026, 10, 24);
        Instant start = Instants.parse("2026-10-24 00:00:00", SHANGHAI);
        Instant end = day.plusDays(1).atStartOfDay(SHANGHAI).toInstant();
        DayBuckets.requireInside(day, start, end, SHANGHAI);
        assertThrows(IllegalArgumentException.class,
                () -> DayBuckets.requireInside(day, start.minusSeconds(1), end, SHANGHAI));
    }

    @Test
    void readingIdIsStableAndInjectionIsRejected() {
        UUIDCheck();
        ReadingWriteCommand command = sample();
        command.setDeviceId("dev' OR '1'='1");
        assertThrows(IllegalArgumentException.class, () -> ReadingRules.validate(command, SHANGHAI, NOON));
    }

    @Test
    void ttlZeroAndUnknownMetricAreRejected() {
        ReadingWriteCommand ttl = sample();
        ttl.setTtlSeconds(0);
        assertThrows(IllegalArgumentException.class, () -> ReadingRules.validate(ttl, SHANGHAI, NOON));
        ReadingWriteCommand metric = sample();
        metric.setMetricType("cpu;drop");
        assertThrows(IllegalArgumentException.class, () -> ReadingRules.validate(metric, SHANGHAI, NOON));
    }

    @Test
    void batchRejectsDuplicatesAndTooManyRows() {
        ReadingWriteCommand first = sample();
        ReadingWriteCommand second = sample();
        assertThrows(IllegalArgumentException.class,
                () -> ReadingRules.validateBatch(List.of(first, second), SHANGHAI, NOON));
        assertEquals(540, SeedPlan.commands(NOON).size());
        assertThrows(IllegalArgumentException.class,
                () -> ReadingRules.validateBatch(SeedPlan.commands(NOON), SHANGHAI, NOON));
        ReadingRules.validate(SeedPlan.commands(NOON).get(0), SHANGHAI, NOON);
    }

    @Test
    void orderAndConsistencyStayOnTheWhitelist() {
        assertFalse(ReadingRules.ascending("desc"));
        assertTrue(ReadingRules.ascending("ASC"));
        assertThrows(IllegalArgumentException.class, () -> ReadingRules.ascending("asc; DROP KEYSPACE allst_iot"));
        assertEquals("LOCAL_ONE", Identifiers.requireConsistency("local_one"));
        assertThrows(IllegalArgumentException.class, () -> Identifiers.requireConsistency("ANY"));
        assertThrows(IllegalArgumentException.class, () -> ContactPoints.parse("127.0.0.1:9042;rm"));
    }

    @Test
    void pagingTokenRoundTripRejectsPathTricks() {
        String token = PageTokens.encode(ByteBuffer.wrap(new byte[]{1, 2, 3, 4}));
        assertEquals(4, PageTokens.decode(token).remaining());
        assertThrows(IllegalArgumentException.class, () -> PageTokens.decode("../etc/passwd"));
    }

    @Test
    void schemaUsesTimeWindowAndDoesNotAllowFiltering() {
        String cql = String.join("\n", Statements.all("allst_iot", "SimpleStrategy", 1, Map.of()));
        assertTrue(cql.contains("TimeWindowCompactionStrategy"));
        assertTrue(cql.contains("hits counter"));
        assertTrue(cql.contains("CREATE MATERIALIZED VIEW IF NOT EXISTS allst_iot.reading_by_metric"));
        assertTrue(cql.contains("sensor_reading_status_idx"));
        assertFalse(cql.contains("ALLOW FILTERING"));
        assertFalse(cql.contains("StorageAttachedIndex"));
        String network = Statements.replicationMap("NetworkTopologyStrategy", 1, Map.of("datacenter1", 3));
        assertTrue(network.contains("'datacenter1': 3"));
        assertThrows(IllegalArgumentException.class, () -> Statements.createKeyspace("system_auth", "SimpleStrategy", 1, Map.of()));
    }

    @Test
    void queriesBindValuesAndKeepFunctionsFixed() {
        String ranged = Statements.selectReadings("allst_iot", true, true, true);
        assertTrue(ranged.contains("ORDER BY recorded_at ASC, reading_id DESC"));
        assertTrue(ranged.contains(":range_from"));
        assertFalse(ranged.contains("ALLOW FILTERING"));
        String aggregate = Statements.aggregate("allst_iot", "AVG");
        assertTrue(aggregate.contains("avg(sensor_value) AS value_avg"));
        assertThrows(IllegalArgumentException.class, () -> Statements.aggregate("allst_iot", "avg();delete"));
        assertTrue(Statements.casStock("allst_iot").contains("IF quantity = :expected"));
        assertTrue(Statements.insertStock("allst_iot").contains("IF NOT EXISTS"));
        assertTrue(Statements.updateCounter("allst_iot").contains("hits = hits + :delta"));
        assertTrue(Statements.arithmetic("allst_iot").contains("sensor_value * 1.8 + 32"));
        assertTrue(Statements.scalar("allst_iot").contains("writetime(sensor_value)"));
        assertTrue(Statements.scalar("allst_iot").contains("CAST(sensor_value AS text)"));
    }

    @Test
    void hourlyBucketsDoNotAverageTheAverage() {
        Instant first = Instant.parse("2026-10-24T01:10:00Z");
        Instant second = Instant.parse("2026-10-24T01:40:00Z");
        Instant nextHour = Instant.parse("2026-10-24T02:00:00Z");
        Map<HourlyRollup.Key, HourlyRollup.Aggregate> buckets = HourlyRollup.accumulate(List.of(
                new HourlyRollup.Point("temperature", first, 10),
                new HourlyRollup.Point("temperature", second, 20),
                new HourlyRollup.Point("humidity", nextHour, 5)
        ), ZoneOffset.UTC);
        assertEquals(2, buckets.size());
        HourlyRollup.Aggregate temperature = buckets.get(new HourlyRollup.Key("temperature", Instant.parse("2026-10-24T01:00:00Z")));
        assertEquals(2, temperature.count());
        assertEquals(30d, temperature.sum());
        assertEquals(10d, temperature.min());
        assertEquals(20d, temperature.max());
    }

    @Test
    void backupNamesCannotEscapeTheDirectory() {
        Path root = Path.of("data", "cassandra-backup");
        assertTrue(BackupPaths.resolveFile(root, "sensor-reading-20261024T000000Z.jsonl").endsWith("sensor-reading-20261024T000000Z.jsonl"));
        assertThrows(IllegalArgumentException.class, () -> BackupPaths.resolveFile(root, "../secret.jsonl"));
        assertThrows(IllegalArgumentException.class, () -> BackupCommands.plan("allst_iot", "tag;rm"));
        assertTrue(BackupCommands.plan("allst_iot", "allst-20261024").get("commands").toString().contains("nodetool snapshot allst_iot"));
    }

    @Test
    void catalogPointsAtExistingDocuments() {
        assertEquals(11, CatalogController.entries().size());
        assertFalse(DataModel.rules().isEmpty());
        assertFalse(GovernanceNotes.advice().isEmpty());
        assertFalse(TuningNotes.checklist().isEmpty());
        for (Map<String, String> entry : CatalogController.entries()) {
            assertTrue(Files.isRegularFile(Path.of(entry.get("doc"))), entry.get("doc"));
        }
    }

    private static void UUIDCheck() {
        Instant time = Instant.parse("2026-10-24T00:00:00Z");
        LocalDate day = LocalDate.of(2026, 10, 24);
        assertEquals(ReadingIds.of("dev-001", day, "temperature", time),
                ReadingIds.of("dev-001", day, "temperature", time));
    }

    private static ReadingWriteCommand sample() {
        ReadingWriteCommand command = new ReadingWriteCommand();
        command.setDeviceId("dev-001");
        command.setRegion("cn-east");
        command.setMetricType("temperature");
        command.setValue(26.5);
        command.setStatus(1);
        command.setTime("2026-10-24T04:00:00Z");
        return command;
    }

    @Test
    void sampleCommandPasses() {
        ValidReading reading = ReadingRules.validate(sample(), SHANGHAI, NOON);
        assertEquals(LocalDate.of(2026, 10, 24), reading.dayBucket());
        assertEquals("dev-001", reading.deviceId());
    }
}
