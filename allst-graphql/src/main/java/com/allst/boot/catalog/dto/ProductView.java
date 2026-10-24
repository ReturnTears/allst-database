package com.allst.boot.catalog.dto;

import com.allst.boot.catalog.enums.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductView {
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private ProductStatus status;
    private LocalDateTime createdAt;

    public boolean getInStock() {
        return stock != null && stock > 0;
    }
}
