package com.allst.boot.guide.web;

import com.allst.boot.guide.entity.Product;
import com.allst.boot.guide.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/guide/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> search(@RequestParam(defaultValue = "") String name) {
        if (name.isBlank()) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContaining(name);
    }
}
