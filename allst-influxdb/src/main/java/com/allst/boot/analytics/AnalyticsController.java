package com.allst.boot.analytics;

import com.allst.boot.influx.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/influx/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/aggregate")
    public ApiResponse<Map<String, Object>> aggregate(@RequestParam String fn,
                                                      @RequestParam String metricType,
                                                      @RequestParam(required = false) Integer percentile,
                                                      @RequestParam(required = false) String deviceId,
                                                      @RequestParam(required = false) String region,
                                                      @RequestParam(required = false) String range,
                                                      @RequestParam(required = false) String start,
                                                      @RequestParam(required = false) String end,
                                                      @RequestParam(required = false) String window,
                                                      @RequestParam(required = false) String fill,
                                                      @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(analyticsService.aggregate(fn, percentile, deviceId, region, metricType,
                range, start, end, window, fill, limit));
    }

    @GetMapping("/transform")
    public ApiResponse<Map<String, Object>> transform(@RequestParam String fn,
                                                      @RequestParam String metricType,
                                                      @RequestParam(required = false) Integer points,
                                                      @RequestParam(required = false) String unit,
                                                      @RequestParam(required = false) String deviceId,
                                                      @RequestParam(required = false) String region,
                                                      @RequestParam(required = false) String range,
                                                      @RequestParam(required = false) String start,
                                                      @RequestParam(required = false) String end,
                                                      @RequestParam(required = false) String window,
                                                      @RequestParam(required = false) String fill,
                                                      @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(analyticsService.transform(fn, points, unit, deviceId, region, metricType,
                range, start, end, window, fill, limit));
    }

    @GetMapping("/calculate")
    public ApiResponse<Map<String, Object>> calculate(@RequestParam String expr,
                                                      @RequestParam String metricType,
                                                      @RequestParam(required = false) String deviceId,
                                                      @RequestParam(required = false) String region,
                                                      @RequestParam(required = false) String range,
                                                      @RequestParam(required = false) String start,
                                                      @RequestParam(required = false) String end,
                                                      @RequestParam(required = false) String window,
                                                      @RequestParam(required = false) String fill,
                                                      @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(analyticsService.calculate(expr, deviceId, region, metricType,
                range, start, end, window, fill, limit));
    }
}
