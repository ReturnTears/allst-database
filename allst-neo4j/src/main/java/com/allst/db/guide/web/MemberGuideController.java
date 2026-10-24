package com.allst.db.guide.web;

import com.allst.db.guide.service.MemberGuideService;
import com.allst.db.guide.web.dto.MemberRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成员的创建、更新、停用和删除。说明见 docs/neo4j/02-statements.md。
 */
@RestController
@RequestMapping(value = "/api/neo4j/guide/members", produces = MediaType.APPLICATION_JSON_VALUE)
public class MemberGuideController {

    private final MemberGuideService memberGuideService;

    public MemberGuideController(MemberGuideService memberGuideService) {
        this.memberGuideService = memberGuideService;
    }

    @GetMapping
    public ApiResponse<Object> list(@RequestParam(required = false) String city,
                                    @RequestParam(required = false) String active,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(memberGuideService.list(city, active, page, size));
    }

    @GetMapping("/{email}")
    public ApiResponse<Object> get(@PathVariable String email) {
        return ApiResponse.ok(memberGuideService.get(email));
    }

    @PostMapping
    public ApiResponse<Object> create(@RequestBody MemberRequest request) {
        return ApiResponse.ok(memberGuideService.create(request));
    }

    @PostMapping("/batch")
    public ApiResponse<Object> batch(@RequestBody List<MemberRequest> requests) {
        return ApiResponse.ok(memberGuideService.batch(requests));
    }

    @PatchMapping("/{email}")
    public ApiResponse<Object> update(@PathVariable String email, @RequestBody MemberRequest request) {
        return ApiResponse.ok(memberGuideService.update(email, request));
    }

    @PostMapping("/{email}/deactivate")
    public ApiResponse<Object> deactivate(@PathVariable String email) {
        return ApiResponse.ok(memberGuideService.deactivate(email));
    }

    @DeleteMapping("/{email}/remark")
    public ApiResponse<Object> removeRemark(@PathVariable String email) {
        return ApiResponse.ok(memberGuideService.removeRemark(email));
    }

    @DeleteMapping("/{email}")
    public ApiResponse<Object> delete(@PathVariable String email) {
        return ApiResponse.ok(memberGuideService.detachDelete(email));
    }
}
