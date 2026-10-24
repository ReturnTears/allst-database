package com.allst.boot.order.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class CreateOrderInput {

    @NotNull(message = "不能为空")
    private Long customerId;

    @NotNull(message = "不能为空")
    private Long addressId;

    @Size(max = 200, message = "最长 200")
    private String remark;

    @NotEmpty(message = "至少一件商品")
    @Valid
    private List<OrderLineInput> items;
}
