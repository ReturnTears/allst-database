package com.allst.db.guide.web;

import com.allst.db.guide.schema.GraphSchemaService;
import com.allst.db.guide.seed.GraphSeedService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 连通性、索引、约束和样例数据。说明见 docs/neo4j/README.md。
 */
@RestController
@RequestMapping(value = "/api/neo4j/guide", produces = MediaType.APPLICATION_JSON_VALUE)
public class SchemaGuideController {

    private final GraphSchemaService schemaService;
    private final GraphSeedService seedService;

    public SchemaGuideController(GraphSchemaService schemaService, GraphSeedService seedService) {
        this.schemaService = schemaService;
        this.seedService = seedService;
    }

    @GetMapping("/health")
    public ApiResponse<Object> health() {
        return ApiResponse.ok(schemaService.health());
    }

    @PostMapping("/schema/init")
    public ApiResponse<Object> init() {
        return ApiResponse.ok(schemaService.init());
    }

    @PostMapping("/schema/drop")
    public ApiResponse<Object> drop() {
        return ApiResponse.ok(schemaService.drop());
    }

    @GetMapping("/schema/indexes")
    public ApiResponse<Object> indexes() {
        return ApiResponse.ok(schemaService.indexes());
    }

    @GetMapping("/schema/constraints")
    public ApiResponse<Object> constraints() {
        return ApiResponse.ok(schemaService.constraints());
    }

    @PostMapping("/seed")
    public ApiResponse<Object> seed() {
        return ApiResponse.ok(seedService.seed());
    }

    @DeleteMapping("/seed")
    public ApiResponse<Object> purge() {
        return ApiResponse.ok(seedService.purge());
    }
}
