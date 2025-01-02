package com.allst.es.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hutu
 * @since 2025-01-02 下午 10:10
 */
@RestController
@RequestMapping(value = "/video")
public class VideoController {
    @GetMapping("/v1")
    public String getVideo() {
        return "video";
    }
}
