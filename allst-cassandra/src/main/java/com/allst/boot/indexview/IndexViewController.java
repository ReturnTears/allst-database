package com.allst.boot.indexview;

import com.allst.boot.cassandra.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cassandra/indexes")
public class IndexViewController {

    private final IndexViewService indexViewService;

    public IndexViewController(IndexViewService indexViewService) {
        this.indexViewService = indexViewService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> definitions() {
        return ApiResponse.ok(indexViewService.definitions());
    }

    @GetMapping("/by-status")
    public ApiResponse<Map<String, Object>> byStatus(@RequestParam String deviceId,
                                                     @RequestParam String day,
                                                     @RequestParam Integer status,
                                                     @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(indexViewService.byStatus(deviceId, day, status, limit));
    }

    @GetMapping("/by-metric")
    public ApiResponse<Map<String, Object>> byMetric(@RequestParam String metricType,
                                                     @RequestParam String day,
                                                     @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(indexViewService.byMetric(metricType, day, limit));
    }
}
