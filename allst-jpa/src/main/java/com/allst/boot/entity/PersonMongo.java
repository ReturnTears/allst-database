package com.allst.boot.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
public class PersonMongo {
    @Field(value = "_id")
    private ObjectId _id;

    @Id
    @Field(value = "id")
    private Long id;

    @Field(value = "name")
    private String name;

    @Field(value = "age")
    private Integer age;

    @Field(value = "address")
    private String address;

}
