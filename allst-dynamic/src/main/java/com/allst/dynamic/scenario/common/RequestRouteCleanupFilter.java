package com.allst.dynamic.scenario.common;

import com.allst.dynamic.scenario.rw.ForceMasterContext;
import com.allst.dynamic.scenario.sharding.OrderTableContext;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求结束时清掉线程上的路由痕迹，避免 Tomcat 线程池把上一个请求的数据源带到下一个请求。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestRouteCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            DynamicDataSourceContextHolder.clear();
            OrderTableContext.clear();
            TenantContextHolder.clear();
            ForceMasterContext.clear();
        }
    }
}
