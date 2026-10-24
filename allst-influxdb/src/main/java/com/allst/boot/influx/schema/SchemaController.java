package com.allst.boot.influx.schema;

import com.allst.boot.influx.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/influx/schema")
public class SchemaController {

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @PostMapping("/init")
    public ApiResponse<List<SchemaStep>> init() {
        return ApiResponse.ok(schemaService.init());
    }

    @GetMapping("/retention-policies")
    public ApiResponse<List<Map<String, Object>>> retentionPolicies() {
        return ApiResponse.ok(schemaService.retentionPolicies());
    }

    @GetMapping("/fields")
    public ApiResponse<List<Map<String, Object>>> fields() {
        return ApiResponse.ok(schemaService.fieldKeys());
    }
}
