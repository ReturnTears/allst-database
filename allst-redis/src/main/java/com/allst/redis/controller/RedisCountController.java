package com.allst.redis.controller;

import com.allst.redis.anno.CheckVisitTimesAroundAnnotation;
import com.allst.redis.pojo.User;
import com.allst.redis.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Hutu
 * @since 2023-06-05 下午 11:27
 */
@RestController
@RequestMapping("/redis")
public class RedisCountController {

    @Autowired
    private IUserService userService;

    @GetMapping("selectOne")
    public String selectReaderInfo(@RequestParam(name = "userId") String userId) {
        User user = new User();
        user.setId(1);
        user.setName("kangkang");
        user.setUserId(userId);
        user.setAge(18);
        return userService.getUsers(user).toString();
    }

}
