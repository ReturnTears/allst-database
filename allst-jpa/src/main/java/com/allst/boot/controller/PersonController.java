package com.allst.boot.controller;

import com.allst.boot.entity.PersonMongo;
import com.allst.boot.model.PersonBo;
import com.allst.boot.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hutu
 * @since 2024-08-05 下午 09:09
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;

    @GetMapping("/findByName/{name}")
    public PersonBo findByName(@PathVariable String name) {
        return personService.findByName(name);
    }

    @GetMapping("/findById/{id}")
    public PersonBo findById(@PathVariable Long id) {
        return personService.findById(id);
    }

    @GetMapping("/findPerByName/{name}")
    public PersonMongo findPersonByName(@PathVariable String name) {
        return personService.findPersonByName(name);
    }
}
