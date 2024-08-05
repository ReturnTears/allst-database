package com.allst.boot.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;

/**
 * 一个同时使用JPA和Spring Data MongoDB注解的 domain 类。
 * 它定义了两个repository：JpaPersonRepository 和 MongoDBPersonRepository。
 * 一个用于JPA，另一个用于MongoDB的使用
 *
 * @author Hutu
 * @since 2024-08-05 下午 09:23
 */
@Data
@Entity
@Document("person")
@Table(name = "person")
public class Person {
    //private ObjectId _id;

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
