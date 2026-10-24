package com.allst.boot.governance;

import com.allst.boot.influx.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/influx/governance")
public class GovernanceController {

    private final GovernanceService governanceService;

    public GovernanceController(GovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    @GetMapping("/cardinality")
    public ApiResponse<Map<String, Object>> cardinality() {
        return ApiResponse.ok(governanceService.cardinality());
    }

    @GetMapping("/tag-values")
    public ApiResponse<Map<String, Object>> tagValues(@RequestParam String key,
                                                      @RequestParam(required = false) String range) {
        return ApiResponse.ok(governanceService.tagValues(key, range));
    }
}
