package com.allst.es.scenario.fulltext;

import com.allst.es.scenario.common.ScenarioResponse;
import com.allst.es.scenario.common.SearchPage;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenario/fulltext")
public class FullTextSearchController {
    private final FullTextSearchService service;

    public FullTextSearchController(FullTextSearchService service) {
        this.service = service;
    }

    @PostMapping("/index/init")
    public ScenarioResponse<Boolean> init() {
        return ScenarioResponse.ok(service.initIndex());
    }

    @PostMapping("/seed")
    public ScenarioResponse<Integer> seed() {
        int count = service.seed();
        return ScenarioResponse.ok(count, count);
    }

    @PostMapping("/docs")
    public ScenarioResponse<ArticleDocument> save(@RequestBody ArticleDocument document) {
        return ScenarioResponse.ok(service.save(document));
    }

    @GetMapping("/docs/{id}")
    public ScenarioResponse<ArticleDocument> get(@PathVariable String id) {
        return ScenarioResponse.ok(service.get(id));
    }

    @DeleteMapping("/docs/{id}")
    public ScenarioResponse<String> delete(@PathVariable String id) {
        return ScenarioResponse.ok(service.delete(id));
    }

    @GetMapping("/docs/{id}/similar")
    public ScenarioResponse<SearchPage<ArticleDocument>> similar(@PathVariable String id,
                                                                   @RequestParam(required = false) Integer size) {
        SearchPage<ArticleDocument> page = service.similar(id, size);
        return ScenarioResponse.ok(page, page.getTotal());
    }

    @GetMapping("/search")
    public ScenarioResponse<SearchPage<ArticleDocument>> search(@RequestParam(required = false) String q,
                                                                  @RequestParam(required = false) String category,
                                                                  @RequestParam(required = false) String author,
                                                                  @RequestParam(required = false) String tag,
                                                                  @RequestParam(required = false) String status,
                                                                  @RequestParam(required = false) String from,
                                                                  @RequestParam(required = false) String to,
                                                                  @RequestParam(defaultValue = "false") boolean phrase,
                                                                  @RequestParam(required = false) Integer page,
                                                                  @RequestParam(required = false) Integer size) {
        SearchPage<ArticleDocument> result = service.search(q, category, author, tag, status, from, to, phrase, page, size);
        return ScenarioResponse.ok(result, result.getTotal());
    }
}
