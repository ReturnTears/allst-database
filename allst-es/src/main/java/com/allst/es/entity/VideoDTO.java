package com.allst.es.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * @author Hutu
 * @since 2024-08-22 下午 09:19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "video")
public class VideoDTO {
    @Id
    @Field(type = FieldType.Text, index = false)
    private Long id;
    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Integer)
    private Integer duration;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;

    public VideoDTO(Long id, String title, String description, Integer duration, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.createTime = LocalDateTime.now();
        this.category = category;
    }
}
