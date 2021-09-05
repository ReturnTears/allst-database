package com.allst.mysql.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author June
 * @since 2021年08月
 */
@Entity
@Table(name = "city")
public class City implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "province")
    private String province;

    public City() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public City(long id, String name, String province) {
        this.id = id;
        this.name = name;
        this.province = province;
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", province='" + province + '\'' +
                '}';
    }
}
