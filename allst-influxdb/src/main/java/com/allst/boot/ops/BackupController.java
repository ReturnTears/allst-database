package com.allst.boot.ops;

import com.allst.boot.influx.common.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/influx/backup")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/export")
    public ApiResponse<Map<String, Object>> export(@RequestParam(required = false) String deviceId,
                                                   @RequestParam(required = false) String region,
                                                   @RequestParam(required = false) String metricType,
                                                   @RequestParam(required = false) String range,
                                                   @RequestParam(required = false) String start,
                                                   @RequestParam(required = false) String end,
                                                   @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(backupService.export(deviceId, region, metricType, range, start, end, limit));
    }

    @PostMapping("/restore")
    public ApiResponse<Map<String, Object>> restore(@RequestParam String fileName) {
        return ApiResponse.ok(backupService.restore(fileName));
    }

    @PostMapping("/portable")
    public ApiResponse<Map<String, Object>> portable(@RequestParam String action,
                                                     @RequestParam(required = false) String name,
                                                     @RequestParam(required = false) String targetDatabase) {
        return ApiResponse.ok(backupService.portable(action, name, targetDatabase));
    }
}
