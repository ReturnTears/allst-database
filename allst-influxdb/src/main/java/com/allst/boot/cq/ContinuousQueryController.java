package com.allst.boot.cq;

import com.allst.boot.influx.common.ApiResponse;
import com.allst.boot.influx.schema.SchemaStep;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/influx/continuous-queries")
public class ContinuousQueryController {

    private final ContinuousQueryService continuousQueryService;

    public ContinuousQueryController(ContinuousQueryService continuousQueryService) {
        this.continuousQueryService = continuousQueryService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(continuousQueryService.list());
    }

    @PostMapping("/init")
    public ApiResponse<List<SchemaStep>> init() {
        return ApiResponse.ok(continuousQueryService.ensure());
    }

    @PostMapping("/backfill")
    public ApiResponse<Map<String, Object>> backfill(@RequestParam String level,
                                                     @RequestParam(required = false) String range,
                                                     @RequestParam(required = false) String start,
                                                     @RequestParam(required = false) String end) {
        return ApiResponse.ok(continuousQueryService.backfill(level, range, start, end));
    }

    @DeleteMapping("/{name}")
    public ApiResponse<Map<String, Object>> drop(@PathVariable String name) {
        return ApiResponse.ok(continuousQueryService.drop(name));
    }
}
