package com.allst.boot.customer.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CreateCustomerInput {

    @NotBlank(message = "不能为空")
    @Size(max = 64, message = "最长 64")
    private String name;

    @NotBlank(message = "不能为空")
    @Email(message = "格式不正确")
    @Size(max = 128, message = "最长 128")
    private String email;

    @NotBlank(message = "不能为空")
    @Size(max = 32, message = "最长 32")
    private String phone;

    @NotNull(message = "不能为空")
    @Valid
    private AddressInput address;
}
