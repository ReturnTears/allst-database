package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Hutu
 * @since 2024-08-28 下午 10:26
 */
@RestController
@RequestMapping("/antlr")
public class AntlrController {
    @GetMapping("/")
    private Map<String, Object> antlr(String sql) {
        System.out.println("sql: " + sql);
        return Map.of("Hello", "World!");
    }

}
