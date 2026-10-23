package com.allst.boot.guide.listener;

import com.allst.boot.guide.entity.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.stereotype.Component;

/**
 * Spring Data REST 的仓库事件，只在 /api 导出接口创建、保存、删除时触发。
 * 普通 repository.save 不会走这里，实体生命周期要看 {@link AuditTrailListener}。
 */
@Component
public class GuideRepositoryEventListener extends AbstractRepositoryEventListener<Department> {

    private static final Logger log = LoggerFactory.getLogger(GuideRepositoryEventListener.class);

    @Override
    protected void onAfterCreate(Department entity) {
        log.info("REST created department id={} name={}", entity.getId(), entity.getName());
    }

    @Override
    protected void onAfterSave(Department entity) {
        log.info("REST saved department id={} name={}", entity.getId(), entity.getName());
    }

    @Override
    protected void onAfterDelete(Department entity) {
        log.info("REST deleted department id={}", entity.getId());
    }
}
