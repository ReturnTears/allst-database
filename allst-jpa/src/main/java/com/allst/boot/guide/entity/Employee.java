package com.allst.boot.guide.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * 员工聚合。多对一默认是 EAGER，这里显式改成 LAZY，避免每次查员工都带出部门。
 */
@Getter
@Setter
@Entity
@Table(name = "g_employee", indexes = {
        @Index(name = "idx_g_employee_email", columnList = "email", unique = true),
        @Index(name = "idx_g_employee_department", columnList = "department_id")
})
@NamedEntityGraphs({
        @NamedEntityGraph(name = "Employee.department", attributeNodes = @NamedAttributeNode("department")),
        @NamedEntityGraph(name = "Employee.details", attributeNodes = {
                @NamedAttributeNode("department"),
                @NamedAttributeNode("profile"),
                @NamedAttributeNode("projects")
        })
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Employee extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 显式列名，对应「字段命名映射」里不完全交给命名策略的情况。
     */
    @Column(name = "emp_name", nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 128)
    private String email;

    private Integer age;

    @Column(name = "salary_amount", precision = 19, scale = 2)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonBackReference
    private Department department;

    @OneToOne(mappedBy = "employee", fetch = FetchType.LAZY, cascade = javax.persistence.CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private EmployeeProfile profile;

    @ManyToMany
    @JoinTable(
            name = "g_employee_project",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    @BatchSize(size = 20)
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();

    @Version
    private Long version;

    public void addProject(Project project) {
        projects.add(project);
        project.getEmployees().add(this);
    }

    public void attachProfile(EmployeeProfile employeeProfile) {
        this.profile = employeeProfile;
        employeeProfile.setEmployee(this);
    }
}
