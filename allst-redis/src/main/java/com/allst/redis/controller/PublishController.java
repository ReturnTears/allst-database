package com.allst.redis.controller;

import com.allst.redis.service.IPublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hutu
 * @since 2024-08-06 下午 09:42
 */
@RestController
@RequestMapping("/publish")
public class PublishController {

    @Autowired
    private IPublishService publishService;

    @GetMapping("/msg")
    public String publishMsg() {
        boolean res = publishService.publishMessageByDefaultChannel("hello world");
        return res ? "publish success !" : "publish fail !";
    }
}
