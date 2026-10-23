package com.allst.es.scenario.geo;

import com.allst.es.scenario.common.IndexSupport;
import com.allst.es.scenario.common.ScenarioHit;
import com.allst.es.scenario.common.ScenarioIds;
import com.allst.es.scenario.common.ScenarioPages;
import com.allst.es.scenario.common.SearchHitMapper;
import com.allst.es.scenario.common.SearchPage;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GeoSearchService {
    private final ElasticsearchRestTemplate template;
    private final IndexSupport indexSupport;

    public GeoSearchService(ElasticsearchRestTemplate template, IndexSupport indexSupport) {
        this.template = template;
        this.indexSupport = indexSupport;
    }

    public boolean initIndex() {
        return indexSupport.init(PlaceDocument.class);
    }

    public int seed() {
        List<PlaceDocument> places = Arrays.asList(
                place("place-1", "故宫博物院", "古迹", "北京", "北京市东城区景山前街4号", 39.916345, 116.397155, 4.8),
                place("place-2", "天安门", "地标", "北京", "北京市东城区长安街", 39.905489, 116.397631, 4.7),
                place("place-3", "中国国家博物馆", "古迹", "北京", "北京市东城区东长安街16号", 39.903333, 116.397500, 4.6),
                place("place-4", "颐和园", "公园", "北京", "北京市海淀区新建宫门路19号", 39.999912, 116.275461, 4.7),
                place("place-5", "外滩", "地标", "上海", "上海市黄浦区中山东一路", 31.240000, 121.490000, 4.6),
                place("place-6", "东方明珠", "地标", "上海", "上海市浦东新区世纪大道1号", 31.239666, 121.499809, 4.5),
                place("place-7", "豫园", "古迹", "上海", "上海市黄浦区福佑路168号", 31.227000, 121.492000, 4.4),
                place("place-8", "西湖", "公园", "杭州", "杭州市西湖区龙井路1号", 30.248000, 120.148000, 4.8),
                place("place-9", "雷峰塔", "古迹", "杭州", "杭州市西湖区南山路15号", 30.231000, 120.149000, 4.5),
                place("place-10", "广州塔", "地标", "广州", "广州市海珠区阅江西路222号", 23.106000, 113.324000, 4.6),
                place("place-11", "深圳湾公园", "公园", "深圳", "深圳市南山区滨海大道", 22.517000, 113.990000, 4.5)
        );
        indexSupport.ensure(PlaceDocument.class);
        template.save(places);
        indexSupport.refresh(PlaceDocument.class);
        return places.size();
    }

    public PlaceDocument save(PlaceCommand command) {
        if (command == null || !StringUtils.hasText(command.getName())) {
            throw new IllegalArgumentException("地点名称不能为空");
        }
        if (command.getLat() == null || command.getLon() == null) {
            throw new IllegalArgumentException("经纬度不能为空");
        }
        indexSupport.ensure(PlaceDocument.class);
        PlaceDocument document = PlaceDocument.builder()
                .id(StringUtils.hasText(command.getId()) ? command.getId() : ScenarioIds.next())
                .name(command.getName())
                .category(command.getCategory())
                .city(command.getCity())
                .address(command.getAddress())
                .location(new GeoPoint(command.getLat(), command.getLon()))
                .rating(command.getRating())
                .build();
        PlaceDocument saved = template.save(document);
        indexSupport.refresh(PlaceDocument.class);
        return saved;
    }

    public SearchPage<PlaceDocument> nearby(double lat, double lon, double distanceKm, String category, String q, Integer size) {
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("距离必须大于0");
        }
        indexSupport.ensure(PlaceDocument.class);
        BoolQueryBuilder bool = baseQuery(q, category);
        bool.filter(QueryBuilders.geoDistanceQuery("location")
                .point(lat, lon)
                .distance(distanceKm, DistanceUnit.KILOMETERS));
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withPageable(ScenarioPages.of(0, size))
                .withSort(SortBuilders.geoDistanceSort("location", lat, lon)
                        .unit(DistanceUnit.KILOMETERS)
                        .order(SortOrder.ASC));
        return withDistance(template.search(builder.build(), PlaceDocument.class));
    }

    public SearchPage<PlaceDocument> boundingBox(double topLat, double leftLon, double bottomLat, double rightLon,
                                                  String category, Integer size) {
        if (topLat <= bottomLat) {
            throw new IllegalArgumentException("北纬必须大于南纬");
        }
        indexSupport.ensure(PlaceDocument.class);
        BoolQueryBuilder bool = baseQuery(null, category);
        bool.filter(QueryBuilders.geoBoundingBoxQuery("location")
                .setCorners(topLat, leftLon, bottomLat, rightLon));
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withPageable(ScenarioPages.of(0, size));
        return SearchPage.of(template.search(builder.build(), PlaceDocument.class));
    }

    public List<HeatCell> heatmap(String category, Integer precision) {
        int gridPrecision = precision == null ? 5 : precision;
        if (gridPrecision < 1 || gridPrecision > 8) {
            throw new IllegalArgumentException("geohash 精度范围是 1 到 8");
        }
        indexSupport.ensure(PlaceDocument.class);
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(baseQuery(null, category))
                .withPageable(PageRequest.of(0, 1))
                .addAggregation(AggregationBuilders.geohashGrid("heat")
                        .field("location")
                        .precision(gridPrecision)
                        .size(100));
        SearchHits<PlaceDocument> hits = template.search(builder.build(), PlaceDocument.class);
        List<HeatCell> cells = new ArrayList<HeatCell>();
        if (hits.getAggregations() == null) {
            return cells;
        }
        Aggregation aggregation = hits.getAggregations().get("heat");
        if (!(aggregation instanceof GeoGrid)) {
            return cells;
        }
        for (GeoGrid.Bucket bucket : ((GeoGrid) aggregation).getBuckets()) {
            HeatCell cell = new HeatCell();
            cell.setGeohash(bucket.getKeyAsString());
            cell.setCount(bucket.getDocCount());
            cells.add(cell);
        }
        return cells;
    }

    private BoolQueryBuilder baseQuery(String q, String category) {
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        if (StringUtils.hasText(q)) {
            bool.must(QueryBuilders.multiMatchQuery(q).field("name", 2.0f).field("address"));
        } else {
            bool.must(QueryBuilders.matchAllQuery());
        }
        if (StringUtils.hasText(category)) {
            bool.filter(QueryBuilders.termQuery("category", category));
        }
        return bool;
    }

    private SearchPage<PlaceDocument> withDistance(SearchHits<PlaceDocument> hits) {
        List<ScenarioHit<PlaceDocument>> mapped = new ArrayList<ScenarioHit<PlaceDocument>>();
        for (SearchHit<PlaceDocument> hit : hits.getSearchHits()) {
            mapped.add(SearchHitMapper.one(hit, SearchHitMapper.distanceOf(hit)));
        }
        SearchPage<PlaceDocument> page = new SearchPage<PlaceDocument>();
        page.setTotal(hits.getTotalHits());
        page.setHits(mapped);
        return page;
    }

    private PlaceDocument place(String id, String name, String category, String city, String address,
                                double lat, double lon, double rating) {
        return PlaceDocument.builder()
                .id(id)
                .name(name)
                .category(category)
                .city(city)
                .address(address)
                .location(new GeoPoint(lat, lon))
                .rating(rating)
                .build();
    }
}
