package com.allst.boot.customer.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class AddressInput {

    @NotBlank(message = "不能为空")
    @Size(max = 64, message = "最长 64")
    private String receiver;

    @NotBlank(message = "不能为空")
    @Size(max = 32, message = "最长 32")
    private String phone;

    @NotBlank(message = "不能为空")
    @Size(max = 32, message = "最长 32")
    private String province;

    @NotBlank(message = "不能为空")
    @Size(max = 32, message = "最长 32")
    private String city;

    @NotBlank(message = "不能为空")
    @Size(max = 255, message = "最长 255")
    private String detail;

    private Boolean defaultAddress;
}
