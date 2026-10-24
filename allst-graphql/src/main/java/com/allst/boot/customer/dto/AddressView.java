package com.allst.boot.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressView {
    private Long id;
    private Long customerId;
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String detail;
    private boolean defaultAddress;
}
