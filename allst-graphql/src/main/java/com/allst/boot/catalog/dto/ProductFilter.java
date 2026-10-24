package com.allst.boot.catalog.dto;

import com.allst.boot.catalog.enums.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductFilter {
    private String keyword;
    private Long categoryId;
    private ProductStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
