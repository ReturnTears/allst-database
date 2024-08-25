package com.allst.postgresql.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Hutu
 * @since 2024-08-25 下午 02:19
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "user_info")
public class UserInfo {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Integer age;
}
