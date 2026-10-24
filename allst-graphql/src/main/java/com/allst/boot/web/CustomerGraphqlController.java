package com.allst.boot.web;

import com.allst.boot.common.PageInput;
import com.allst.boot.common.Slice;
import com.allst.boot.customer.dto.AddressInput;
import com.allst.boot.customer.dto.AddressView;
import com.allst.boot.customer.dto.CreateCustomerInput;
import com.allst.boot.customer.dto.CustomerInput;
import com.allst.boot.customer.dto.CustomerView;
import com.allst.boot.customer.service.CustomerService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class CustomerGraphqlController {

    private final CustomerService customerService;

    public CustomerGraphqlController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @QueryMapping
    public CustomerView customer(@Argument Long id) {
        return customerService.get(id);
    }

    @QueryMapping
    public Slice<CustomerView> customers(@Argument PageInput page) {
        return customerService.page(page);
    }

    @MutationMapping
    public CustomerView createCustomer(@Argument CreateCustomerInput input) {
        return customerService.create(input);
    }

    @MutationMapping
    public CustomerView updateCustomer(@Argument Long id, @Argument CustomerInput input) {
        return customerService.update(id, input);
    }

    @MutationMapping
    public boolean deleteCustomer(@Argument Long id) {
        return customerService.delete(id);
    }

    @MutationMapping
    public AddressView createAddress(@Argument Long customerId, @Argument AddressInput input) {
        return customerService.addAddress(customerId, input);
    }

    @MutationMapping
    public boolean deleteAddress(@Argument Long id) {
        return customerService.deleteAddress(id);
    }

    @BatchMapping(typeName = "Customer")
    public Map<CustomerView, List<AddressView>> addresses(List<CustomerView> customers) {
        return customerService.addressesOf(customers);
    }
}
