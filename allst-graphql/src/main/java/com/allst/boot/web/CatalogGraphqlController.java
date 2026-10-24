package com.allst.boot.web;

import com.allst.boot.catalog.dto.CategoryInput;
import com.allst.boot.catalog.dto.CategoryView;
import com.allst.boot.catalog.dto.ProductFilter;
import com.allst.boot.catalog.dto.ProductInput;
import com.allst.boot.catalog.dto.ProductView;
import com.allst.boot.catalog.service.CatalogService;
import com.allst.boot.common.PageInput;
import com.allst.boot.common.Slice;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class CatalogGraphqlController {

    private final CatalogService catalogService;

    public CatalogGraphqlController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @QueryMapping
    public CategoryView category(@Argument Long id) {
        return catalogService.getCategory(id);
    }

    @QueryMapping
    public List<CategoryView> categories() {
        return catalogService.categories();
    }

    @QueryMapping
    public ProductView product(@Argument Long id) {
        return catalogService.getProduct(id);
    }

    @QueryMapping
    public Slice<ProductView> products(@Argument ProductFilter filter, @Argument PageInput page) {
        return catalogService.products(filter, page);
    }

    @MutationMapping
    public CategoryView createCategory(@Argument CategoryInput input) {
        return catalogService.createCategory(input);
    }

    @MutationMapping
    public CategoryView updateCategory(@Argument Long id, @Argument CategoryInput input) {
        return catalogService.updateCategory(id, input);
    }

    @MutationMapping
    public boolean deleteCategory(@Argument Long id) {
        return catalogService.deleteCategory(id);
    }

    @MutationMapping
    public ProductView createProduct(@Argument ProductInput input) {
        return catalogService.createProduct(input);
    }

    @MutationMapping
    public ProductView updateProduct(@Argument Long id, @Argument ProductInput input) {
        return catalogService.updateProduct(id, input);
    }

    @MutationMapping
    public boolean deleteProduct(@Argument Long id) {
        return catalogService.deleteProduct(id);
    }

    @BatchMapping(typeName = "Product")
    public Map<ProductView, CategoryView> category(List<ProductView> products) {
        return catalogService.categoriesOf(products);
    }

    @BatchMapping(typeName = "Category")
    public Map<CategoryView, CategoryView> parent(List<CategoryView> categories) {
        return catalogService.parentsOf(categories);
    }

    @BatchMapping(typeName = "Category")
    public Map<CategoryView, List<CategoryView>> children(List<CategoryView> categories) {
        return catalogService.childrenOf(categories);
    }

    @BatchMapping(typeName = "Category")
    public Map<CategoryView, Integer> productCount(List<CategoryView> categories) {
        return catalogService.productCounts(categories);
    }
}
