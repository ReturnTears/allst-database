package com.allst.boot.catalog.service;

import com.allst.boot.catalog.dto.CategoryInput;
import com.allst.boot.catalog.dto.CategoryView;
import com.allst.boot.catalog.dto.ProductFilter;
import com.allst.boot.catalog.dto.ProductInput;
import com.allst.boot.catalog.dto.ProductView;
import com.allst.boot.catalog.entity.Category;
import com.allst.boot.catalog.entity.Product;
import com.allst.boot.catalog.repo.CategoryRepository;
import com.allst.boot.catalog.repo.ProductRepository;
import com.allst.boot.catalog.repo.ProductSpecs;
import com.allst.boot.common.ApiException;
import com.allst.boot.common.InputValidator;
import com.allst.boot.common.PageInput;
import com.allst.boot.common.PageRequests;
import com.allst.boot.common.Slice;
import com.allst.boot.order.dto.OrderItemView;
import com.allst.boot.order.repo.OrderItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final InputValidator validator;

    public CatalogService(CategoryRepository categoryRepository,
                          ProductRepository productRepository,
                          OrderItemRepository orderItemRepository,
                          InputValidator validator) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.validator = validator;
    }

    public CategoryView getCategory(Long id) {
        return toCategoryView(loadCategory(id));
    }

    public List<CategoryView> categories() {
        List<CategoryView> views = new ArrayList<CategoryView>();
        for (Category category : categoryRepository.findAllByOrderByIdAsc()) {
            views.add(toCategoryView(category));
        }
        return views;
    }

    public ProductView getProduct(Long id) {
        return toProductView(loadProduct(id));
    }

    public Slice<ProductView> products(ProductFilter filter, PageInput page) {
        Page<Product> result = productRepository.findAll(ProductSpecs.by(filter), PageRequests.of(page));
        List<ProductView> views = new ArrayList<ProductView>();
        for (Product product : result.getContent()) {
            views.add(toProductView(product));
        }
        return Slice.from(result, views);
    }

    public Product loadProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> ApiException.notFound("商品不存在"));
    }

    public Category loadCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> ApiException.notFound("分类不存在"));
    }

    public Map<ProductView, CategoryView> categoriesOf(List<ProductView> products) {
        Map<ProductView, CategoryView> result = new HashMap<ProductView, CategoryView>();
        if (products.isEmpty()) {
            return result;
        }
        Set<Long> ids = new HashSet<Long>();
        for (ProductView product : products) {
            ids.add(product.getCategoryId());
        }
        Map<Long, CategoryView> byId = new HashMap<Long, CategoryView>();
        for (Category category : categoryRepository.findAllById(ids)) {
            byId.put(category.getId(), toCategoryView(category));
        }
        for (ProductView product : products) {
            CategoryView category = byId.get(product.getCategoryId());
            if (category == null) {
                throw ApiException.notFound("分类不存在");
            }
            result.put(product, category);
        }
        return result;
    }

    public Map<CategoryView, CategoryView> parentsOf(List<CategoryView> categories) {
        Map<CategoryView, CategoryView> result = new HashMap<CategoryView, CategoryView>();
        if (categories.isEmpty()) {
            return result;
        }
        Set<Long> ids = new HashSet<Long>();
        for (CategoryView category : categories) {
            if (category.getParentId() != null) {
                ids.add(category.getParentId());
            }
        }
        Map<Long, CategoryView> byId = new HashMap<Long, CategoryView>();
        if (!ids.isEmpty()) {
            for (Category category : categoryRepository.findAllById(ids)) {
                byId.put(category.getId(), toCategoryView(category));
            }
        }
        for (CategoryView category : categories) {
            if (category.getParentId() != null) {
                result.put(category, byId.get(category.getParentId()));
            }
        }
        return result;
    }

    public Map<CategoryView, List<CategoryView>> childrenOf(List<CategoryView> categories) {
        Map<CategoryView, List<CategoryView>> result = new HashMap<CategoryView, List<CategoryView>>();
        if (categories.isEmpty()) {
            return result;
        }
        List<Long> ids = new ArrayList<Long>();
        for (CategoryView category : categories) {
            ids.add(category.getId());
            result.put(category, new ArrayList<CategoryView>());
        }
        for (Category child : categoryRepository.findByParent_IdIn(ids)) {
            CategoryView parent = findCategory(categories, child.getParent().getId());
            if (parent != null) {
                result.get(parent).add(toCategoryView(child));
            }
        }
        return result;
    }

    public Map<CategoryView, Integer> productCounts(List<CategoryView> categories) {
        Map<CategoryView, Integer> result = new HashMap<CategoryView, Integer>();
        if (categories.isEmpty()) {
            return result;
        }
        List<Long> ids = new ArrayList<Long>();
        for (CategoryView category : categories) {
            ids.add(category.getId());
            result.put(category, 0);
        }
        Map<Long, Integer> counts = new HashMap<Long, Integer>();
        for (Object[] row : productRepository.countGrouped(ids)) {
            counts.put((Long) row[0], ((Number) row[1]).intValue());
        }
        for (CategoryView category : categories) {
            Integer count = counts.get(category.getId());
            result.put(category, count == null ? 0 : count);
        }
        return result;
    }

    public Map<Long, ProductView> productsByIds(Collection<Long> ids) {
        Map<Long, ProductView> result = new HashMap<Long, ProductView>();
        if (ids.isEmpty()) {
            return result;
        }
        for (Product product : productRepository.findAllById(ids)) {
            result.put(product.getId(), toProductView(product));
        }
        return result;
    }

    public Map<OrderItemView, ProductView> productsOf(List<OrderItemView> items) {
        Map<OrderItemView, ProductView> result = new HashMap<OrderItemView, ProductView>();
        if (items.isEmpty()) {
            return result;
        }
        Set<Long> ids = new HashSet<Long>();
        for (OrderItemView item : items) {
            if (item.getProductId() != null) {
                ids.add(item.getProductId());
            }
        }
        Map<Long, ProductView> byId = productsByIds(ids);
        for (OrderItemView item : items) {
            if (item.getProductId() != null && byId.containsKey(item.getProductId())) {
                result.put(item, byId.get(item.getProductId()));
            }
        }
        return result;
    }

    @Transactional
    public CategoryView createCategory(CategoryInput input) {
        validator.validate(input);
        String name = input.getName().trim();
        if (categoryRepository.existsByName(name)) {
            throw ApiException.conflict("分类名称已存在");
        }
        Category category = new Category();
        category.setName(name);
        category.setParent(resolveParent(null, input.getParentId()));
        return toCategoryView(categoryRepository.save(category));
    }

    @Transactional
    public CategoryView updateCategory(Long id, CategoryInput input) {
        validator.validate(input);
        Category category = loadCategory(id);
        String name = input.getName().trim();
        if (categoryRepository.existsByNameAndIdNot(name, id)) {
            throw ApiException.conflict("分类名称已存在");
        }
        category.setName(name);
        category.setParent(resolveParent(id, input.getParentId()));
        return toCategoryView(category);
    }

    @Transactional
    public boolean deleteCategory(Long id) {
        loadCategory(id);
        if (categoryRepository.existsByParent_Id(id)) {
            throw ApiException.conflict("请先删除下级分类");
        }
        if (productRepository.existsByCategory_Id(id)) {
            throw ApiException.conflict("分类下还有商品，不能删除");
        }
        categoryRepository.deleteById(id);
        return true;
    }

    @Transactional
    public ProductView createProduct(ProductInput input) {
        validator.validate(input);
        Product product = new Product();
        apply(product, input);
        product.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return toProductView(productRepository.save(product));
    }

    @Transactional
    public ProductView updateProduct(Long id, ProductInput input) {
        validator.validate(input);
        Product product = loadProduct(id);
        apply(product, input);
        return toProductView(product);
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        loadProduct(id);
        if (orderItemRepository.existsByProduct_Id(id)) {
            throw ApiException.conflict("商品已被订单引用，不能删除");
        }
        productRepository.deleteById(id);
        return true;
    }

    private void apply(Product product, ProductInput input) {
        product.setName(input.getName().trim());
        product.setDescription(blankToNull(input.getDescription()));
        product.setPrice(input.getPrice());
        product.setStock(input.getStock());
        product.setStatus(input.getStatus());
        product.setCategory(loadCategory(input.getCategoryId()));
    }

    private Category resolveParent(Long selfId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw ApiException.invalid("不能把分类设成自己的上级");
        }
        Long cursor = parentId;
        int guard = 0;
        while (cursor != null && guard < 20) {
            if (selfId != null && selfId.equals(cursor)) {
                throw ApiException.invalid("不能把分类移动到自己的下级");
            }
            Category current = loadCategory(cursor);
            cursor = current.getParent() == null ? null : current.getParent().getId();
            guard++;
        }
        return loadCategory(parentId);
    }

    private CategoryView findCategory(List<CategoryView> categories, Long id) {
        for (CategoryView category : categories) {
            if (category.getId().equals(id)) {
                return category;
            }
        }
        return null;
    }

    private CategoryView toCategoryView(Category category) {
        CategoryView view = new CategoryView();
        view.setId(category.getId());
        view.setName(category.getName());
        view.setParentId(category.getParent() == null ? null : category.getParent().getId());
        return view;
    }

    private ProductView toProductView(Product product) {
        ProductView view = new ProductView();
        view.setId(product.getId());
        view.setCategoryId(product.getCategory().getId());
        view.setName(product.getName());
        view.setDescription(product.getDescription());
        view.setPrice(product.getPrice());
        view.setStock(product.getStock());
        view.setStatus(product.getStatus());
        view.setCreatedAt(product.getCreatedAt());
        return view;
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
