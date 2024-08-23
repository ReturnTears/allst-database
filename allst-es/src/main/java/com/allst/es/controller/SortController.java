package com.allst.es.controller;

import com.allst.es.service.SortStrategy;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Hutu
 * @since 2024-08-23 下午 09:21
 */
@RestController
@RequestMapping(value = "/sort")
public class SortController {

    private final SortStrategy sortStrategy;

    @Autowired
    public SortController(@Qualifier("descendingSortStrategy") SortStrategy sortStrategy) {
        this.sortStrategy = sortStrategy;
    }

    public void sort(List<Integer> numbers) {
        numbers.sort(sortStrategy.getComparator());
    }

    @GetMapping("/asc")
    public String ascendingSort() {
        List<Integer> numbers = Lists.newArrayList(5, 3, 2, 8, 1);
        sort(numbers);
        return String.join(",", numbers.stream().map(String::valueOf).toArray(String[]::new));
    }

    @GetMapping("/desc")
    public String descendingSort() {
        List<Integer> numbers = Lists.newArrayList(5, 3, 2, 8, 1);
        sort(numbers);
        return String.join(",", numbers.stream().map(String::valueOf).toArray(String[]::new));
    }
}
