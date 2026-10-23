package com.allst.boot.guide.repository;

import com.allst.boot.guide.entity.Customer;
import com.allst.boot.guide.repository.support.GuideBaseRepository;

import java.util.Optional;

public interface CustomerRepository extends GuideBaseRepository<Customer, Long> {

    Optional<Customer> findByName(String name);
}
