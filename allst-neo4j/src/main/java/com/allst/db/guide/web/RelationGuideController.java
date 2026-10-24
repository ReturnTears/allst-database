package com.allst.db.guide.web;

import com.allst.db.guide.service.RelationGuideService;
import com.allst.db.guide.web.dto.BelongsToRequest;
import com.allst.db.guide.web.dto.KnowsRequest;
import com.allst.db.guide.web.dto.ManagesRequest;
import com.allst.db.guide.web.dto.TagRequest;
import com.allst.db.guide.web.dto.WorksOnRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关系写入和 FOREACH 打标签。说明见 docs/neo4j/02-statements.md。
 */
@RestController
@RequestMapping(value = "/api/neo4j/guide", produces = MediaType.APPLICATION_JSON_VALUE)
public class RelationGuideController {

    private final RelationGuideService relationGuideService;

    public RelationGuideController(RelationGuideService relationGuideService) {
        this.relationGuideService = relationGuideService;
    }

    @PostMapping("/relations/belongs-to")
    public ApiResponse<Object> belongsTo(@RequestBody BelongsToRequest request) {
        return ApiResponse.ok(relationGuideService.belongsTo(request));
    }

    @PostMapping("/relations/works-on")
    public ApiResponse<Object> worksOn(@RequestBody WorksOnRequest request) {
        return ApiResponse.ok(relationGuideService.worksOn(request));
    }

    @PostMapping("/relations/knows")
    public ApiResponse<Object> knows(@RequestBody KnowsRequest request) {
        return ApiResponse.ok(relationGuideService.knows(request));
    }

    @PostMapping("/relations/manages")
    public ApiResponse<Object> manages(@RequestBody ManagesRequest request) {
        return ApiResponse.ok(relationGuideService.manages(request));
    }

    @DeleteMapping("/relations/knows")
    public ApiResponse<Object> deleteKnows(@RequestParam String from, @RequestParam String to) {
        return ApiResponse.ok(relationGuideService.deleteKnows(from, to));
    }

    @PostMapping(value = "/documents/{code}/tags", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Object> tags(@PathVariable String code, @RequestBody TagRequest request) {
        return ApiResponse.ok(relationGuideService.tagDocument(code, request.getTags()));
    }
}
