package com.allst.boot.ops;

import com.allst.boot.cassandra.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cassandra/backup")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping("/plan")
    public ApiResponse<Map<String, Object>> plan(@RequestParam(required = false) String tag) {
        return ApiResponse.ok(backupService.plan(tag));
    }

    @PostMapping("/export")
    public ApiResponse<Map<String, Object>> export(@RequestParam String deviceId,
                                                   @RequestParam String day,
                                                   @RequestParam(required = false) String metricType,
                                                   @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(backupService.export(deviceId, day, metricType, limit));
    }

    @PostMapping("/restore")
    public ApiResponse<Map<String, Object>> restore(@RequestParam String fileName) {
        return ApiResponse.ok(backupService.restore(fileName));
    }
}
