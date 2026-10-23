package com.allst.es.scenario.product;

import com.allst.es.scenario.common.AggregationReads;
import com.allst.es.scenario.common.IndexSupport;
import com.allst.es.scenario.common.NamedCount;
import com.allst.es.scenario.common.ScenarioIds;
import com.allst.es.scenario.common.ScenarioPages;
import com.allst.es.scenario.common.SearchHitMapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.metrics.Stats;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductSearchService {
    private final ElasticsearchRestTemplate template;
    private final IndexSupport indexSupport;

    public ProductSearchService(ElasticsearchRestTemplate template, IndexSupport indexSupport) {
        this.template = template;
        this.indexSupport = indexSupport;
    }

    public boolean initIndex() {
        return indexSupport.init(ProductDocument.class);
    }

    public int seed() {
        List<ProductDocument> products = Arrays.asList(
                product("product-1", "小米手机 14", "小米", "手机", 3999D, 820, true, "5G", "拍照"),
                product("product-2", "华为手机 Mate", "华为", "手机", 5999D, 640, true, "5G", "商务"),
                product("product-3", "机械键盘 K8", "罗技", "外设", 499D, 1500, true, "办公", "键盘"),
                product("product-4", "Java 编程思想", "机械工业", "图书", 89D, 2100, false, "编程", "Java"),
                product("product-5", "Elasticsearch 权威指南", "电子工业", "图书", 108D, 960, true, "搜索", "Elasticsearch"),
                product("product-6", "手冲咖啡豆", "三顿半", "食品", 68D, 3200, true, "咖啡", "食品"),
                product("product-7", "跑步鞋飞电", "李宁", "服饰", 699D, 540, false, "运动", "跑鞋"),
                product("product-8", "显示器 27 英寸", "戴尔", "外设", 1899D, 410, true, "办公", "屏幕")
        );
        indexSupport.ensure(ProductDocument.class);
        template.save(products);
        indexSupport.refresh(ProductDocument.class);
        return products.size();
    }

    public ProductDocument save(ProductDocument document) {
        if (document == null || !StringUtils.hasText(document.getTitle())) {
            throw new IllegalArgumentException("商品标题不能为空");
        }
        if (document.getPrice() == null) {
            throw new IllegalArgumentException("商品价格不能为空");
        }
        indexSupport.ensure(ProductDocument.class);
        if (!StringUtils.hasText(document.getId())) {
            document.setId(ScenarioIds.next());
        }
        if (document.getSales() == null) {
            document.setSales(0);
        }
        if (document.getOnSale() == null) {
            document.setOnSale(Boolean.FALSE);
        }
        ProductDocument saved = template.save(document);
        indexSupport.refresh(ProductDocument.class);
        return saved;
    }

    public ProductSearchResult search(ProductSearchQuery query) {
        if (query == null) {
            query = new ProductSearchQuery();
        }
        if (query.getMinPrice() != null && query.getMaxPrice() != null && query.getMinPrice() > query.getMaxPrice()) {
            throw new IllegalArgumentException("最低价不能高于最高价");
        }
        indexSupport.ensure(ProductDocument.class);
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(textQuery(query))
                .withFilter(facetFilter(query, true, true, true))
                .withPageable(ScenarioPages.of(query.getPage(), query.getSize()))
                .withTrackTotalHits(Boolean.TRUE)
                .addAggregation(AggregationBuilders.filter("category_facet", facetFilter(query, true, false, true))
                        .subAggregation(AggregationBuilders.terms("category").field("category").size(20)))
                .addAggregation(AggregationBuilders.filter("brand_facet", facetFilter(query, false, true, true))
                        .subAggregation(AggregationBuilders.terms("brand").field("brand").size(20)))
                .addAggregation(AggregationBuilders.filter("price_facet", facetFilter(query, true, true, false))
                        .subAggregation(AggregationBuilders.range("price_range").field("price")
                                .addUnboundedTo("100以下", 100)
                                .addRange("100-499", 100, 500)
                                .addRange("500-1999", 500, 2000)
                                .addUnboundedFrom("2000及以上", 2000))
                        .subAggregation(AggregationBuilders.stats("price_stats").field("price")));
        applySort(builder, query);
        SearchHits<ProductDocument> hits = template.search(builder.build(), ProductDocument.class);

        ProductSearchResult result = new ProductSearchResult();
        result.setTotal(hits.getTotalHits());
        result.setHits(SearchHitMapper.map(hits));
        result.setCategories(AggregationReads.nestedTerms(hits.getAggregations(), "category_facet", "category"));
        result.setBrands(AggregationReads.nestedTerms(hits.getAggregations(), "brand_facet", "brand"));
        result.setPriceRanges(priceRanges(hits));
        fillPriceStats(result, hits);
        return result;
    }

    private QueryBuilder textQuery(ProductSearchQuery query) {
        if (StringUtils.hasText(query.getQ())) {
            return QueryBuilders.multiMatchQuery(query.getQ()).field("title", 2.0f);
        }
        return QueryBuilders.matchAllQuery();
    }

    private QueryBuilder facetFilter(ProductSearchQuery query, boolean brand, boolean category, boolean price) {
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        if (brand && StringUtils.hasText(query.getBrand())) {
            bool.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }
        if (category && StringUtils.hasText(query.getCategory())) {
            bool.filter(QueryBuilders.termQuery("category", query.getCategory()));
        }
        if (price && (query.getMinPrice() != null || query.getMaxPrice() != null)) {
            RangeQueryBuilder range = QueryBuilders.rangeQuery("price");
            if (query.getMinPrice() != null) {
                range.gte(query.getMinPrice());
            }
            if (query.getMaxPrice() != null) {
                range.lte(query.getMaxPrice());
            }
            bool.filter(range);
        }
        if (query.getOnSale() != null) {
            bool.filter(QueryBuilders.termQuery("onSale", query.getOnSale()));
        }
        if (bool.filter().isEmpty()) {
            return QueryBuilders.matchAllQuery();
        }
        return bool;
    }

    private void applySort(NativeSearchQueryBuilder builder, ProductSearchQuery query) {
        String sort = query.getSort();
        if ("priceAsc".equals(sort)) {
            builder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
        } else if ("priceDesc".equals(sort)) {
            builder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        } else if ("sales".equals(sort) || !StringUtils.hasText(query.getQ())) {
            builder.withSort(SortBuilders.fieldSort("sales").order(SortOrder.DESC));
        }
    }

    private List<NamedCount> priceRanges(SearchHits<ProductDocument> hits) {
        List<NamedCount> ranges = new ArrayList<NamedCount>();
        if (hits.getAggregations() == null) {
            return ranges;
        }
        Filter filter = hits.getAggregations().get("price_facet");
        if (filter == null) {
            return ranges;
        }
        Range range = filter.getAggregations().get("price_range");
        if (range == null) {
            return ranges;
        }
        for (Range.Bucket bucket : range.getBuckets()) {
            NamedCount item = new NamedCount();
            item.setKey(bucket.getKeyAsString());
            item.setCount(bucket.getDocCount());
            ranges.add(item);
        }
        return ranges;
    }

    private ProductDocument product(String id, String title, String brand, String category,
                                     double price, int sales, boolean onSale, String tag1, String tag2) {
        List<String> tags = new ArrayList<String>();
        tags.add(tag1);
        tags.add(tag2);
        return ProductDocument.builder()
                .id(id)
                .title(title)
                .brand(brand)
                .category(category)
                .price(price)
                .sales(sales)
                .onSale(onSale)
                .tags(tags)
                .build();
    }

    private void fillPriceStats(ProductSearchResult result, SearchHits<ProductDocument> hits) {
        if (hits.getAggregations() == null) {
            return;
        }
        Filter filter = hits.getAggregations().get("price_facet");
        if (filter == null) {
            return;
        }
        Stats stats = filter.getAggregations().get("price_stats");
        if (stats == null || stats.getCount() == 0) {
            return;
        }
        result.setMinPrice(stats.getMin());
        result.setMaxPrice(stats.getMax());
        result.setAvgPrice(stats.getAvg());
    }
}
