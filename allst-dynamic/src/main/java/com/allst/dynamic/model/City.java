package com.allst.dynamic.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Hutu
 * @since 2024-11-20 下午 10:08
 */
@Data
@TableName("city")
public class City {
    private Long id;
    private String name;
    private String province;
}
