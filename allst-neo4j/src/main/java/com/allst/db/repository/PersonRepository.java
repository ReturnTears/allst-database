package com.allst.db.repository;

import com.allst.db.bean.Person;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author June
 * @since 2021年11月
 */
@Repository
public interface PersonRepository extends Neo4jRepository<Person, Long> {
    /**
     * 查看money 大于指定值的Person 列表
     */
    @Query("match(p:Person) where p.money>{0} return p")
    List<Person> personList1(@Param("money") double money);

    @Query("match(p:Person) where p.money>{money} return p")
    List<Person> personList2(@Param("money") double money);

    /**
     * 指定开始的名字 和 结束的名字 查询最短路径  限定深度为4以层包含4
     */
    @Query("match p=shortestPath((person:Person{name:{startName}})-[*1..4]-(person2:Person {name:{endName}})) return p")
    List<Person> shortestPath(String startName, String endName);

    @Query("match p =(person:Person {name:{name}})-[*1..2]-(:Person) return p")
    List<Person> personListDept(String name);
}
