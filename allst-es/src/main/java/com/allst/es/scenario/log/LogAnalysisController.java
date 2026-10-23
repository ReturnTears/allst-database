package com.allst.es.scenario.log;

import com.allst.es.scenario.common.ScenarioResponse;
import com.allst.es.scenario.common.SearchPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenario/log")
public class LogAnalysisController {
    private final LogAnalysisService service;

    public LogAnalysisController(LogAnalysisService service) {
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

    @PostMapping("/events")
    public ScenarioResponse<AppLogDocument> ingest(@RequestBody AppLogDocument log) {
        return ScenarioResponse.ok(service.ingest(log));
    }

    @GetMapping("/search")
    public ScenarioResponse<SearchPage<AppLogDocument>> search(@RequestParam(required = false) String q,
                                                                 @RequestParam(required = false) String level,
                                                                 @RequestParam(required = false) String service,
                                                                 @RequestParam(required = false) String event,
                                                                 @RequestParam(required = false) String from,
                                                                 @RequestParam(required = false) String to,
                                                                 @RequestParam(required = false) Integer page,
                                                                 @RequestParam(required = false) Integer size) {
        SearchPage<AppLogDocument> result = this.service.search(q, level, service, event, from, to, page, size);
        return ScenarioResponse.ok(result, result.getTotal());
    }

    @GetMapping("/report")
    public ScenarioResponse<LogReport> report(@RequestParam(value = "service", required = false) String serviceName,
                                               @RequestParam(required = false) String from,
                                               @RequestParam(required = false) String to,
                                               @RequestParam(required = false) String interval) {
        return ScenarioResponse.ok(service.report(serviceName, from, to, interval));
    }
}
