package com.allst.es.service.impl;

import com.allst.es.service.SortStrategy;
import org.springframework.stereotype.Service;

import java.util.Comparator;

/**
 * @author Hutu
 * @since 2024-08-23 下午 09:19
 */
@Service
public class DescendingSortStrategy implements SortStrategy {
    @Override
    public Comparator<Integer> getComparator() {
        // return (o1, o2) -> o2.compareTo(o1);
        return Comparator.reverseOrder(); // 使用lambda表达式实现
    }
}
