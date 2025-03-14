package com.allst.es.controller;

import com.allst.es.func.NumFuncInterface;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping(value = "/video")
public class VideoController {
    private static int count = 0;
    @GetMapping("/v1")
    public String getVideo() {
        return "video";
    }

    @GetMapping("/calc")
    public Object getCalc() {
        count += 1;
        System.out.println(LocalDateTime.now() + ": count : " + count);
        Integer[] params = {1, 2, 3, 4, 5};
        return calcNum(new NumFuncInterface() {
            @Override
            public Object calc(Integer... params) {
                return count % 3 == 0 ? sum(params) : count % 3 == 1 ? multiply(params) : sum(params) + multiply(params);
            }
        }, params);
    }

    private Object calcNum(final NumFuncInterface test, Integer... params) {
        return test.calc(params);
    }
}
