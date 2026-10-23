package com.allst.es.scenario.product;

import com.allst.es.scenario.common.NamedCount;
import com.allst.es.scenario.common.ScenarioHit;
import lombok.Data;

import java.util.List;

@Data
public class ProductSearchResult {
    private long total;
    private List<ScenarioHit<ProductDocument>> hits;
    private List<NamedCount> categories;
    private List<NamedCount> brands;
    private List<NamedCount> priceRanges;
    private Double minPrice;
    private Double maxPrice;
    private Double avgPrice;
}
