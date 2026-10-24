package com.allst.db.guide.web;

import com.allst.db.guide.service.PlanGuideService;
import com.allst.db.guide.service.QueryGuideService;
import com.allst.db.guide.service.SearchGuideService;
import com.allst.db.guide.service.TransactionGuideService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 只读查询、全文检索、执行计划和事务演示。
 */
@RestController
@RequestMapping(value = "/api/neo4j/guide", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReadGuideController {

    private final QueryGuideService queryGuideService;
    private final SearchGuideService searchGuideService;
    private final PlanGuideService planGuideService;
    private final TransactionGuideService transactionGuideService;

    public ReadGuideController(QueryGuideService queryGuideService,
                               SearchGuideService searchGuideService,
                               PlanGuideService planGuideService,
                               TransactionGuideService transactionGuideService) {
        this.queryGuideService = queryGuideService;
        this.searchGuideService = searchGuideService;
        this.planGuideService = planGuideService;
        this.transactionGuideService = transactionGuideService;
    }

    @GetMapping("/query")
    public ApiResponse<Object> list(@RequestParam(required = false) String category) {
        return ApiResponse.ok(queryGuideService.list(category));
    }

    @GetMapping("/query/{name}")
    public ApiResponse<Object> run(@PathVariable String name, @RequestParam Map<String, String> params) {
        return ApiResponse.ok(queryGuideService.run(name, params));
    }

    @GetMapping("/search/documents")
    public ApiResponse<Object> documents(@RequestParam String q, @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(searchGuideService.documents(q, limit));
    }

    @GetMapping("/search/knows")
    public ApiResponse<Object> knows(@RequestParam String q, @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(searchGuideService.knows(q, limit));
    }

    @GetMapping("/plan/{name}/explain")
    public ApiResponse<Object> explain(@PathVariable String name, @RequestParam Map<String, String> params) {
        return ApiResponse.ok(planGuideService.explain(name, params));
    }

    @GetMapping("/plan/{name}/profile")
    public ApiResponse<Object> profile(@PathVariable String name, @RequestParam Map<String, String> params) {
        return ApiResponse.ok(planGuideService.profile(name, params));
    }

    @GetMapping("/plan/compare")
    public ApiResponse<Object> compare(@RequestParam String left, @RequestParam String right) {
        return ApiResponse.ok(planGuideService.compare(left, right));
    }

    @PostMapping("/tx/commit-demo")
    public ApiResponse<Object> commitDemo(@RequestParam(required = false) String marker) {
        return ApiResponse.ok(transactionGuideService.commitDemo(marker));
    }

    @PostMapping("/tx/rollback-demo")
    public ApiResponse<Object> rollbackDemo() {
        return ApiResponse.ok(transactionGuideService.rollbackDemo());
    }
}
