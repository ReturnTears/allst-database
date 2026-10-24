package com.allst.boot.continuous;

import com.allst.boot.cassandra.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cassandra/continuous")
public class ContinuousController {

    private final ContinuousService continuousService;

    public ContinuousController(ContinuousService continuousService) {
        this.continuousService = continuousService;
    }

    @GetMapping("/page")
    public ApiResponse<Map<String, Object>> page(@RequestParam String deviceId,
                                                 @RequestParam String day,
                                                 @RequestParam(required = false) String metricType,
                                                 @RequestParam(required = false) Integer pageSize,
                                                 @RequestParam(required = false) String pagingState) {
        return ApiResponse.ok(continuousService.page(deviceId, day, metricType, pageSize, pagingState));
    }

    @GetMapping("/scan")
    public ApiResponse<Map<String, Object>> scan(@RequestParam(required = false) Integer pageSize,
                                                 @RequestParam(required = false) String pagingState,
                                                 @RequestParam String confirm) {
        return ApiResponse.ok(continuousService.scan(pageSize, pagingState, confirm));
    }

    @PostMapping("/rollup")
    public ApiResponse<Map<String, Object>> rollup(@RequestParam String deviceId, @RequestParam String day) {
        return ApiResponse.ok(continuousService.rollup(deviceId, day));
    }

    @GetMapping("/hourly")
    public ApiResponse<Map<String, Object>> hourly(@RequestParam String deviceId,
                                                   @RequestParam String day,
                                                   @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(continuousService.hourly(deviceId, day, limit));
    }
}
