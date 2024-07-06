package com.allst.redis.controller;

import com.allst.redis.anno.ApiLimit;
import com.allst.redis.enums.LimitType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hutu
 * @since 2024-07-06 下午 03:26
 */
@RestController
@RequestMapping("/api")
public class ApiLimitController {

    @RequestMapping("/limit")
    @ApiLimit(time = 10, count = 10, limitType = LimitType.IP)
    public String apiLimit() {
        return "apiLimit";
    }

}
