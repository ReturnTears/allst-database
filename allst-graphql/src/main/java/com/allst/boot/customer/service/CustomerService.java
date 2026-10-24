package com.allst.boot.customer.service;

import com.allst.boot.common.ApiException;
import com.allst.boot.common.InputValidator;
import com.allst.boot.common.PageInput;
import com.allst.boot.common.PageRequests;
import com.allst.boot.common.Slice;
import com.allst.boot.customer.dto.AddressInput;
import com.allst.boot.customer.dto.AddressView;
import com.allst.boot.customer.dto.CreateCustomerInput;
import com.allst.boot.customer.dto.CustomerInput;
import com.allst.boot.customer.dto.CustomerView;
import com.allst.boot.customer.entity.Address;
import com.allst.boot.customer.entity.Customer;
import com.allst.boot.customer.repo.AddressRepository;
import com.allst.boot.customer.repo.CustomerRepository;
import com.allst.boot.order.repo.CustomerOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CustomerOrderRepository orderRepository;
    private final InputValidator validator;

    public CustomerService(CustomerRepository customerRepository,
                           AddressRepository addressRepository,
                           CustomerOrderRepository orderRepository,
                           InputValidator validator) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.validator = validator;
    }

    public CustomerView get(Long id) {
        return toView(load(id));
    }

    public Slice<CustomerView> page(PageInput page) {
        Page<Customer> result = customerRepository.findAll(PageRequests.of(page));
        List<CustomerView> views = new ArrayList<CustomerView>();
        for (Customer customer : result.getContent()) {
            views.add(toView(customer));
        }
        return Slice.from(result, views);
    }

    public Customer load(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> ApiException.notFound("客户不存在"));
    }

    public Address loadAddress(Long id) {
        return addressRepository.findById(id).orElseThrow(() -> ApiException.notFound("地址不存在"));
    }

    public AddressView defaultAddress(Long customerId) {
        return addressRepository.findByCustomer_IdAndDefaultAddress(customerId, true)
                .map(this::toAddressView)
                .orElseThrow(() -> ApiException.notFound("客户还没有默认地址"));
    }

    public Map<CustomerView, List<AddressView>> addressesOf(List<CustomerView> customers) {
        Map<CustomerView, List<AddressView>> result = new HashMap<CustomerView, List<AddressView>>();
        if (customers.isEmpty()) {
            return result;
        }
        List<Long> ids = new ArrayList<Long>();
        for (CustomerView customer : customers) {
            ids.add(customer.getId());
            result.put(customer, new ArrayList<AddressView>());
        }
        for (Address address : addressRepository.findByCustomer_IdIn(ids)) {
            CustomerView key = findCustomer(customers, address.getCustomer().getId());
            if (key != null) {
                result.get(key).add(toAddressView(address));
            }
        }
        return result;
    }

    public Map<Long, CustomerView> customersByIds(Collection<Long> ids) {
        Map<Long, CustomerView> result = new HashMap<Long, CustomerView>();
        if (ids.isEmpty()) {
            return result;
        }
        for (Customer customer : customerRepository.findAllById(ids)) {
            result.put(customer.getId(), toView(customer));
        }
        return result;
    }

    @Transactional
    public CustomerView create(CreateCustomerInput input) {
        validator.validate(input);
        String email = normalizeEmail(input.getEmail());
        if (customerRepository.existsByEmail(email)) {
            throw ApiException.conflict("邮箱已存在");
        }
        Customer customer = new Customer();
        customer.setName(input.getName().trim());
        customer.setEmail(email);
        customer.setPhone(input.getPhone().trim());
        customer.setCreatedAt(now());
        customerRepository.save(customer);
        saveAddress(customer, input.getAddress(), true);
        return toView(customer);
    }

    @Transactional
    public CustomerView update(Long id, CustomerInput input) {
        validator.validate(input);
        Customer customer = load(id);
        String email = normalizeEmail(input.getEmail());
        customerRepository.findByEmail(email)
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw ApiException.conflict("邮箱已存在");
                });
        customer.setName(input.getName().trim());
        customer.setEmail(email);
        customer.setPhone(input.getPhone().trim());
        return toView(customer);
    }

    @Transactional
    public boolean delete(Long id) {
        Customer customer = load(id);
        if (orderRepository.existsByCustomer_Id(id)) {
            throw ApiException.conflict("客户已有订单，不能删除");
        }
        addressRepository.deleteByCustomer_Id(id);
        customerRepository.deleteById(customer.getId());
        return true;
    }

    @Transactional
    public AddressView addAddress(Long customerId, AddressInput input) {
        validator.validate(input);
        Customer customer = load(customerId);
        boolean first = addressRepository.findByCustomer_IdOrderByIdAsc(customerId).isEmpty();
        return toAddressView(saveAddress(customer, input, first));
    }

    @Transactional
    public boolean deleteAddress(Long id) {
        Address address = loadAddress(id);
        long customerId = address.getCustomer().getId();
        List<Address> left = addressRepository.findByCustomer_IdOrderByIdAsc(customerId);
        if (left.size() <= 1) {
            throw ApiException.illegal("至少保留一条收货地址");
        }
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);
        addressRepository.flush();
        if (wasDefault) {
            List<Address> remain = addressRepository.findByCustomer_IdOrderByIdAsc(customerId);
            if (!remain.isEmpty()) {
                remain.get(0).setDefaultAddress(true);
            }
        }
        return true;
    }

    private Address saveAddress(Customer customer, AddressInput input, boolean forceDefault) {
        boolean preferred = forceDefault || Boolean.TRUE.equals(input.getDefaultAddress());
        if (preferred) {
            addressRepository.clearDefault(customer.getId());
        }
        Address address = new Address();
        address.setCustomer(customer);
        address.setReceiver(input.getReceiver().trim());
        address.setPhone(input.getPhone().trim());
        address.setProvince(input.getProvince().trim());
        address.setCity(input.getCity().trim());
        address.setDetail(input.getDetail().trim());
        address.setDefaultAddress(preferred);
        return addressRepository.save(address);
    }

    private CustomerView findCustomer(List<CustomerView> customers, Long id) {
        for (CustomerView customer : customers) {
            if (customer.getId().equals(id)) {
                return customer;
            }
        }
        return null;
    }

    private CustomerView toView(Customer customer) {
        CustomerView view = new CustomerView();
        view.setId(customer.getId());
        view.setName(customer.getName());
        view.setEmail(customer.getEmail());
        view.setPhone(customer.getPhone());
        view.setCreatedAt(customer.getCreatedAt());
        return view;
    }

    private AddressView toAddressView(Address address) {
        AddressView view = new AddressView();
        view.setId(address.getId());
        view.setCustomerId(address.getCustomer().getId());
        view.setReceiver(address.getReceiver());
        view.setPhone(address.getPhone());
        view.setProvince(address.getProvince());
        view.setCity(address.getCity());
        view.setDetail(address.getDetail());
        view.setDefaultAddress(address.isDefaultAddress());
        return view;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
