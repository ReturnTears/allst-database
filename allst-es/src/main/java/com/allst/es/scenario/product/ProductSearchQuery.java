package com.allst.es.scenario.product;

import lombok.Data;

@Data
public class ProductSearchQuery {
    private String q;
    private String brand;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private Boolean onSale;
    private String sort;
    private Integer page;
    private Integer size;
}
