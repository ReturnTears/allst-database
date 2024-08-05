package com.allst.boot.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author Hutu
 * @since 2024-08-05 下午 09:23
 */
@Data
@Entity
@Table(name = "person")
public class Person {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private Integer age;

    @Column(name = "address")
    private String address;
}
