package com.allst.luence.entity;

/**
 * @author June
 * @since 2021年08月
 */
public class Book {
    private Integer id;
    private String name;
    private Float price;
    private String desc;
    private String picture;

    public Book() {
    }

    public Book(Integer id, String name, Float price, String desc, String picture) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.desc = desc;
        this.picture = picture;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", desc='" + desc + '\'' +
                ", picture='" + picture + '\'' +
                '}';
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

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
