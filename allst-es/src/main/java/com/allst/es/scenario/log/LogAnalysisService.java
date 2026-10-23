package com.allst.es.scenario.log;

import com.allst.es.scenario.common.AggregationReads;
import com.allst.es.scenario.common.EsQuerySupport;
import com.allst.es.scenario.common.IndexSupport;
import com.allst.es.scenario.common.ScenarioIds;
import com.allst.es.scenario.common.ScenarioPages;
import com.allst.es.scenario.common.ScenarioTimes;
import com.allst.es.scenario.common.SearchPage;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.metrics.Percentiles;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogAnalysisService {
    private final ElasticsearchRestTemplate template;
    private final IndexSupport indexSupport;

    public LogAnalysisService(ElasticsearchRestTemplate template, IndexSupport indexSupport) {
        this.template = template;
        this.indexSupport = indexSupport;
    }

    public boolean initIndex() {
        return indexSupport.init(AppLogDocument.class);
    }

    public int seed() {
        LocalDateTime end = LocalDateTime.now().withNano(0);
        List<AppLogDocument> logs = new ArrayList<AppLogDocument>();
        String[] services = {"gateway", "order", "payment", "user"};
        for (int i = 0; i < 36; i++) {
            AppLogDocument log = new AppLogDocument();
            log.setId("app-log-" + i);
            log.setTimestamp(end.minusMinutes(36 - i));
            log.setService(services[i % services.length]);
            log.setHost("host-" + (i % 3));
            log.setTraceId("trace-" + (i / 3));
            log.setCostMs(20 + (i % 7) * 30);
            log.setStatusCode(200);
            log.setLevel("INFO");
            log.setEvent("REQUEST");
            log.setMessage(log.getService() + " 处理请求完成");
            if (i % 9 == 0) {
                log.setLevel("ERROR");
                log.setEvent("PAY_FAIL");
                log.setStatusCode(500);
                log.setCostMs(800 + i);
                log.setMessage("支付服务调用超时，订单未完成扣款");
            } else if (i % 5 == 0) {
                log.setLevel("WARN");
                log.setEvent("DB_SLOW");
                log.setCostMs(450 + i);
                log.setMessage("数据库慢查询，已超过内部耗时阈值");
            }
            logs.add(log);
        }
        indexSupport.ensure(AppLogDocument.class);
        template.save(logs);
        indexSupport.refresh(AppLogDocument.class);
        return logs.size();
    }

    public AppLogDocument ingest(AppLogDocument log) {
        if (log == null || !StringUtils.hasText(log.getMessage())) {
            throw new IllegalArgumentException("日志内容不能为空");
        }
        indexSupport.ensure(AppLogDocument.class);
        if (!StringUtils.hasText(log.getId())) {
            log.setId(ScenarioIds.next());
        }
        if (!StringUtils.hasText(log.getLevel())) {
            log.setLevel("INFO");
        }
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now().withNano(0));
        }
        AppLogDocument saved = template.save(log);
        indexSupport.refresh(AppLogDocument.class);
        return saved;
    }

    public SearchPage<AppLogDocument> search(String q, String level, String service, String event,
                                              String from, String to, Integer page, Integer size) {
        indexSupport.ensure(AppLogDocument.class);
        LocalDateTime[] window = ScenarioTimes.window(from, to, 1);
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        if (StringUtils.hasText(q)) {
            bool.must(QueryBuilders.matchQuery("message", q));
        } else {
            bool.must(QueryBuilders.matchAllQuery());
        }
        if (StringUtils.hasText(level)) {
            bool.filter(QueryBuilders.termQuery("level", level));
        }
        if (StringUtils.hasText(service)) {
            bool.filter(QueryBuilders.termQuery("service", service));
        }
        if (StringUtils.hasText(event)) {
            bool.filter(QueryBuilders.termQuery("event", event));
        }
        EsQuerySupport.filterTime(bool, "timestamp", window[0], window[1]);
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withPageable(ScenarioPages.of(page, size))
                .withTrackTotalHits(Boolean.TRUE)
                .withSort(org.elasticsearch.search.sort.SortBuilders.fieldSort("timestamp")
                        .order(org.elasticsearch.search.sort.SortOrder.DESC));
        return SearchPage.of(template.search(builder.build(), AppLogDocument.class));
    }

    public LogReport report(String service, String from, String to, String interval) {
        indexSupport.ensure(AppLogDocument.class);
        LocalDateTime[] window = ScenarioTimes.window(from, to, 1);
        String chosen = ScenarioTimes.chooseInterval(interval, window[0], window[1]);
        DateHistogramInterval histogramInterval = "day".equals(chosen)
                ? DateHistogramInterval.DAY : DateHistogramInterval.HOUR;

        BoolQueryBuilder bool = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        if (StringUtils.hasText(service)) {
            bool.filter(QueryBuilders.termQuery("service", service));
        }
        EsQuerySupport.filterTime(bool, "timestamp", window[0], window[1]);
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withPageable(PageRequest.of(0, 1))
                .withTrackTotalHits(Boolean.TRUE)
                .addAggregation(AggregationBuilders.filter("errors", QueryBuilders.termQuery("level", "ERROR")))
                .addAggregation(AggregationBuilders.terms("by_level").field("level").size(10))
                .addAggregation(AggregationBuilders.terms("by_service").field("service").size(10))
                .addAggregation(AggregationBuilders.terms("top_events").field("event").size(10))
                .addAggregation(AggregationBuilders.percentiles("cost").field("costMs").percentiles(50, 95, 99))
                .addAggregation(AggregationBuilders.dateHistogram("trend")
                        .field("timestamp")
                        .calendarInterval(histogramInterval)
                        .format("yyyy-MM-dd HH:mm:ss")
                        .minDocCount(1));

        SearchHits<AppLogDocument> hits = template.search(builder.build(), AppLogDocument.class);
        long errorCount = AggregationReads.filterCount(hits.getAggregations(), "errors");
        LogReport report = new LogReport();
        report.setFrom(ScenarioTimes.format(window[0]));
        report.setTo(ScenarioTimes.format(window[1]));
        report.setInterval(chosen);
        report.setTotal(hits.getTotalHits());
        report.setErrorCount(errorCount);
        report.setErrorRate(hits.getTotalHits() == 0 ? 0D : (double) errorCount / hits.getTotalHits());
        report.setByLevel(AggregationReads.terms(hits.getAggregations(), "by_level"));
        report.setByService(AggregationReads.terms(hits.getAggregations(), "by_service"));
        report.setTopEvents(AggregationReads.terms(hits.getAggregations(), "top_events"));
        report.setTrend(AggregationReads.histogram(hits.getAggregations(), "trend"));
        fillPercentiles(report, hits);
        return report;
    }

    private void fillPercentiles(LogReport report, SearchHits<AppLogDocument> hits) {
        if (hits.getAggregations() == null) {
            return;
        }
        Percentiles percentiles = hits.getAggregations().get("cost");
        if (percentiles == null) {
            return;
        }
        report.setCostP50(finite(percentiles.percentile(50)));
        report.setCostP95(finite(percentiles.percentile(95)));
        report.setCostP99(finite(percentiles.percentile(99)));
    }

    private double finite(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0D;
        }
        return value;
    }
}
