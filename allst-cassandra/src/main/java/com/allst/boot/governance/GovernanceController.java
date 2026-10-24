package com.allst.boot.governance;

import com.allst.boot.cassandra.common.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cassandra/governance")
public class GovernanceController {

    private final GovernanceService governanceService;

    public GovernanceController(GovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    @GetMapping("/advice")
    public ApiResponse<Map<String, Object>> advice() {
        return ApiResponse.ok(governanceService.advice());
    }

    @DeleteMapping("/readings/row")
    public ApiResponse<Map<String, Object>> deleteRow(@RequestParam String deviceId,
                                                      @RequestParam String day,
                                                      @RequestParam String metricType,
                                                      @RequestParam String time,
                                                      @RequestParam String readingId,
                                                      @RequestParam(required = false) String region) {
        return ApiResponse.ok(governanceService.deleteRow(deviceId, day, metricType, time, readingId, region));
    }

    @DeleteMapping("/readings/range")
    public ApiResponse<Map<String, Object>> deleteRange(@RequestParam String deviceId,
                                                        @RequestParam String day,
                                                        @RequestParam String metricType,
                                                        @RequestParam String from,
                                                        @RequestParam String to) {
        return ApiResponse.ok(governanceService.deleteRange(deviceId, day, metricType, from, to));
    }

    @DeleteMapping("/readings/partition")
    public ApiResponse<Map<String, Object>> deletePartition(@RequestParam String deviceId, @RequestParam String day) {
        return ApiResponse.ok(governanceService.deletePartition(deviceId, day));
    }
}
