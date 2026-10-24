package com.allst.boot.analytics;

import com.allst.boot.cassandra.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cassandra/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/aggregate")
    public ApiResponse<Map<String, Object>> aggregate(@RequestParam String deviceId,
                                                      @RequestParam String day,
                                                      @RequestParam String metricType,
                                                      @RequestParam String fn) {
        return ApiResponse.ok(analyticsService.aggregate(deviceId, day, metricType, fn));
    }

    @GetMapping("/group")
    public ApiResponse<Map<String, Object>> group(@RequestParam String deviceId, @RequestParam String day) {
        return ApiResponse.ok(analyticsService.group(deviceId, day));
    }

    @GetMapping("/scalar")
    public ApiResponse<Map<String, Object>> scalar(@RequestParam String deviceId,
                                                   @RequestParam String day,
                                                   @RequestParam String metricType,
                                                   @RequestParam String time,
                                                   @RequestParam String readingId) {
        return ApiResponse.ok(analyticsService.scalar(deviceId, day, metricType, time, readingId));
    }

    @GetMapping("/arithmetic")
    public ApiResponse<Map<String, Object>> arithmetic(@RequestParam String deviceId,
                                                       @RequestParam String day,
                                                       @RequestParam String metricType,
                                                       @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(analyticsService.arithmetic(deviceId, day, metricType, limit));
    }

    @GetMapping("/token")
    public ApiResponse<Map<String, Object>> token(@RequestParam String deviceId, @RequestParam String day) {
        return ApiResponse.ok(analyticsService.token(deviceId, day));
    }

    @GetMapping("/server-time")
    public ApiResponse<Map<String, Object>> serverTime() {
        return ApiResponse.ok(analyticsService.serverTime());
    }
}
