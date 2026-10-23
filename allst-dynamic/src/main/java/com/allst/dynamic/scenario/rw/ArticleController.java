package com.allst.dynamic.scenario.rw;

import com.allst.dynamic.scenario.common.ScenarioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scenario/rw")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping("/articles")
    public ScenarioResponse<?> create(@RequestBody ArticleCommand command) {
        return ScenarioResponse.ok(articleService.create(command));
    }

    @GetMapping("/articles")
    public ScenarioResponse<?> list() {
        return ScenarioResponse.ok(articleService.list());
    }

    @GetMapping("/articles/master")
    public ScenarioResponse<?> listFromMaster() {
        return ScenarioResponse.ok(articleService.listFromMaster());
    }

    @PostMapping("/articles/read-after-write")
    public ScenarioResponse<ReadAfterWriteView> readAfterWrite(@RequestBody ArticleCommand command) {
        ReadAfterWriteView view = new ReadAfterWriteView();
        view.setWritten(articleService.create(command));
        view.setRead(articleService.list());
        return ScenarioResponse.ok(view);
    }

    @PostMapping("/seed")
    public ScenarioResponse<List<String>> seed() {
        return ScenarioResponse.ok(articleService.seed());
    }
}
