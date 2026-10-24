package com.allst.boot.catalog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryView {
    private Long id;
    private Long parentId;
    private String name;
}
