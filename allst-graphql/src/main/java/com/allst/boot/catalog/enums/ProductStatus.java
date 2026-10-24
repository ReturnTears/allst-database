package com.allst.boot.catalog.enums;

public enum ProductStatus {
    ON_SALE("在售"),
    OFF_SALE("已下架");

    private final String label;

    ProductStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
