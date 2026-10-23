package com.allst.dynamic.scenario.tenant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_account")
public class TenantAccount {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String accountName;
    private String levelName;
    private LocalDateTime createdAt;
}
