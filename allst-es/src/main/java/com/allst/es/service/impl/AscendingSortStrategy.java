package com.allst.es.service.impl;

import com.allst.es.service.SortStrategy;
import org.springframework.stereotype.Service;

import java.util.Comparator;

/**
 * @author Hutu
 * @since 2024-08-23 下午 09:20
 */
@Service
public class AscendingSortStrategy implements SortStrategy {
    @Override
    public Comparator<Integer> getComparator() {
        return Integer::compareTo; // 使用lambda表达式实现
    }
}
