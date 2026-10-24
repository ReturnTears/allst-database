package com.allst.trino.controller;

import com.allst.trino.model.ApiResponse;
import com.allst.trino.model.QueryRequest;
import com.allst.trino.model.QueryResult;
import com.allst.trino.service.TrinoQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Hutu
 * @since 2026-10-23
 */
@RestController
@RequestMapping("/trino")
public class TrinoController {

    private final TrinoQueryService trinoQueryService;

    public TrinoController(TrinoQueryService trinoQueryService) {
        this.trinoQueryService = trinoQueryService;
    }

    @GetMapping("/halo")
    public String halo() {
        return "Hello Trino ~";
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        return ApiResponse.ok(trinoQueryService.info());
    }

    @GetMapping("/samples")
    public ApiResponse<List<Map<String, String>>> samples() {
        return ApiResponse.ok(trinoQueryService.samples());
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(trinoQueryService.health());
    }

    @GetMapping("/nodes")
    public ApiResponse<QueryResult> nodes() {
        return ApiResponse.ok(trinoQueryService.nodes());
    }

    @GetMapping("/catalogs")
    public ApiResponse<List<String>> catalogs() {
        return ApiResponse.ok(trinoQueryService.catalogs());
    }

    @GetMapping("/schemas")
    public ApiResponse<List<String>> schemas(@RequestParam("catalog") String catalog) {
        return ApiResponse.ok(trinoQueryService.schemas(catalog));
    }

    @GetMapping("/tables")
    public ApiResponse<List<String>> tables(@RequestParam("catalog") String catalog,
                                            @RequestParam("schema") String schema) {
        return ApiResponse.ok(trinoQueryService.tables(catalog, schema));
    }

    @PostMapping("/query")
    public ApiResponse<QueryResult> query(@RequestBody QueryRequest request) {
        return ApiResponse.ok(trinoQueryService.query(request));
    }
}
