package com.allst.es.scenario.analytics;

import com.allst.es.scenario.common.ScenarioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenario/analytics")
public class RealtimeAnalyticsController {
    private final RealtimeAnalyticsService service;

    public RealtimeAnalyticsController(RealtimeAnalyticsService service) {
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
    public ScenarioResponse<OrderEventDocument> ingest(@RequestBody OrderEventDocument event) {
        return ScenarioResponse.ok(service.ingest(event));
    }

    @GetMapping("/report")
    public ScenarioResponse<AnalyticsReport> report(@RequestParam(required = false) String from,
                                                      @RequestParam(required = false) String to,
                                                      @RequestParam(required = false) String interval) {
        return ScenarioResponse.ok(service.report(from, to, interval));
    }
}
