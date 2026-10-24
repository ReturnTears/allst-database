package com.allst.db;

import com.allst.db.bean.Person;
import com.allst.db.service.Neo4jPersonService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
@EntityScan("com.allst.db.bean")
public class AllstNeo4jApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(AllstNeo4jApplication.class, args);
        runLegacyPersonDemo(applicationContext);
    }

    /**
     * 保留原来的 Person 练习。Neo4j 4 与旧版 OGM 驱动不兼容时只记录原因，规范示例接口仍然启动。
     */
    private static void runLegacyPersonDemo(ApplicationContext applicationContext) {
        try {
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
        } catch (Exception ex) {
            System.err.println("[legacy-person-demo] 旧的 SDN Person 练习未执行，/api/neo4j/guide 仍可使用。原因: " + ex.getMessage());
        }
    }

}
