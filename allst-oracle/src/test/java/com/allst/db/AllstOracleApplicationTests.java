package com.allst.db;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class AllstOracleApplicationTests {

    @Test
    void contextLoads() {
        List<String> list1 = List.of("1", "2", "3");
        List<String> list2 = List.of();
        List<String> result = list1.stream().filter(i1 -> !list2.contains(i1)).collect(Collectors.toList());

        System.out.println(result);

        List<String> list3 = List.of();
        List<String> list4 = List.of("1", "2", "3");
        List<String> result2 = list3.stream().filter(i1 -> !list4.contains(i1)).collect(Collectors.toList());

        System.out.println(result2);
    }

}
