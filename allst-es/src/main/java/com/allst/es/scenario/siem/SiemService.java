package com.allst.es.scenario.siem;

import com.allst.es.scenario.common.AggregationReads;
import com.allst.es.scenario.common.EsQuerySupport;
import com.allst.es.scenario.common.IndexSupport;
import com.allst.es.scenario.common.NamedCount;
import com.allst.es.scenario.common.ScenarioIds;
import com.allst.es.scenario.common.ScenarioPages;
import com.allst.es.scenario.common.ScenarioTimes;
import com.allst.es.scenario.common.SearchPage;
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

@Service
public class SiemService {
    private final ElasticsearchRestTemplate template;
    private final IndexSupport indexSupport;
    private final SiemRuleEngine ruleEngine = new SiemRuleEngine();

    public SiemService(ElasticsearchRestTemplate template, IndexSupport indexSupport) {
        this.template = template;
        this.indexSupport = indexSupport;
    }

    public boolean initIndex() {
        return indexSupport.init(SecurityEventDocument.class);
    }

    public int seed() {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<SecurityEventDocument> events = new ArrayList<SecurityEventDocument>();
        for (int i = 0; i < 6; i++) {
            events.add(event("siem-brute-" + i, now.minusMinutes(30 - i), "login_failure", "medium",
                    "198.51.100.23", "10.0.0.8", null, 443, "bob", "auth-1",
                    "login", "failure", "auth-login", "控制台登录失败"));
        }
        events.add(event("siem-login-ok", now.minusMinutes(20), "login_success", "low",
                "198.51.100.40", "10.0.0.8", null, 443, "carol", "auth-1",
                "login", "success", "auth-login", "控制台登录成功"));
        int[] ports = {22, 80, 443, 3306, 6379, 8080, 9200, 27017};
        for (int i = 0; i < ports.length; i++) {
            events.add(event("siem-scan-" + i, now.minusMinutes(15 - i), "port_scan", "high",
                    "203.0.113.10", "10.0.0.20", 50000 + i, ports[i], null, "edge-1",
                    "connect", "failure", "net-scan", "边缘主机记录到一次端口访问"));
        }
        for (int i = 0; i < 3; i++) {
            events.add(event("siem-priv-" + i, now.minusMinutes(10 - i), "privilege_change", "critical",
                    "10.1.1.5", "10.0.0.30", null, 443, "alice", "iam-1",
                    "grant", "success", "iam-grant", "账号权限发生变更"));
        }
        events.add(event("siem-file", now.minusMinutes(5), "malware_detect", "high",
                "10.2.2.9", "10.0.0.40", null, null, "dave", "endpoint-3",
                "detect", "success", "edr-hash", "终端检测到与已知恶意哈希匹配的文件"));
        indexSupport.ensure(SecurityEventDocument.class);
        template.save(events);
        indexSupport.refresh(SecurityEventDocument.class);
        return events.size();
    }

    public SecurityEventDocument ingest(SecurityEventDocument event) {
        if (event == null || !StringUtils.hasText(event.getEventType())) {
            throw new IllegalArgumentException("事件类型不能为空");
        }
        indexSupport.ensure(SecurityEventDocument.class);
        if (!StringUtils.hasText(event.getId())) {
            event.setId(ScenarioIds.next());
        }
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now().withNano(0));
        }
        if (!StringUtils.hasText(event.getSeverity())) {
            event.setSeverity("low");
        }
        SecurityEventDocument saved = template.save(event);
        indexSupport.refresh(SecurityEventDocument.class);
        return saved;
    }

    public SearchPage<SecurityEventDocument> search(String q, String eventType, String severity, String srcIp,
                                                     String userName, String from, String to,
                                                     Integer page, Integer size) {
        indexSupport.ensure(SecurityEventDocument.class);
        LocalDateTime[] window = ScenarioTimes.window(from, to, 1);
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        if (StringUtils.hasText(q)) {
            bool.must(QueryBuilders.matchQuery("message", q));
        } else {
            bool.must(QueryBuilders.matchAllQuery());
        }
        term(bool, "eventType", eventType);
        term(bool, "severity", severity);
        term(bool, "srcIp", srcIp);
        term(bool, "userName", userName);
        EsQuerySupport.filterTime(bool, "eventTime", window[0], window[1]);
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withPageable(ScenarioPages.of(page, size))
                .withTrackTotalHits(Boolean.TRUE)
                .withSort(org.elasticsearch.search.sort.SortBuilders.fieldSort("eventTime")
                        .order(org.elasticsearch.search.sort.SortOrder.DESC));
        return SearchPage.of(template.search(builder.build(), SecurityEventDocument.class));
    }

    public SiemDashboard dashboard(String from, String to, String interval,
                                   Integer bruteForceThreshold, Integer portScanThreshold, Integer privilegeThreshold) {
        indexSupport.ensure(SecurityEventDocument.class);
        LocalDateTime[] window = ScenarioTimes.window(from, to, 1);
        String chosen = ScenarioTimes.chooseInterval(interval, window[0], window[1]);
        DateHistogramInterval histogramInterval = "day".equals(chosen)
                ? DateHistogramInterval.DAY : DateHistogramInterval.HOUR;

        BoolQueryBuilder bool = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        EsQuerySupport.filterTime(bool, "eventTime", window[0], window[1]);
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withPageable(PageRequest.of(0, 1))
                .withTrackTotalHits(Boolean.TRUE)
                .addAggregation(AggregationBuilders.terms("by_event").field("eventType").size(20))
                .addAggregation(AggregationBuilders.terms("by_severity").field("severity").size(10))
                .addAggregation(AggregationBuilders.filter("brute", QueryBuilders.termQuery("eventType", "login_failure"))
                        .subAggregation(AggregationBuilders.terms("by_subject").field("srcIp").size(50)))
                .addAggregation(AggregationBuilders.filter("scan", QueryBuilders.termQuery("eventType", "port_scan"))
                        .subAggregation(AggregationBuilders.terms("by_subject").field("srcIp").size(50)
                                .subAggregation(AggregationBuilders.cardinality("metric").field("destPort"))))
                .addAggregation(AggregationBuilders.filter("privilege", QueryBuilders.termQuery("eventType", "privilege_change"))
                        .subAggregation(AggregationBuilders.terms("by_subject").field("userName").size(50)))
                .addAggregation(AggregationBuilders.dateHistogram("trend")
                        .field("eventTime")
                        .calendarInterval(histogramInterval)
                        .format("yyyy-MM-dd HH:mm:ss")
                        .minDocCount(1));

        SearchHits<SecurityEventDocument> hits = template.search(builder.build(), SecurityEventDocument.class);
        List<NamedCount> brute = AggregationReads.nestedTerms(hits.getAggregations(), "brute", "by_subject");
        List<NamedCount> scan = AggregationReads.nestedTerms(hits.getAggregations(), "scan", "by_subject");
        List<NamedCount> privilege = AggregationReads.nestedTerms(hits.getAggregations(), "privilege", "by_subject");

        SiemDashboard dashboard = new SiemDashboard();
        dashboard.setFrom(ScenarioTimes.format(window[0]));
        dashboard.setTo(ScenarioTimes.format(window[1]));
        dashboard.setInterval(chosen);
        dashboard.setTotal(hits.getTotalHits());
        dashboard.setByEventType(AggregationReads.terms(hits.getAggregations(), "by_event"));
        dashboard.setBySeverity(AggregationReads.terms(hits.getAggregations(), "by_severity"));
        dashboard.setTrend(AggregationReads.histogram(hits.getAggregations(), "trend"));
        dashboard.setAlerts(ruleEngine.evaluate(
                brute, value(bruteForceThreshold),
                scan, value(portScanThreshold),
                privilege, value(privilegeThreshold)));
        return dashboard;
    }

    private int value(Integer threshold) {
        return threshold == null ? 0 : threshold;
    }

    private void term(BoolQueryBuilder bool, String field, String value) {
        if (StringUtils.hasText(value)) {
            bool.filter(QueryBuilders.termQuery(field, value));
        }
    }

    private SecurityEventDocument event(String id, LocalDateTime time, String type, String severity,
                                         String srcIp, String destIp, Integer srcPort, Integer destPort,
                                         String userName, String host, String action, String outcome,
                                         String ruleId, String message) {
        return SecurityEventDocument.builder()
                .id(id)
                .eventTime(time)
                .eventType(type)
                .severity(severity)
                .srcIp(srcIp)
                .destIp(destIp)
                .srcPort(srcPort)
                .destPort(destPort)
                .userName(userName)
                .host(host)
                .action(action)
                .outcome(outcome)
                .ruleId(ruleId)
                .message(message)
                .build();
    }
}
