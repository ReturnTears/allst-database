package com.allst.dynamic.scenario.shard;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_event")
public class ShardEvent {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String shardKey;
    private String eventType;
    private String payload;
    private LocalDateTime createdAt;
}
