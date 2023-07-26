# Oracle 11g
```text
username: scott password: 123456
hyj 123456
```

## Oracle 检索记录
```text
1、WHERE子句会比SELECT子句先执行

```

## Mybatis-flux
```text
创建数据库表
CREATE TABLE tb_account
(
    id        INTEGER PRIMARY KEY ,
    user_name VARCHAR(100),
    age       INTEGER,
    birthday  DATE
);
select * from tb_account ;
INSERT INTO tb_account(id, user_name, age, birthday) VALUES (1, '张三', 18, to_date('2020-01-11', 'yyyy-MM-dd'));
INSERT INTO tb_account(id, user_name, age, birthday) VALUES (2, '李四', 19, to_date('2021-03-21', 'yyyy-MM-dd'));
INSERT INTO tb_account(id, user_name, age, birthday) VALUES (3, '王五', 20, to_date('2022-09-01', 'yyyy-MM-dd'));
INSERT INTO tb_account(id, user_name, age, birthday) VALUES (4, '马六', 21, to_date('2023-02-14', 'yyyy-MM-dd'));

添加 Maven 依赖
<dependencies>
    <dependency>
        <groupId>com.mybatis-flex</groupId>
        <artifactId>mybatis-flex-spring-boot-starter</artifactId>
        <version>1.5.3</version>
    </dependency>
    <dependency>
        <groupId>com.mybatis-flex</groupId>
        <artifactId>mybatis-flex-processor</artifactId>
        <version>1.5.3</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
    </dependency>
    <dependency>
        <groupId>com.oracle.database.jdbc</groupId>
        <artifactId>ojdbc8</artifactId>
        <version>21.1.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.oracle.database.nls</groupId>
        <artifactId>orai18n</artifactId>
        <version>19.7.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.20</version>
    </dependency>
</dependencies>


```