package com.allst.boot.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Hutu
 * @since 2024-08-06 下午 10:34
 */
@Data
public abstract class BaseEntity {
    //@Field(value = "_id")
    private ObjectId _id;
}
