package com.allst.dynamic.scenario.tenant;

import com.allst.dynamic.scenario.common.RouteView;

import java.util.List;

public interface TenantAccountService {

    RouteView<TenantAccount> create(AccountCommand command);

    RouteView<List<TenantAccount>> list();

    RouteView<List<LedgerEntry>> ledgers();

    String register(RegisterTenantCommand command);

    String remove(String tenantId);
}
