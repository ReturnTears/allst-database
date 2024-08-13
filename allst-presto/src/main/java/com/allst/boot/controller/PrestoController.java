package com.allst.boot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hutu
 * @since 2024-08-13 下午 09:59
 */
@RestController
@RequestMapping("/presto")
public class PrestoController {
    @GetMapping("/halo")
    public String halo() {
        return "Hello Presto ~";
    }
}
