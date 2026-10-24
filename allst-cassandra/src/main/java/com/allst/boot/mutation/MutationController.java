package com.allst.boot.mutation;

import com.allst.boot.cassandra.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cassandra/mutations")
public class MutationController {

    private final MutationService mutationService;

    public MutationController(MutationService mutationService) {
        this.mutationService = mutationService;
    }

    @PostMapping("/stock")
    public ApiResponse<Map<String, Object>> createStock(@RequestBody StockCreateCommand command) {
        return ApiResponse.ok(mutationService.createStock(command));
    }

    @PostMapping("/stock/cas")
    public ApiResponse<Map<String, Object>> compareAndSet(@RequestBody StockCasCommand command) {
        return ApiResponse.ok(mutationService.compareAndSet(command));
    }

    @GetMapping("/stock")
    public ApiResponse<Map<String, Object>> stock(@RequestParam String skuId) {
        return ApiResponse.ok(mutationService.stock(skuId));
    }

    @PostMapping("/counters")
    public ApiResponse<Map<String, Object>> increment(@RequestBody CounterCommand command) {
        return ApiResponse.ok(mutationService.increment(command));
    }

    @GetMapping("/counters")
    public ApiResponse<Map<String, Object>> counters(@RequestParam String deviceId,
                                                     @RequestParam(required = false) String day) {
        return ApiResponse.ok(mutationService.counters(deviceId, day));
    }

    @PutMapping("/profiles")
    public ApiResponse<Map<String, Object>> saveProfile(@RequestBody ProfileCommand command) {
        return ApiResponse.ok(mutationService.saveProfile(command));
    }

    @PostMapping("/profiles/labels")
    public ApiResponse<Map<String, Object>> appendLabels(@RequestBody LabelCommand command) {
        return ApiResponse.ok(mutationService.appendLabels(command));
    }

    @PostMapping("/profiles/thresholds")
    public ApiResponse<Map<String, Object>> updateThreshold(@RequestBody ThresholdCommand command) {
        return ApiResponse.ok(mutationService.updateThreshold(command));
    }

    @GetMapping("/profiles")
    public ApiResponse<Map<String, Object>> profile(@RequestParam String deviceId) {
        return ApiResponse.ok(mutationService.profile(deviceId));
    }
}
