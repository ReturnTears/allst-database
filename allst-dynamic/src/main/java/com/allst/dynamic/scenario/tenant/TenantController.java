package com.allst.dynamic.scenario.tenant;

import com.allst.dynamic.scenario.common.ScenarioResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenario/tenant")
public class TenantController {

    private final TenantAccountService tenantAccountService;
    private final TransferService transferService;

    public TenantController(TenantAccountService tenantAccountService, TransferService transferService) {
        this.tenantAccountService = tenantAccountService;
        this.transferService = transferService;
    }

    @PostMapping("/accounts")
    public ScenarioResponse<?> create(@RequestBody AccountCommand command) {
        return ScenarioResponse.ok(tenantAccountService.create(command));
    }

    @GetMapping("/accounts")
    public ScenarioResponse<?> list() {
        return ScenarioResponse.ok(tenantAccountService.list());
    }

    @GetMapping("/ledgers")
    public ScenarioResponse<?> ledgers() {
        return ScenarioResponse.ok(tenantAccountService.ledgers());
    }

    @PostMapping("/transfer")
    public ScenarioResponse<String> transfer(@RequestBody TransferCommand command) {
        transferService.transfer(command);
        return ScenarioResponse.ok("转账已提交");
    }

    @PostMapping("/datasources")
    public ScenarioResponse<String> register(@RequestBody RegisterTenantCommand command) {
        return ScenarioResponse.ok(tenantAccountService.register(command));
    }

    @DeleteMapping("/datasources/{tenantId}")
    public ScenarioResponse<String> remove(@PathVariable("tenantId") String tenantId) {
        return ScenarioResponse.ok(tenantAccountService.remove(tenantId));
    }
}
