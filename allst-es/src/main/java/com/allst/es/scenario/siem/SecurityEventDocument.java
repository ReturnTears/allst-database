package com.allst.es.scenario.siem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * 安全事件的扁平字段。生产环境可再向 Elastic Common Schema 靠拢。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = SecurityEventDocument.INDEX, createIndex = false)
public class SecurityEventDocument {
    public static final String INDEX = "scenario_security_event";

    @Id
    private String id;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime eventTime;

    @Field(type = FieldType.Keyword)
    private String eventType;

    @Field(type = FieldType.Keyword)
    private String severity;

    @Field(type = FieldType.Keyword)
    private String srcIp;

    @Field(type = FieldType.Keyword)
    private String destIp;

    @Field(type = FieldType.Integer)
    private Integer srcPort;

    @Field(type = FieldType.Integer)
    private Integer destPort;

    @Field(type = FieldType.Keyword)
    private String userName;

    @Field(type = FieldType.Keyword)
    private String host;

    @Field(type = FieldType.Keyword)
    private String action;

    @Field(type = FieldType.Keyword)
    private String outcome;

    @Field(type = FieldType.Keyword)
    private String ruleId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String message;
}
