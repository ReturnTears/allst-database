package com.allst.boot.guide.entity;

import com.allst.boot.guide.jackson.Views;
import com.allst.boot.guide.listener.AuditTrailListener;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDate;

/**
 * Jackson 注解和 JPA 回调放在同一个实体上，方便对照它们各自解决的问题。
 * password 只允许写入，不允许出现在响应里。
 */
@Getter
@Setter
@Entity
@Table(name = "g_customer")
@EntityListeners(AuditTrailListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Public.class)
    private Long id;

    @JsonProperty("customer_name")
    @JsonView(Views.Public.class)
    @Column(nullable = false, length = 64)
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 128)
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonView(Views.Public.class)
    private LocalDate birthday;

    @JsonView(Views.Public.class)
    @Column(length = 64)
    private String nickname;

    @JsonView(Views.Internal.class)
    @Column(length = 32)
    private String phone;

    @Transient
    @JsonIgnore
    private boolean loaded;
}
