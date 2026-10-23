package com.allst.dynamic.scenario.gray;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_feature")
public class FeatureFlag {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String featureKey;
    private String featureValue;
    private String versionTag;
}
