package com.allst.boot.guide.service;

import com.allst.boot.guide.common.NotFoundException;
import com.allst.boot.guide.entity.Customer;
import com.allst.boot.guide.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer register(String name, String password, LocalDate birthday, String phone) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPassword(password);
        customer.setBirthday(birthday);
        customer.setPhone(phone);
        return customerRepository.save(customer);
    }

    public Customer get(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("客户不存在: " + id));
    }
}
