package com.allst.es.scenario.analytics;

import com.allst.es.scenario.common.AggregationReads;
import com.allst.es.scenario.common.EsQuerySupport;
import com.allst.es.scenario.common.IndexSupport;
import com.allst.es.scenario.common.ScenarioIds;
import com.allst.es.scenario.common.ScenarioTimes;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class RealtimeAnalyticsService {
    private static final String[] STATUSES = {"CREATED", "PAID", "SHIPPED", "CANCELLED"};
    private static final String[] CHANNELS = {"app", "web", "mini"};
    private static final String[] CATEGORIES = {"数码", "图书", "食品", "服饰"};
    private static final String[] PROVINCES = {"北京", "上海", "浙江", "广东", "四川"};

    private final ElasticsearchRestTemplate template;
    private final IndexSupport indexSupport;

    public RealtimeAnalyticsService(ElasticsearchRestTemplate template, IndexSupport indexSupport) {
        this.template = template;
        this.indexSupport = indexSupport;
    }

    public boolean initIndex() {
        return indexSupport.init(OrderEventDocument.class);
    }

    public int seed() {
        Random random = new Random(20261023L);
        LocalDateTime end = LocalDateTime.now().withNano(0);
        List<OrderEventDocument> events = new ArrayList<OrderEventDocument>();
        for (int i = 0; i < 48; i++) {
            OrderEventDocument event = new OrderEventDocument();
            event.setId("order-event-" + i);
            event.setOrderId("NO" + (10000 + i));
            event.setUserId("user-" + (i % 12));
            event.setAmount(20D + random.nextInt(500));
            event.setStatus(STATUSES[i % STATUSES.length]);
            event.setChannel(CHANNELS[i % CHANNELS.length]);
            event.setCategory(CATEGORIES[i % CATEGORIES.length]);
            event.setProvince(PROVINCES[i % PROVINCES.length]);
            event.setEventTime(end.minusHours(47 - i));
            events.add(event);
        }
        indexSupport.ensure(OrderEventDocument.class);
        template.save(events);
        indexSupport.refresh(OrderEventDocument.class);
        return events.size();
    }

    public OrderEventDocument ingest(OrderEventDocument event) {
        if (event == null || event.getAmount() == null) {
            throw new IllegalArgumentException("订单金额不能为空");
        }
        indexSupport.ensure(OrderEventDocument.class);
        if (!StringUtils.hasText(event.getId())) {
            event.setId(ScenarioIds.next());
        }
        if (!StringUtils.hasText(event.getStatus())) {
            event.setStatus("CREATED");
        }
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now().withNano(0));
        }
        OrderEventDocument saved = template.save(event);
        indexSupport.refresh(OrderEventDocument.class);
        return saved;
    }

    public AnalyticsReport report(String from, String to, String interval) {
        indexSupport.ensure(OrderEventDocument.class);
        LocalDateTime[] window = ScenarioTimes.window(from, to, 2);
        String chosen = ScenarioTimes.chooseInterval(interval, window[0], window[1]);
        DateHistogramInterval histogramInterval = "day".equals(chosen)
                ? DateHistogramInterval.DAY : DateHistogramInterval.HOUR;

        BoolQueryBuilder bool = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        EsQuerySupport.filterTime(bool, "eventTime", window[0], window[1]);
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withPageable(PageRequest.of(0, 1))
                .withTrackTotalHits(Boolean.TRUE)
                .addAggregation(AggregationBuilders.sum("sum_amount").field("amount"))
                .addAggregation(AggregationBuilders.avg("avg_amount").field("amount"))
                .addAggregation(AggregationBuilders.cardinality("users").field("userId"))
                .addAggregation(AggregationBuilders.terms("by_status").field("status").size(10))
                .addAggregation(AggregationBuilders.terms("by_channel").field("channel").size(10)
                        .subAggregation(AggregationBuilders.sum("metric").field("amount")))
                .addAggregation(AggregationBuilders.terms("by_category").field("category").size(10)
                        .subAggregation(AggregationBuilders.sum("metric").field("amount")))
                .addAggregation(AggregationBuilders.terms("by_province").field("province").size(10)
                        .subAggregation(AggregationBuilders.sum("metric").field("amount")))
                .addAggregation(AggregationBuilders.dateHistogram("trend")
                        .field("eventTime")
                        .calendarInterval(histogramInterval)
                        .format("yyyy-MM-dd HH:mm:ss")
                        .minDocCount(1)
                        .subAggregation(AggregationBuilders.sum("metric").field("amount")));

        SearchHits<OrderEventDocument> hits = template.search(builder.build(), OrderEventDocument.class);
        AnalyticsReport report = new AnalyticsReport();
        report.setFrom(ScenarioTimes.format(window[0]));
        report.setTo(ScenarioTimes.format(window[1]));
        report.setInterval(chosen);
        report.setTotal(hits.getTotalHits());
        report.setSumAmount(AggregationReads.value(hits.getAggregations(), "sum_amount"));
        report.setAvgAmount(AggregationReads.value(hits.getAggregations(), "avg_amount"));
        report.setUniqueUsers(Math.round(AggregationReads.value(hits.getAggregations(), "users")));
        report.setByStatus(AggregationReads.terms(hits.getAggregations(), "by_status"));
        report.setByChannel(AggregationReads.terms(hits.getAggregations(), "by_channel"));
        report.setByCategory(AggregationReads.terms(hits.getAggregations(), "by_category"));
        report.setByProvince(AggregationReads.terms(hits.getAggregations(), "by_province"));
        report.setTrend(AggregationReads.histogram(hits.getAggregations(), "trend"));
        return report;
    }
}
