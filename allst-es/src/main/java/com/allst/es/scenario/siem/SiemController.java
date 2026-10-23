package com.allst.es.scenario.siem;

import com.allst.es.scenario.common.ScenarioResponse;
import com.allst.es.scenario.common.SearchPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenario/siem")
public class SiemController {
    private final SiemService service;

    public SiemController(SiemService service) {
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
    public ScenarioResponse<SecurityEventDocument> ingest(@RequestBody SecurityEventDocument event) {
        return ScenarioResponse.ok(service.ingest(event));
    }

    @GetMapping("/search")
    public ScenarioResponse<SearchPage<SecurityEventDocument>> search(@RequestParam(required = false) String q,
                                                                        @RequestParam(required = false) String eventType,
                                                                        @RequestParam(required = false) String severity,
                                                                        @RequestParam(required = false) String srcIp,
                                                                        @RequestParam(required = false) String userName,
                                                                        @RequestParam(required = false) String from,
                                                                        @RequestParam(required = false) String to,
                                                                        @RequestParam(required = false) Integer page,
                                                                        @RequestParam(required = false) Integer size) {
        SearchPage<SecurityEventDocument> result = service.search(q, eventType, severity, srcIp, userName, from, to, page, size);
        return ScenarioResponse.ok(result, result.getTotal());
    }

    @GetMapping("/dashboard")
    public ScenarioResponse<SiemDashboard> dashboard(@RequestParam(required = false) String from,
                                                      @RequestParam(required = false) String to,
                                                      @RequestParam(required = false) String interval,
                                                      @RequestParam(required = false) Integer bruteForceThreshold,
                                                      @RequestParam(required = false) Integer portScanThreshold,
                                                      @RequestParam(required = false) Integer privilegeThreshold) {
        return ScenarioResponse.ok(service.dashboard(from, to, interval, bruteForceThreshold, portScanThreshold, privilegeThreshold));
    }
}
