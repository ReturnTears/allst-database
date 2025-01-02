package com.allst.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author Hutu
 * @since 2025-01-02 下午 10:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "es_user")
public class EsUser {
    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Integer)
    private Integer age;
    @Field(type = FieldType.Text)
    private String email;
}
