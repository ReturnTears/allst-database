package com.allst.es.scenario.product;

import com.allst.es.scenario.common.ScenarioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenario/product")
public class ProductSearchController {
    private final ProductSearchService service;

    public ProductSearchController(ProductSearchService service) {
        this.service = service;
    }

    @PostMapping("/index/init")
    public ScenarioResponse<Boolean> init() {
        return ScenarioResponse.ok(service.initIndex());
    }

    @PostMapping("/seed")
    public ScenarioResponse<Integer> seed() {
        int count = service.seed();
        return ScenarioResponse.ok(count, count);
    }

    @PostMapping("/docs")
    public ScenarioResponse<ProductDocument> save(@RequestBody ProductDocument document) {
        return ScenarioResponse.ok(service.save(document));
    }

    @GetMapping("/search")
    public ScenarioResponse<ProductSearchResult> search(ProductSearchQuery query) {
        ProductSearchResult result = service.search(query);
        return ScenarioResponse.ok(result, result.getTotal());
    }
}
