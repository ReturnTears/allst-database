package com.allst.dynamic.scenario.shard;

import lombok.Data;

import java.util.List;

@Data
public class MoveSlotsCommand {
    private String dataSource;
    private List<Integer> slots;
}
