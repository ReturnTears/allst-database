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
@Table(name = "g_book")
@DiscriminatorValue("BOOK")
public class Book extends Product {

    @Column(length = 32)
    private String isbn;

    @Column(length = 64)
    private String author;
}
