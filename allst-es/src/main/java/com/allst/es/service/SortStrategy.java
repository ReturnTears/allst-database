package com.allst.es.service;

import java.util.Comparator;

/**
 * @author Hutu
 * @since 2024-08-23 下午 09:19
 */
@FunctionalInterface
public interface SortStrategy {
    Comparator<Integer> getComparator();
}
