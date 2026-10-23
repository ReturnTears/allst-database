package com.allst.boot.guide.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * 当前操作人。测试线程可以写入 {@link #CURRENT}，Web 请求优先读 X-Auditor。
 */
@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    public static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    @Override
    public Optional<String> getCurrentAuditor() {
        if (CURRENT.get() != null && !CURRENT.get().isBlank()) {
            return Optional.of(CURRENT.get());
        }
        ServletRequestAttributes attributes = currentRequest();
        if (attributes != null) {
            String header = attributes.getRequest().getHeader("X-Auditor");
            if (header != null && !header.isBlank()) {
                return Optional.of(header);
            }
        }
        return Optional.of("system");
    }

    private ServletRequestAttributes currentRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes)) {
            return null;
        }
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }
}
