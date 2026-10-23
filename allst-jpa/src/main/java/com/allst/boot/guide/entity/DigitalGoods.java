package com.allst.boot.guide.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "g_digital_goods")
@DiscriminatorValue("DIGITAL")
public class DigitalGoods extends Product {

    @Column(length = 255)
    private String downloadUrl;

    private Integer licenseDays;
}
