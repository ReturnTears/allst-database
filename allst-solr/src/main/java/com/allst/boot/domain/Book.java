package com.allst.boot.domain;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author Hutu
 * @since 2023-09-19 下午 10:25
 */
public class Book {
    @Field("id")
    private Integer id;
    @Field
    private String name;
    @Field
    private String description;
    @Field
    private Double price;

    public Book() {
    }

    public Book(Integer id, String name, String description, Double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}
