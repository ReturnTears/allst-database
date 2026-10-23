package com.allst.dynamic.scenario.tenant;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 单独的 Bean，供 @DS 和 @DSTransactional 通过代理切换数据源。同类内部调用不会触发切换。
 */
@Service
public class LedgerWriter {

    private final LedgerMapper ledgerMapper;

    public LedgerWriter(LedgerMapper ledgerMapper) {
        this.ledgerMapper = ledgerMapper;
    }

    @DS("#key")
    public void append(String key, LedgerEntry entry) {
        ledgerMapper.insert(entry);
    }

    @DS("#key")
    public List<LedgerEntry> list(String key) {
        return ledgerMapper.selectList(new QueryWrapper<LedgerEntry>().orderByAsc("id"));
    }
}
