package com.allst.boot.customer.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerView {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
