package com.allst.boot.guide.service;

import com.allst.boot.guide.dto.EmployeeSummary;
import com.allst.boot.guide.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis 未启动时用假的 StringRedisTemplate 验证 Cache-Aside。
 * 真正连 Redis 时打开 spring.profiles.active=redis。
 */
@ExtendWith(MockitoExtension.class)
class RedisCacheAsideTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> values;

    @Mock
    private ObjectProvider<StringRedisTemplate> redisProvider;

    @Test
    void cacheHitDoesNotQueryJpa() {
        when(redisProvider.getIfAvailable()).thenReturn(redis);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("guide:employee:7")).thenReturn("7|Ada|ada@example.com");
        EmployeeCacheService service = new EmployeeCacheService(employeeRepository, redisProvider);

        Optional<EmployeeSummary> summary = service.findSummary(7L);

        assertTrue(summary.isPresent());
        assertEquals("Ada", summary.get().getName());
        verify(employeeRepository, never()).findById(7L);
    }
}
