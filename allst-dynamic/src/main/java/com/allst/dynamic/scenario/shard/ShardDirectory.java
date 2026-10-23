package com.allst.dynamic.scenario.shard;

import com.allst.dynamic.scenario.common.DataSourceKeys;
import com.allst.dynamic.scenario.common.ScenarioException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * 16 个槽位。分片键先落到槽，槽再指向数据源。扩容时只改槽位指向，不改分片键算法。
 */
@Component
public class ShardDirectory {

    public static final int SLOT_COUNT = 16;

    private final AtomicReferenceArray<String> slots = new AtomicReferenceArray<>(SLOT_COUNT);

    public ShardDirectory() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots.set(i, i % 2 == 0 ? DataSourceKeys.DATA_SHARD_0 : DataSourceKeys.DATA_SHARD_1);
        }
    }

    public int slotOf(String shardKey) {
        return Math.floorMod(shardKey.hashCode(), SLOT_COUNT);
    }

    public String dataSourceOf(String shardKey) {
        return slots.get(slotOf(shardKey));
    }

    public ShardLocation locate(String shardKey) {
        int slot = slotOf(shardKey);
        return new ShardLocation(shardKey, slot, slots.get(slot));
    }

    public void move(String dataSource, List<Integer> slotIndexes) {
        if (slotIndexes == null || slotIndexes.isEmpty()) {
            throw new ScenarioException(400, "slots 不能为空");
        }
        for (Integer slot : slotIndexes) {
            if (slot == null || slot < 0 || slot >= SLOT_COUNT) {
                throw new ScenarioException(400, "槽位必须在 0 到 " + (SLOT_COUNT - 1) + " 之间");
            }
        }
        for (Integer slot : slotIndexes) {
            slots.set(slot, dataSource);
        }
    }

    public List<ShardSlot> snapshot() {
        List<ShardSlot> all = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            all.add(new ShardSlot(i, slots.get(i)));
        }
        return all;
    }

    @Data
    @AllArgsConstructor
    public static class ShardLocation {
        private String shardKey;
        private int slot;
        private String dataSource;
    }

    @Data
    @AllArgsConstructor
    public static class ShardSlot {
        private int slot;
        private String dataSource;
    }
}
