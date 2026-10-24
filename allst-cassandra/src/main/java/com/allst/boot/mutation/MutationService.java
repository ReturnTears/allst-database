package com.allst.boot.mutation;

import com.allst.boot.cassandra.client.CqlGateway;
import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.common.Rows;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.allst.boot.cassandra.cql.Statements;
import com.allst.boot.reading.ReadingRules;
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MutationService {

    private final CqlGateway gateway;
    private final CassandraProperties properties;
    private final ZoneId zone;

    public MutationService(CqlGateway gateway, CassandraProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
        this.zone = ZoneId.of(properties.getZone());
    }

    public Map<String, Object> createStock(StockCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        BoundStatementBuilder builder = lwt(Statements.insertStock(properties.getKeyspace()))
                .setString("sku_id", MutationRules.sku(command.getSkuId()))
                .setString("warehouse", MutationRules.warehouse(command.getWarehouse()))
                .setInt("quantity", MutationRules.quantity(command.getQuantity(), "quantity"));
        return applied("sku_stock", Rows.first(gateway.execute(builder.build())),
                "IF NOT EXISTS 只在这一行还不存在时写入。applied 为 false 说明已经被占用。");
    }

    public Map<String, Object> compareAndSet(StockCasCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        BoundStatementBuilder builder = lwt(Statements.casStock(properties.getKeyspace()))
                .setString("sku_id", MutationRules.sku(command.getSkuId()))
                .setInt("expected", MutationRules.quantity(command.getExpected(), "expected"))
                .setInt("quantity", MutationRules.quantity(command.getQuantity(), "quantity"));
        return applied("sku_stock", Rows.first(gateway.execute(builder.build())),
                "条件不成立时 applied 为 false，返回里会带上当前的 quantity。调用方用新的 expected 再试。");
    }

    public Map<String, Object> stock(String skuId) {
        Map<String, Object> row = Rows.first(gateway.query(Statements.selectStock(properties.getKeyspace()),
                builder -> builder.setString("sku_id", MutationRules.sku(skuId))));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".sku_stock");
        body.put("row", row);
        return body;
    }

    public Map<String, Object> increment(CounterCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String deviceId = Identifiers.requireId(command.getDeviceId(), "deviceId");
        LocalDate day = command.getDay() == null || command.getDay().isBlank()
                ? LocalDate.now(zone) : ReadingRules.day(command.getDay());
        String metric = ReadingRules.requireMetric(command.getMetricType());
        long delta = MutationRules.delta(command.getDelta());
        gateway.execute(gateway.mutation(Statements.updateCounter(properties.getKeyspace()), false)
                .setString("device_id", deviceId)
                .setLocalDate("day_bucket", day)
                .setString("metric_type", metric)
                .setLong("delta", delta)
                .build());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".device_hit_counter");
        body.put("deviceId", deviceId);
        body.put("day", day.toString());
        body.put("metricType", metric);
        body.put("delta", delta);
        body.put("hint", "计数器不是幂等的。超时后不要盲目重试，重试会再加一次。读计数器可能短时间偏旧。");
        return body;
    }

    public Map<String, Object> counters(String deviceId, String dayText) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = dayText == null || dayText.isBlank() ? LocalDate.now(zone) : ReadingRules.day(dayText);
        List<Map<String, Object>> rows = Rows.list(gateway.query(Statements.selectCounter(properties.getKeyspace()),
                builder -> builder.setString("device_id", device).setLocalDate("day_bucket", day)), 20);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".device_hit_counter");
        body.put("rows", rows);
        body.put("count", rows.size());
        return body;
    }

    public Map<String, Object> saveProfile(ProfileCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String deviceId = Identifiers.requireId(command.getDeviceId(), "deviceId");
        String firmware = MutationRules.firmware(command.getFirmware());
        Set<String> labels = MutationRules.labels(command.getLabels(), false);
        Map<String, Double> thresholds = MutationRules.thresholds(command.getThresholds());
        List<String> channels = MutationRules.channels(command.getChannels());
        if (firmware == null && labels.isEmpty() && thresholds.isEmpty() && channels.isEmpty()) {
            throw new IllegalArgumentException("至少填写 firmware、labels、thresholds、channels 中的一项");
        }
        BoundStatementBuilder builder = gateway.mutation(Statements.insertProfile(properties.getKeyspace()), true)
                .setString("device_id", deviceId);
        if (firmware == null) {
            builder = builder.unset("firmware");
        } else {
            builder = builder.setString("firmware", firmware);
        }
        builder = labels.isEmpty() ? builder.unset("labels") : builder.setSet("labels", labels, String.class);
        builder = thresholds.isEmpty() ? builder.unset("thresholds")
                : builder.setMap("thresholds", thresholds, String.class, Double.class);
        builder = channels.isEmpty() ? builder.unset("channels") : builder.setList("channels", channels, String.class);
        gateway.execute(builder.build());
        return profileReceipt(deviceId, "整行替换已经提交的集合。空集合没有写入，避免把原值盖成空。");
    }

    public Map<String, Object> appendLabels(LabelCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String deviceId = Identifiers.requireId(command.getDeviceId(), "deviceId");
        Set<String> labels = MutationRules.labels(command.getLabels(), true);
        gateway.execute(gateway.mutation(Statements.appendLabels(properties.getKeyspace()), false)
                .setString("device_id", deviceId)
                .setSet("labels", labels, String.class)
                .build());
        return profileReceipt(deviceId, "set 追加会先读出这一格再写回。不要在遥测热分区上做集合追加。");
    }

    public Map<String, Object> updateThreshold(ThresholdCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String deviceId = Identifiers.requireId(command.getDeviceId(), "deviceId");
        String metric = ReadingRules.requireMetric(command.getMetricType());
        double threshold = MutationRules.threshold(command.getThreshold());
        gateway.execute(gateway.mutation(Statements.updateThreshold(properties.getKeyspace()), true)
                .setString("device_id", deviceId)
                .setString("metric_type", metric)
                .setDouble("threshold", threshold)
                .build());
        return profileReceipt(deviceId, "map 的单个键更新也是读改写。主键是 device_id，所以幂等，同一个值重试结果一样。");
    }

    public Map<String, Object> profile(String deviceId) {
        Map<String, Object> row = Rows.first(gateway.query(Statements.selectProfile(properties.getKeyspace()),
                builder -> builder.setString("device_id", Identifiers.requireId(deviceId, "deviceId"))));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".device_profile");
        body.put("row", row);
        return body;
    }

    private BoundStatementBuilder lwt(String cql) {
        return gateway.mutation(cql, false)
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
                .setSerialConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL);
    }

    private Map<String, Object> applied(String table, Map<String, Object> row, String hint) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + "." + table);
        body.put("row", row);
        body.put("applied", row != null && Boolean.TRUE.equals(row.get("[applied]")));
        body.put("consistency", "LOCAL_QUORUM");
        body.put("serialConsistency", "LOCAL_SERIAL");
        body.put("hint", hint);
        return body;
    }

    private Map<String, Object> profileReceipt(String deviceId, String hint) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".device_profile");
        body.put("deviceId", deviceId);
        body.put("hint", hint);
        return body;
    }
}
