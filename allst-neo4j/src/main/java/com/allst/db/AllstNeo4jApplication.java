package com.allst.db;

import com.allst.db.bean.Person;
import com.allst.db.service.Neo4jPersonService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
public class AllstNeo4jApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(AllstNeo4jApplication.class, args);
        Neo4jPersonService personService = applicationContext.getBean("personService", Neo4jPersonService.class);
        Person person = new Person();
        person.setName("xiaoQ");
        person.setMoney(12345.45);
        person.setCharacter("A");
        person.setAge(11);
        Person p1 = personService.save(person);
        System.out.println(p1);

        List<Person> list = personService.getList();
        list.forEach(System.out::println);
    }

}
