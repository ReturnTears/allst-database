package com.allst.es.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Hutu
 * @since 2025-01-02 下午 10:03
 */
@Data
@TableName("user")
public class User {
    private Long id;
    private String name;
    private Integer age;
    private String email;
}
