package com.allst.boot.controller;

import com.allst.boot.model.ApiResponse;
import com.allst.boot.model.QueryRequest;
import com.allst.boot.model.QueryResult;
import com.allst.boot.service.PrestoQueryService;
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
 * @since 2024-08-13 下午 09:59
 */
@RestController
@RequestMapping("/presto")
public class PrestoController {

    private final PrestoQueryService prestoQueryService;

    public PrestoController(PrestoQueryService prestoQueryService) {
        this.prestoQueryService = prestoQueryService;
    }

    @GetMapping("/halo")
    public String halo() {
        return "Hello Presto ~";
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        return ApiResponse.ok(prestoQueryService.info());
    }

    @GetMapping("/samples")
    public ApiResponse<List<Map<String, String>>> samples() {
        return ApiResponse.ok(prestoQueryService.samples());
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(prestoQueryService.health());
    }

    @GetMapping("/catalogs")
    public ApiResponse<List<String>> catalogs() {
        return ApiResponse.ok(prestoQueryService.catalogs());
    }

    @GetMapping("/schemas")
    public ApiResponse<List<String>> schemas(@RequestParam("catalog") String catalog) {
        return ApiResponse.ok(prestoQueryService.schemas(catalog));
    }

    @GetMapping("/tables")
    public ApiResponse<List<String>> tables(@RequestParam("catalog") String catalog,
                                            @RequestParam("schema") String schema) {
        return ApiResponse.ok(prestoQueryService.tables(catalog, schema));
    }

    @PostMapping("/query")
    public ApiResponse<QueryResult> query(@RequestBody QueryRequest request) {
        return ApiResponse.ok(prestoQueryService.query(request));
    }
}
