package com.allst.dynamic.scenario.rw;

import com.allst.dynamic.scenario.common.DataSourceKeys;
import com.allst.dynamic.scenario.common.ScenarioException;
import com.allst.dynamic.scenario.common.ScenarioHeaders;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.GroupDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.sql.DataSource;

/**
 * 路由切面要包在事务外面。连接是在事务开始时拿到的，切面如果跑在事务里面，切换不会生效。
 * 从库组在进入方法时选定一个成员，避免同一方法里的多条 SQL 各自重新轮询。
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReadWriteRouteAspect {

    private final DataSource dataSource;

    public ReadWriteRouteAspect(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Around("@annotation(com.allst.dynamic.scenario.rw.WriteRoute)")
    public Object write(ProceedingJoinPoint point) throws Throwable {
        ForceMasterContext.mark();
        DynamicDataSourceContextHolder.push(DataSourceKeys.RW_MASTER);
        try {
            return point.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    @Around("@annotation(com.allst.dynamic.scenario.rw.ReadRoute)")
    public Object read(ProceedingJoinPoint point) throws Throwable {
        String key = forceMaster() ? DataSourceKeys.RW_MASTER : nextSlave();
        DynamicDataSourceContextHolder.push(key);
        try {
            return point.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    private boolean forceMaster() {
        if (ForceMasterContext.marked()) {
            return true;
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return false;
        }
        String header = servletAttributes.getRequest().getHeader(ScenarioHeaders.FORCE_MASTER);
        return "true".equalsIgnoreCase(header);
    }

    private String nextSlave() {
        if (!(dataSource instanceof DynamicRoutingDataSource routing)) {
            throw new ScenarioException(500, "当前 DataSource 不是 DynamicRoutingDataSource");
        }
        GroupDataSource group = routing.getGroupDataSources().get(DataSourceKeys.RW_SLAVE_GROUP);
        if (group == null || group.getDataSourceMap().isEmpty()) {
            throw new ScenarioException(503, "从库组 rwSlave 没有可用数据源");
        }
        return group.determineDsKey();
    }
}
