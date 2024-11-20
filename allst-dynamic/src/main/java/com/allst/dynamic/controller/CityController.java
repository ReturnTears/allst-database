package com.allst.dynamic.controller;

import com.allst.dynamic.service.CityService;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hutu
 * @since 2024-11-20 下午 10:13
 */
@RestController
@RequestMapping("/city")
public class CityController {

    @Autowired
    private CityService cityService;
    @GetMapping("/get/{id}")
    public String getCity(@PathVariable long id) {
        return cityService.getById(id).getName();
    }

    @DS("slave")
    @GetMapping("/get2/{id}")
    public String getCity2(@PathVariable long id) {
        return cityService.getById(id).getName();
    }
}
