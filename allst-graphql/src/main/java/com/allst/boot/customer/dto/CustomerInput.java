package com.allst.boot.customer.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CustomerInput {

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
}
