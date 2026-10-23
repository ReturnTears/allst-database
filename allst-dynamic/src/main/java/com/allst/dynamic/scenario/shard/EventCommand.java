package com.allst.dynamic.scenario.shard;

import lombok.Data;

@Data
public class EventCommand {
    private String shardKey;
    private String eventType;
    private String payload;
}
