# Spring Boot Jpa
```text
Spring Data JPA 是 Spring Data 项目的一部分，它提供了一种简化的数据访问方式，用于与关系型数据库进行交互。
它基于 Java Persistence API（JPA） 标准，并提供了一套简洁的 API 和注解，使开发人员能够通过简单的 Java 对象来表示数据库表，
并通过自动生成的 SQL 语句执行常见的 CRUD 操作。Spring Data JPA 通过封装 JPA 的复杂性，简化了数据访问层的开发工作，
使开发人员能够更专注于业务逻辑的实现。它还提供了丰富的查询方法的定义、分页和排序支持、事务管理等功能，使开发人员能够更方便地进行数据访问和操作。

Spring Data JPA 为 Jakarta Persistence API（JPA）提供 repository 支持。它简化了需要访问JPA数据源的应用程序的开发。

Spring Data Repository 抽象的目标是大大减少为各种持久性store实现数据访问层所需的模板代码量。

Spring Data repository 抽象的中心接口是 Repository。它把要管理的 domain 类以及 domain 类的ID类型作为泛型参数。
这个接口主要是作为一个标记接口，用来捕捉工作中的类型，并帮助你发现扩展这个接口的接口。 
CrudRepository 和 ListCrudRepository 接口为被管理的实体类提供复杂的CRUD功能。

```
## CrudRepository 接口
```java
/**
 * 1保存给定的实体。
 * 2根据ID返回实体。
 * 3返回所有实体。
 * 4返回实体数量。
 * 5删除给定的实体。
 * 6根据ID判断实体是否存在。
 */
public interface CrudRepository<T, ID> extends Repository<T, ID> {

  <S extends T> S save(S entity);      //(1)

  Optional<T> findById(ID primaryKey); //(2)

  Iterable<T> findAll();               //(3)

  long count();                        //(4)

  void delete(T entity);               //(5)

  boolean existsById(ID primaryKey);   //(6)

  // … more functionality omitted.
}
```
```text
ListCrudRepository 提供了同等的方法，但它们返回 List，而 CrudRepository 的方法返回 Iterable。
除了 CrudRepository 之外，还有一个 PagingAndSortingRepository 的抽象，它增加了额外的分页，排序方法。
```
## PagingAndSortingRepository 接口
```java
public interface PagingAndSortingRepository<T, ID>  {

  Iterable<T> findAll(Sort sort);

  Page<T> findAll(Pageable pageable);
}
```
