package com.allst.es.scenario.fulltext;

import com.allst.es.scenario.common.EsQuerySupport;
import com.allst.es.scenario.common.IndexSupport;
import com.allst.es.scenario.common.ScenarioIds;
import com.allst.es.scenario.common.ScenarioPages;
import com.allst.es.scenario.common.ScenarioTimes;
import com.allst.es.scenario.common.SearchPage;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FullTextSearchService {
    private final ElasticsearchRestTemplate template;
    private final IndexSupport indexSupport;

    public FullTextSearchService(ElasticsearchRestTemplate template, IndexSupport indexSupport) {
        this.template = template;
        this.indexSupport = indexSupport;
    }

    public boolean initIndex() {
        return indexSupport.init(ArticleDocument.class);
    }

    public int seed() {
        List<ArticleDocument> articles = Arrays.asList(
                article("article-1", "Elasticsearch 全文检索在 Spring Boot 中的实践", "搜索", "林知夏",
                        "介绍 multi_match、高亮和过滤条件如何一起用。",
                        "在 Spring Boot 里做全文检索，标题权重要高于正文。Elasticsearch 的 multi_match 可以同时查标题、摘要和正文，再用 keyword 字段过滤栏目。",
                        "全文检索", "Spring Boot"),
                article("article-2", "用 Elasticsearch 聚合做实时订单分析", "分析", "林知夏",
                        "订单事件写入后立刻聚合。",
                        "实时分析不要把明细拉回应用再计算。按渠道、类目和省份做 terms 聚合，再用 date_histogram 看金额趋势。",
                        "聚合", "订单"),
                article("article-3", "使用 Elasticsearch 检索地理位置", "地理", "周衡",
                        "附近的地点和矩形范围。",
                        "地点索引用 geo_point。附近检索使用 geo_distance，并按距离排序。热力分布可以用 geohash_grid。",
                        "地理", "附近"),
                article("article-4", "应用日志进入 Elasticsearch 之后怎么查", "日志", "周衡",
                        "级别、服务和时间范围一起过滤。",
                        "日志检索把 message 做成 text，把 level、service、event 做成 keyword。错误率和耗时分位数都交给聚合。",
                        "日志", "错误率"),
                article("article-5", "用 Elasticsearch 做安全事件检测", "安全", "陈疏影",
                        "登录失败、端口探测和权限变更。",
                        "安全信息和事件管理先把事件规范化，再按来源 IP 和账号聚合。超过阈值就生成告警，而不是在查询里写攻击过程。",
                        "安全", "告警"),
                article("article-6", "Spring Boot 连接 Elasticsearch 的索引设计规范", "工程", "陈疏影",
                        "text 用来搜，keyword 用来筛。",
                        "索引设计先分清检索字段和聚合字段。中文全文使用 IK 分词，精确过滤使用 keyword。不要在启动阶段强行连接集群。",
                        "规范", "索引")
        );
        return saveAll(articles);
    }

    public ArticleDocument save(ArticleDocument document) {
        if (document == null || !StringUtils.hasText(document.getTitle())) {
            throw new IllegalArgumentException("标题不能为空");
        }
        indexSupport.ensure(ArticleDocument.class);
        if (!StringUtils.hasText(document.getId())) {
            document.setId(ScenarioIds.next());
        }
        if (!StringUtils.hasText(document.getStatus())) {
            document.setStatus("published");
        }
        if (document.getPublishTime() == null) {
            document.setPublishTime(LocalDateTime.now().withNano(0));
        }
        if (document.getViewCount() == null) {
            document.setViewCount(0);
        }
        ArticleDocument saved = template.save(document);
        indexSupport.refresh(ArticleDocument.class);
        return saved;
    }

    public ArticleDocument get(String id) {
        indexSupport.ensure(ArticleDocument.class);
        ArticleDocument document = template.get(id, ArticleDocument.class);
        if (document == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        return document;
    }

    public String delete(String id) {
        indexSupport.ensure(ArticleDocument.class);
        String deleted = template.delete(id, ArticleDocument.class);
        indexSupport.refresh(ArticleDocument.class);
        return deleted;
    }

    public SearchPage<ArticleDocument> search(String q, String category, String author, String tag,
                                               String status, String from, String to, boolean phrase,
                                               Integer page, Integer size) {
        indexSupport.ensure(ArticleDocument.class);
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        if (StringUtils.hasText(q)) {
            MultiMatchQueryBuilder multi = QueryBuilders.multiMatchQuery(q)
                    .field("title", 3.0f)
                    .field("summary", 2.0f)
                    .field("content", 1.0f);
            multi.type(phrase ? MultiMatchQueryBuilder.Type.PHRASE : MultiMatchQueryBuilder.Type.BEST_FIELDS);
            bool.must(multi);
        } else {
            bool.must(QueryBuilders.matchAllQuery());
        }
        if (StringUtils.hasText(category)) {
            bool.filter(QueryBuilders.termQuery("category", category));
        }
        if (StringUtils.hasText(author)) {
            bool.filter(QueryBuilders.termQuery("author", author));
        }
        if (StringUtils.hasText(tag)) {
            bool.filter(QueryBuilders.termQuery("tags", tag));
        }
        if (!"all".equalsIgnoreCase(status)) {
            bool.filter(QueryBuilders.termQuery("status", StringUtils.hasText(status) ? status : "published"));
        }
        LocalDateTime start = ScenarioTimes.parse(from);
        LocalDateTime end = ScenarioTimes.parse(to);
        if (start != null || end != null) {
            if (start != null && end != null && start.isAfter(end)) {
                throw new IllegalArgumentException("开始时间不能晚于结束时间");
            }
            if (start == null) {
                start = LocalDateTime.of(1970, 1, 1, 0, 0);
            }
            if (end == null) {
                end = LocalDateTime.now().withNano(0).plusDays(1);
            }
            EsQuerySupport.filterTime(bool, "publishTime", start, end);
        }

        HighlightBuilder.Field titleField = new HighlightBuilder.Field("title");
        titleField.preTags("<em>");
        titleField.postTags("</em>");
        HighlightBuilder.Field contentField = new HighlightBuilder.Field("content");
        contentField.preTags("<em>");
        contentField.postTags("</em>");
        contentField.fragmentSize(80);
        contentField.numOfFragments(2);

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withPageable(ScenarioPages.of(page, size))
                .withTrackTotalHits(Boolean.TRUE)
                .withHighlightFields(titleField, contentField);
        if (!StringUtils.hasText(q)) {
            builder.withSort(SortBuilders.fieldSort("publishTime").order(SortOrder.DESC));
        }
        return SearchPage.of(template.search(builder.build(), ArticleDocument.class));
    }

    public SearchPage<ArticleDocument> similar(String id, Integer size) {
        ArticleDocument document = get(id);
        String like = document.getTitle() + " " + document.getSummary() + " " + document.getContent();
        BoolQueryBuilder bool = QueryBuilders.boolQuery()
                .must(QueryBuilders.moreLikeThisQuery(
                        new String[]{"title", "summary", "content"}, new String[]{like}, null)
                        .minTermFreq(1)
                        .minDocFreq(1)
                        .maxQueryTerms(12))
                .mustNot(QueryBuilders.idsQuery().addIds(id))
                .filter(QueryBuilders.termQuery("status", "published"));
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withPageable(ScenarioPages.of(0, size == null ? 5 : size))
                .withTrackTotalHits(Boolean.TRUE);
        return SearchPage.of(template.search(builder.build(), ArticleDocument.class));
    }

    private int saveAll(List<ArticleDocument> articles) {
        indexSupport.ensure(ArticleDocument.class);
        template.save(articles);
        indexSupport.refresh(ArticleDocument.class);
        return articles.size();
    }

    private ArticleDocument article(String id, String title, String category, String author,
                                    String summary, String content, String tag1, String tag2) {
        List<String> tags = new ArrayList<String>();
        tags.add(tag1);
        tags.add(tag2);
        return ArticleDocument.builder()
                .id(id)
                .title(title)
                .summary(summary)
                .content(content)
                .author(author)
                .category(category)
                .tags(tags)
                .status("published")
                .publishTime(LocalDateTime.now().withNano(0).minusDays(Long.parseLong(id.substring(id.lastIndexOf('-') + 1))))
                .viewCount(100)
                .build();
    }
}
