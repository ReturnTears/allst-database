package com.allst.boot.tuning;

import java.util.List;

public final class TuningNotes {

    private TuningNotes() {
    }

    public static List<String> checklist() {
        return List.of(
                "查询带完整分区键。聚簇列按表定义的顺序限制，不跳列。",
                "预编译语句由驱动按 CQL 缓存。值走绑定，不拼接。",
                "遥测写入带齐主键，并标记 idempotent，驱动才会在超时后重试。",
                "轻量事务和计数器不是幂等的，不能开投机重试。",
                "UNLOGGED BATCH 只装同一个分区。跨分区的日志批次会把压力打到一台协调节点。",
                "单次页大小不超过 200。大结果用 pagingState 连续取，不一次拉完。",
                "本地连接池 2 到 8 条就够。驱动 4 的默认值是 1，忙的时候会排队。",
                "投机执行保持关闭。语句如果不是幂等的，重试会写重。",
                "时序表用 TimeWindowCompactionStrategy，按天窗口，和日桶对齐。",
                "一致性：遥测 LOCAL_ONE，要确认副本的业务用 LOCAL_QUORUM。库存的条件更新再加 LOCAL_SERIAL。",
                "生产副本数是 3，策略用 NetworkTopologyStrategy。单节点演示保持 RF 1。",
                "tombstone_warn_threshold 默认 1000。一个分区里的删除太多时，读会先变慢再失败。"
        );
    }

    public static List<String> settings() {
        return List.of(
                "concurrent_reads",
                "concurrent_writes",
                "native_transport_max_threads",
                "read_request_timeout_in_ms",
                "write_request_timeout_in_ms",
                "range_request_timeout_in_ms",
                "hinted_handoff_enabled",
                "incremental_backups",
                "num_tokens",
                "tombstone_warn_threshold",
                "tombstone_failure_threshold",
                "commitlog_sync",
                "memtable_heap_space_in_mb",
                "key_cache_size_in_mb",
                "compaction_throughput_mb_per_sec"
        );
    }
}
