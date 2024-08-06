package com.allst.boot.service;

import com.allst.boot.entity.Person;
import com.allst.boot.entity.PersonMongo;
import com.allst.boot.model.PersonBo;
import com.allst.boot.repository.jpa.PersonRepository;
import com.allst.boot.repository.mongo.MongoDBPersonRepository;
import com.allst.boot.toobj.PersonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Hutu
 * @since 2024-08-05 下午 09:42
 */
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    private final MongoDBPersonRepository mongoDBPersonRepository;

    @Override
    public void save(PersonBo personBo) {

    }

    @Override
    public PersonBo findById(Long id) {
        Optional<Person> person = personRepository.findById(id);
        return person.map(PersonMapper.INSTANCE::personToPersonDto).orElse(null);
    }

    @Override
    public PersonBo findByName(String name) {
        Person person = personRepository.findByName(name);
        return PersonMapper.INSTANCE.personToPersonDto(person);
    }

    @Override
    public PersonMongo findPersonByName(String name) {
        PersonMongo personMongo = mongoDBPersonRepository.findByName(name);
        System.out.println("person: " + personMongo);
        return personMongo;
    }
}
