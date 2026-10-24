package com.allst.boot.reading;

import com.allst.boot.cassandra.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cassandra/readings")
public class ReadingController {

    private final ReadingWriteService writeService;
    private final ReadingQueryService queryService;

    public ReadingController(ReadingWriteService writeService, ReadingQueryService queryService) {
        this.writeService = writeService;
        this.queryService = queryService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> write(@RequestBody ReadingWriteCommand command) {
        return ApiResponse.ok(writeService.write(command));
    }

    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> batch(@RequestBody ReadingBatchCommand command) {
        return ApiResponse.ok(writeService.writeBatch(command));
    }

    @PostMapping("/async")
    public ApiResponse<Map<String, Object>> async(@RequestBody ReadingWriteCommand command) {
        return ApiResponse.ok(writeService.writeAsync(command));
    }

    @PostMapping("/seed")
    public ApiResponse<Map<String, Object>> seed() {
        return ApiResponse.ok(writeService.seed());
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> query(@RequestParam String deviceId,
                                                  @RequestParam String day,
                                                  @RequestParam(required = false) String metricType,
                                                  @RequestParam(required = false) String from,
                                                  @RequestParam(required = false) String to,
                                                  @RequestParam(required = false) String order,
                                                  @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(queryService.query(deviceId, day, metricType, from, to, order, limit));
    }

    @GetMapping("/latest")
    public ApiResponse<Map<String, Object>> latest(@RequestParam String deviceId,
                                                   @RequestParam String day,
                                                   @RequestParam String metricType) {
        return ApiResponse.ok(queryService.latest(deviceId, day, metricType));
    }

    @GetMapping("/latest-per-metric")
    public ApiResponse<Map<String, Object>> latestPerMetric(@RequestParam String deviceId, @RequestParam String day) {
        return ApiResponse.ok(queryService.latestPerMetric(deviceId, day));
    }

    @GetMapping("/by-region")
    public ApiResponse<Map<String, Object>> byRegion(@RequestParam String region,
                                                     @RequestParam String day,
                                                     @RequestParam(required = false) String metricType,
                                                     @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(queryService.byRegion(region, day, metricType, limit));
    }
}
