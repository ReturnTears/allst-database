package com.allst.db.service;

import com.allst.db.bean.Person;
import com.allst.db.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author June
 * @since 2021年11月
 */
@Service("personService")
public class Neo4jPersonService {
    @Autowired
    private PersonRepository personRepository;

    public List<Person> getList() {
        List<Person> datas = new ArrayList<>();
        personRepository.findAll().forEach(datas::add);
        return datas;
    }

    public Person save(Person person) {
        return personRepository.save(person);
    }

    public  List<Person>  personList(double money){
        return  personRepository.personList1(money);
    }

    public List<Person> shortestPath(String startName,String endName){
        return personRepository.shortestPath(startName,endName);
    }
    public  List<Person> personListDept(String name){
        return  personRepository.personListDept(name);
    }
}
