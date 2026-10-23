package com.allst.boot.guide.service;

import com.allst.boot.guide.dto.EmployeeSummary;
import com.allst.boot.guide.repository.EmployeeRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

/**
 * Cache-Aside：先读 Redis，未命中再读 JPA，然后回填缓存。
 * 没有 redis profile 时容器里没有 StringRedisTemplate，查询直接走数据库。
 */
@Service
public class EmployeeCacheService {

    static final String KEY_PREFIX = "guide:employee:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final EmployeeRepository employeeRepository;
    private final ObjectProvider<StringRedisTemplate> redisProvider;

    public EmployeeCacheService(EmployeeRepository employeeRepository,
                                ObjectProvider<StringRedisTemplate> redisProvider) {
        this.employeeRepository = employeeRepository;
        this.redisProvider = redisProvider;
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeSummary> findSummary(Long id) {
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        String key = KEY_PREFIX + id;
        if (redis != null) {
            String cached = redis.opsForValue().get(key);
            EmployeeSummary summary = decode(cached);
            if (summary != null) {
                return Optional.of(summary);
            }
        }
        return employeeRepository.findById(id).map(employee -> {
            EmployeeSummary summary = EmployeeSummary.from(employee);
            if (redis != null) {
                redis.opsForValue().set(key, encode(summary), TTL);
            }
            return summary;
        });
    }

    public void evict(Long id) {
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis != null) {
            redis.delete(KEY_PREFIX + id);
        }
    }

    private String encode(EmployeeSummary summary) {
        return summary.getId() + "|" + summary.getName() + "|" + summary.getEmail();
    }

    private EmployeeSummary decode(String cached) {
        if (cached == null || cached.isBlank()) {
            return null;
        }
        String[] parts = cached.split("\\|", 3);
        if (parts.length < 3) {
            return null;
        }
        return new EmployeeSummary(Long.valueOf(parts[0]), parts[1], parts[2]);
    }
}
