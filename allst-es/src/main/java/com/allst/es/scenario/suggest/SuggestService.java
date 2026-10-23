package com.allst.es.scenario.suggest;

import com.allst.es.scenario.common.IndexSupport;
import com.allst.es.scenario.common.ScenarioIds;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.completion.Completion;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Data Elasticsearch 4.2 的 SearchHits 不返回 suggest。
 * 索引映射仍由模板创建，补全查询改走 RestHighLevelClient。
 */
@Service
public class SuggestService {
    private final ElasticsearchRestTemplate template;
    private final IndexSupport indexSupport;
    private final RestHighLevelClient client;

    public SuggestService(ElasticsearchRestTemplate template, IndexSupport indexSupport, RestHighLevelClient client) {
        this.template = template;
        this.indexSupport = indexSupport;
        this.client = client;
    }

    public boolean initIndex() {
        return indexSupport.init(SuggestTermDocument.class);
    }

    public int seed() {
        List<SuggestTermDocument> terms = Arrays.asList(
                term("suggest-1", "Elasticsearch 全文检索", "文章", 10, "全文检索", "Elasticsearch"),
                term("suggest-2", "实时订单分析", "文章", 8, "订单分析", "实时分析"),
                term("suggest-3", "地理位置检索", "文章", 7, "附近检索", "地理"),
                term("suggest-4", "故宫博物院", "地点", 9, "故宫"),
                term("suggest-5", "东方明珠", "地点", 8, "明珠"),
                term("suggest-6", "小米手机 14", "商品", 12, "小米", "手机"),
                term("suggest-7", "华为手机 Mate", "商品", 11, "华为", "手机"),
                term("suggest-8", "安全事件告警", "安全", 6, "告警", "SIEM")
        );
        indexSupport.ensure(SuggestTermDocument.class);
        template.save(terms);
        indexSupport.refresh(SuggestTermDocument.class);
        return terms.size();
    }

    public SuggestTermDocument save(SuggestCommand command) {
        if (command == null || !StringUtils.hasText(command.getName())) {
            throw new IllegalArgumentException("补全词不能为空");
        }
        indexSupport.ensure(SuggestTermDocument.class);
        List<String> inputs = new ArrayList<String>();
        if (command.getInputs() != null) {
            for (String input : command.getInputs()) {
                if (StringUtils.hasText(input)) {
                    inputs.add(input.trim());
                }
            }
        }
        if (!inputs.contains(command.getName())) {
            inputs.add(command.getName());
        }
        Completion completion = new Completion(inputs.toArray(new String[0]));
        if (command.getWeight() != null) {
            completion.setWeight(command.getWeight());
        }
        SuggestTermDocument document = SuggestTermDocument.builder()
                .id(StringUtils.hasText(command.getId()) ? command.getId() : ScenarioIds.next())
                .name(command.getName())
                .category(command.getCategory())
                .weight(command.getWeight())
                .suggest(completion)
                .build();
        SuggestTermDocument saved = template.save(document);
        indexSupport.refresh(SuggestTermDocument.class);
        return saved;
    }

    public List<SuggestOption> complete(String prefix, Integer size) {
        if (!StringUtils.hasText(prefix)) {
            throw new IllegalArgumentException("前缀不能为空");
        }
        int limit = size == null || size < 1 ? 8 : Math.min(size, 20);
        indexSupport.ensure(SuggestTermDocument.class);
        SearchSourceBuilder source = new SearchSourceBuilder();
        source.size(0);
        SuggestBuilder suggest = new SuggestBuilder();
        suggest.addSuggestion("term-suggest", new CompletionSuggestionBuilder("suggest")
                .prefix(prefix)
                .size(limit)
                .skipDuplicates(true));
        source.suggest(suggest);
        SearchRequest request = new SearchRequest(SuggestTermDocument.INDEX);
        request.source(source);
        SearchResponse response;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            throw new IllegalStateException("搜索建议查询失败", ex);
        }
        List<SuggestOption> options = new ArrayList<SuggestOption>();
        if (response.getSuggest() == null) {
            return options;
        }
        CompletionSuggestion suggestion = response.getSuggest().getSuggestion("term-suggest");
        if (suggestion == null) {
            return options;
        }
        for (CompletionSuggestion.Entry entry : suggestion.getEntries()) {
            for (CompletionSuggestion.Entry.Option option : entry.getOptions()) {
                SuggestOption item = new SuggestOption();
                item.setText(option.getText().string());
                item.setScore(option.getScore());
                options.add(item);
            }
        }
        return options;
    }

    private SuggestTermDocument term(String id, String name, String category, int weight, String... aliases) {
        List<String> inputs = new ArrayList<String>();
        inputs.add(name);
        for (String alias : aliases) {
            inputs.add(alias);
        }
        Completion completion = new Completion(inputs.toArray(new String[0]));
        completion.setWeight(weight);
        return SuggestTermDocument.builder()
                .id(id)
                .name(name)
                .category(category)
                .weight(weight)
                .suggest(completion)
                .build();
    }
}
