package com.allst.boot.cassandra.schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DataModel {

    private DataModel() {
    }

    public static Map<String, Object> describe() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("keyspace", "allst_iot");
        body.put("zone", "Asia/Shanghai");
        body.put("tables", tables());
        body.put("rules", rules());
        return body;
    }

    public static List<Map<String, Object>> tables() {
        List<Map<String, Object>> tables = new ArrayList<>();
        tables.add(table("sensor_reading", "((device_id, day_bucket), metric_type, recorded_at, reading_id)",
                "一台设备一天一个分区。先写 metric_type 才能按时间范围查。device_model 是 static。"));
        tables.add(table("reading_by_region", "((region, day_bucket), metric_type, recorded_at, device_id, reading_id)",
                "区域查询的第二张表，由应用双写。不要用 ALLOW FILTERING 在原表上按 region 查。"));
        tables.add(table("sensor_hourly", "((device_id, day_bucket), hour_bucket, metric_type)",
                "小时降采样。Cassandra 没有连续查询，由应用读一个分区后写入。"));
        tables.add(table("reading_by_metric", "((metric_type, day_bucket), recorded_at, device_id, reading_id)",
                "物化视图。基表写入后异步维护，适合按指标反查，写入会被放大。"));
        tables.add(table("device_hit_counter", "((device_id, day_bucket), metric_type)",
                "计数器表。除了主键只能有 counter 列，不能设 TTL，也不能和普通列放在一起。"));
        tables.add(table("sku_stock", "(sku_id)", "库存比较并设置。走 Paxos，吞吐低，不拿来写遥测。"));
        tables.add(table("device_profile", "(device_id)", "非冻结集合。和时序分区分开，避免集合更新把遥测分区写热。"));
        return tables;
    }

    public static List<String> rules() {
        return List.of(
                "先有查询，再定主键。分区键决定数据落在哪台机器，聚簇键决定分区内的顺序。",
                "时序按设备加日桶分区。一个分区控制在十万行、一百兆以内，不要把所有设备写进同一个分区。",
                "查询必须带完整分区键。聚簇列要按顺序给，不能跳过 metric_type 直接限制 recorded_at。",
                "一张表服务一类查询。第二种查询用双写或物化视图，不在原表上 ALLOW FILTERING。",
                "写入用客户端生成的 reading_id，保证整行主键稳定，重试才是幂等的。不要用 now() 当主键。",
                "空字段用 unset，不要绑定 null。null 会写成墓碑。",
                "USING TTL 会作用在这条 INSERT 的所有普通列上。static 列单独写，避免短 TTL 把设备型号提前删掉。",
                "计数器、轻量事务、普通插入是三条不同的写路径，不能放进同一个 BATCH。"
        );
    }

    private static Map<String, Object> table(String name, String primaryKey, String query) {
        Map<String, Object> table = new LinkedHashMap<>();
        table.put("table", name);
        table.put("primaryKey", primaryKey);
        table.put("query", query);
        return table;
    }
}
