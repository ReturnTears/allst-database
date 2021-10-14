package com.allst.db.bean;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Lagou 实体
 * @author June
 * @since 2021年10月
 */
@Document("lagou")
public class Lagou {
    private String id;
    private String name;
    private Date birthday;
    private Double salary;
    private String city;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "Lagou{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", birthday=" + birthday +
                ", salary=" + salary +
                ", city='" + city + '\'' +
                '}';
    }
}
