package com.allst.dynamic.scenario.tenant;

import com.allst.dynamic.scenario.common.Checks;
import com.allst.dynamic.scenario.common.DataSources;
import com.allst.dynamic.scenario.common.RouteProbe;
import com.allst.dynamic.scenario.common.RouteView;
import com.allst.dynamic.scenario.common.ScenarioException;
import com.allst.dynamic.scenario.common.TenantContextHolder;
import com.allst.dynamic.scenario.runtime.RuntimeDataSourceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TenantAccountServiceImpl implements TenantAccountService {

    private final TenantAccountMapper tenantAccountMapper;
    private final LedgerWriter ledgerWriter;
    private final TenantDirectory tenantDirectory;
    private final RuntimeDataSourceService runtimeDataSourceService;
    private final RouteProbe routeProbe;

    public TenantAccountServiceImpl(TenantAccountMapper tenantAccountMapper, LedgerWriter ledgerWriter,
                                    TenantDirectory tenantDirectory, RuntimeDataSourceService runtimeDataSourceService,
                                    RouteProbe routeProbe) {
        this.tenantAccountMapper = tenantAccountMapper;
        this.ledgerWriter = ledgerWriter;
        this.tenantDirectory = tenantDirectory;
        this.runtimeDataSourceService = runtimeDataSourceService;
        this.routeProbe = routeProbe;
    }

    @Override
    public RouteView<TenantAccount> create(AccountCommand command) {
        String tenantId = currentTenantId();
        String key = tenantDirectory.requireKey(tenantId);
        TenantAccount account = new TenantAccount();
        account.setAccountName(Checks.requireText(command == null ? null : command.getAccountName(), "accountName", 64));
        account.setLevelName(Checks.requireText(command == null ? null : command.getLevelName(), "levelName", 32));
        account.setCreatedAt(LocalDateTime.now());
        return DataSources.call(key, () -> {
            tenantAccountMapper.insert(account);
            return RouteView.of(key, routeProbe.database(), "t_account", "请求头 X-Tenant-Id=" + tenantId, account);
        });
    }

    @Override
    public RouteView<List<TenantAccount>> list() {
        String tenantId = currentTenantId();
        String key = tenantDirectory.requireKey(tenantId);
        return DataSources.call(key, () -> {
            List<TenantAccount> accounts = tenantAccountMapper.selectList(new QueryWrapper<TenantAccount>().orderByAsc("id"));
            return RouteView.of(key, routeProbe.database(), "t_account", "请求头 X-Tenant-Id=" + tenantId, accounts);
        });
    }

    @Override
    public RouteView<List<LedgerEntry>> ledgers() {
        String tenantId = currentTenantId();
        String key = tenantDirectory.requireKey(tenantId);
        List<LedgerEntry> entries = ledgerWriter.list(key);
        String database = DataSources.call(key, routeProbe::database);
        return RouteView.of(key, database, "t_ledger", "请求头 X-Tenant-Id=" + tenantId, entries);
    }

    @Override
    public String register(RegisterTenantCommand command) {
        if (command == null) {
            throw new ScenarioException(400, "请求体不能为空");
        }
        TenantKeys.checkTenantId(command.getTenantId());
        if (tenantDirectory.builtin(command.getTenantId())) {
            throw new ScenarioException(400, "内置租户不能重复注册");
        }
        String key = TenantKeys.runtimeKey(command.getTenantId());
        runtimeDataSourceService.register(key, command.getUrl(), command.getUsername(), command.getPassword(), command.getDriverClassName());
        try {
            tenantDirectory.bindRuntime(command.getTenantId(), key);
        } catch (RuntimeException exception) {
            runtimeDataSourceService.remove(key);
            throw exception;
        }
        return key;
    }

    @Override
    public String remove(String tenantId) {
        String key = tenantDirectory.removeRuntime(tenantId);
        runtimeDataSourceService.remove(key);
        return key;
    }

    private String currentTenantId() {
        String tenantId = TenantContextHolder.get();
        if (tenantId == null || tenantId.isBlank()) {
            throw new ScenarioException(400, "缺少请求头 X-Tenant-Id");
        }
        return tenantId;
    }
}
