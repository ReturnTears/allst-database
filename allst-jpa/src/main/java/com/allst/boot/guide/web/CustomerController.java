package com.allst.boot.guide.web;

import com.allst.boot.guide.entity.Customer;
import com.allst.boot.guide.jackson.Views;
import com.allst.boot.guide.service.CustomerService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/guide/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @JsonView(Views.Public.class)
    public Customer register(@RequestParam String name,
                             @RequestParam String password,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday,
                             @RequestParam(required = false) String phone) {
        return customerService.register(name, password, birthday, phone);
    }

    @GetMapping("/{id}")
    @JsonView(Views.Public.class)
    public Customer publicView(@PathVariable Long id) {
        return customerService.get(id);
    }

    @GetMapping("/{id}/internal")
    @JsonView(Views.Internal.class)
    public Customer internalView(@PathVariable Long id) {
        return customerService.get(id);
    }
}
