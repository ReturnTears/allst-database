package com.allst.boot.catalog.dto;

import com.allst.boot.catalog.enums.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductInput {

    @NotBlank(message = "不能为空")
    @Size(max = 80, message = "最长 80")
    private String name;

    @Size(max = 500, message = "最长 500")
    private String description;

    @NotNull(message = "不能为空")
    @DecimalMin(value = "0.01", message = "至少 0.01")
    @Digits(integer = 10, fraction = 2, message = "最多两位小数")
    private BigDecimal price;

    @NotNull(message = "不能为空")
    @Min(value = 0, message = "不能为负")
    @Max(value = 1000000, message = "过大")
    private Integer stock;

    @NotNull(message = "不能为空")
    private ProductStatus status;

    @NotNull(message = "不能为空")
    private Long categoryId;
}
