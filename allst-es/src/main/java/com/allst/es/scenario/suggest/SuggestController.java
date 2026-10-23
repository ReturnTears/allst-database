package com.allst.es.scenario.suggest;

import com.allst.es.scenario.common.ScenarioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scenario/suggest")
public class SuggestController {
    private final SuggestService service;

    public SuggestController(SuggestService service) {
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

    @PostMapping("/terms")
    public ScenarioResponse<SuggestTermDocument> save(@RequestBody SuggestCommand command) {
        return ScenarioResponse.ok(service.save(command));
    }

    @GetMapping("/complete")
    public ScenarioResponse<List<SuggestOption>> complete(@RequestParam String prefix,
                                                           @RequestParam(required = false) Integer size) {
        List<SuggestOption> options = service.complete(prefix, size);
        return ScenarioResponse.ok(options, options.size());
    }
}
