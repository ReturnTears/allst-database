package com.allst.dynamic.scenario.tenant;

import com.allst.dynamic.scenario.common.Checks;
import com.allst.dynamic.scenario.common.ScenarioException;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @DSTransactional 为每个数据源持有本地事务，一起提交或一起回滚。它不是 XA。
 * 3.5.1 里任意 Exception 都会回滚，注解上没有 rollbackFor 属性。
 */
@Service
public class TransferService {

    private final LedgerWriter ledgerWriter;
    private final TenantDirectory tenantDirectory;

    public TransferService(LedgerWriter ledgerWriter, TenantDirectory tenantDirectory) {
        this.ledgerWriter = ledgerWriter;
        this.tenantDirectory = tenantDirectory;
    }

    @DSTransactional
    public void transfer(TransferCommand command) {
        if (command == null) {
            throw new ScenarioException(400, "请求体不能为空");
        }
        String fromTenant = Checks.requireText(command.getFromTenant(), "fromTenant", 16);
        String toTenant = Checks.requireText(command.getToTenant(), "toTenant", 16);
        TenantKeys.checkTenantId(fromTenant);
        TenantKeys.checkTenantId(toTenant);
        String fromKey = tenantDirectory.requireKey(fromTenant);
        String toKey = tenantDirectory.requireKey(toTenant);
        if (fromKey.equals(toKey)) {
            throw new ScenarioException(400, "转出和转入必须是不同租户");
        }
        String bizNo = Checks.requireText(command.getBizNo(), "bizNo", 64);
        ledgerWriter.append(fromKey, entry(fromTenant, "DEBIT", command, bizNo));
        if (command.isFailAfterDebit()) {
            throw new ScenarioException(409, "模拟贷方失败，多数据源事务会回滚借方");
        }
        ledgerWriter.append(toKey, entry(toTenant, "CREDIT", command, bizNo));
    }

    private LedgerEntry entry(String tenantId, String direction, TransferCommand command, String bizNo) {
        LedgerEntry entry = new LedgerEntry();
        entry.setTenantId(tenantId);
        entry.setDirection(direction);
        entry.setAmount(Checks.requireAmount(command.getAmount()));
        entry.setBizNo(bizNo);
        entry.setCreatedAt(LocalDateTime.now());
        return entry;
    }
}
