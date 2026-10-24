package com.allst.boot.cassandra.schema;

import com.allst.boot.cassandra.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cassandra/schema")
public class SchemaController {

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @PostMapping("/init")
    public ApiResponse<List<Map<String, Object>>> init() {
        return ApiResponse.ok(schemaService.init());
    }

    @GetMapping("/tables")
    public ApiResponse<Map<String, Object>> tables() {
        return ApiResponse.ok(schemaService.tables());
    }

    @GetMapping("/columns")
    public ApiResponse<Map<String, Object>> columns() {
        return ApiResponse.ok(schemaService.columns());
    }

    @GetMapping("/model")
    public ApiResponse<Map<String, Object>> model() {
        return ApiResponse.ok(DataModel.describe());
    }

    @GetMapping("/replication")
    public ApiResponse<Map<String, Object>> replication() {
        return ApiResponse.ok(schemaService.replication());
    }
}
