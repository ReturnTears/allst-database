package com.allst.boot.tuning;

import com.allst.boot.cassandra.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cassandra/tuning")
public class TuningController {

    private final TuningService tuningService;

    public TuningController(TuningService tuningService) {
        this.tuningService = tuningService;
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile() {
        return ApiResponse.ok(tuningService.profile());
    }

    @GetMapping("/runtime")
    public ApiResponse<Map<String, Object>> runtime() {
        return ApiResponse.ok(tuningService.runtime());
    }

    @PostMapping("/benchmark")
    public ApiResponse<Map<String, Object>> benchmark(@RequestParam(required = false) Integer rows) {
        return ApiResponse.ok(tuningService.benchmark(rows));
    }
}
