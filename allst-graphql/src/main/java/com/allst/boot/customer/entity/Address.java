package com.allst.boot.customer.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 64)
    private String receiver;

    @Column(nullable = false, length = 32)
    private String phone;

    @Column(nullable = false, length = 32)
    private String province;

    @Column(nullable = false, length = 32)
    private String city;

    @Column(nullable = false, length = 255)
    private String detail;

    @Column(name = "default_address", nullable = false)
    private boolean defaultAddress;
}
