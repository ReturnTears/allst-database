package com.allst.boot.order.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class OrderLineInput {

    @NotNull(message = "不能为空")
    private Long productId;

    @NotNull(message = "不能为空")
    @Min(value = 1, message = "至少 1")
    @Max(value = 999, message = "过大")
    private Integer quantity;
}
