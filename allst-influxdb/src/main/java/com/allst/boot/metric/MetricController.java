package com.allst.boot.metric;

import com.allst.boot.influx.common.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/influx/metrics")
public class MetricController {

    private final MetricWriteService writeService;
    private final MetricQueryService queryService;

    public MetricController(MetricWriteService writeService, MetricQueryService queryService) {
        this.writeService = writeService;
        this.queryService = queryService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> write(@RequestBody MetricWriteCommand command) {
        return ApiResponse.ok(writeService.write(command));
    }

    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> batch(@RequestBody MetricBatchCommand command) {
        return ApiResponse.ok(writeService.writeBatch(command));
    }

    @PostMapping("/async")
    public ApiResponse<Map<String, Object>> async(@RequestBody MetricWriteCommand command) {
        return ApiResponse.ok(writeService.writeAsync(command));
    }

    @PostMapping("/seed")
    public ApiResponse<Map<String, Object>> seed() {
        return ApiResponse.ok(writeService.seed());
    }

    @PostMapping("/flush")
    public ApiResponse<Map<String, Object>> flush() {
        return ApiResponse.ok(writeService.flush());
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> query(@RequestParam String deviceId,
                                                  @RequestParam String metricType,
                                                  @RequestParam(required = false) String region,
                                                  @RequestParam(required = false) String range,
                                                  @RequestParam(required = false) String start,
                                                  @RequestParam(required = false) String end,
                                                  @RequestParam(required = false) String cursor,
                                                  @RequestParam(required = false) String order,
                                                  @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(queryService.query(deviceId, region, metricType, range, start, end, cursor, order, limit));
    }

    @GetMapping("/latest")
    public ApiResponse<Map<String, Object>> latest(@RequestParam String deviceId,
                                                   @RequestParam(required = false) String region,
                                                   @RequestParam(required = false) String metricType,
                                                   @RequestParam(required = false) String range) {
        return ApiResponse.ok(queryService.latest(deviceId, region, metricType, range));
    }

    @DeleteMapping
    public ApiResponse<Map<String, Object>> delete(@RequestParam(required = false) String deviceId,
                                                   @RequestParam(required = false) String region,
                                                   @RequestParam(required = false) String metricType,
                                                   @RequestParam(required = false) String range,
                                                   @RequestParam(required = false) String start,
                                                   @RequestParam(required = false) String end) {
        return ApiResponse.ok(queryService.delete(deviceId, region, metricType, range, start, end));
    }
}
