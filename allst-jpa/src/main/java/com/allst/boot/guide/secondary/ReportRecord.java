package com.allst.boot.guide.secondary;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 第二数据源上的报表实体。主持久化单元不要扫描这个包，否则两套库会建出同一张表。
 */
@Getter
@Setter
@Entity
@Table(name = "g_report_record")
public class ReportRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String title;

    @Column(length = 255)
    private String body;
}
