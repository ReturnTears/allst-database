package com.allst.boot.guide.es;

import com.allst.boot.guide.entity.Employee;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 搜索文档。它不是 JPA 实体，不能放进 EntityManager 的扫描包。
 */
@Getter
@Setter
@Document(indexName = "guide_employee")
public class EmployeeDocument {

    @Id
    private String id;

    private Long employeeId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String email;

    public static EmployeeDocument from(Employee employee) {
        EmployeeDocument document = new EmployeeDocument();
        document.setId(String.valueOf(employee.getId()));
        document.setEmployeeId(employee.getId());
        document.setName(employee.getName());
        document.setEmail(employee.getEmail());
        return document;
    }
}
