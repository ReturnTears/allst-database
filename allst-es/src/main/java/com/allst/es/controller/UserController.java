package com.allst.es.controller;

import com.allst.es.entity.User;
import com.allst.es.entity.EsUser;
import com.allst.es.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Hutu
 * @since 2025-01-02 下午 10:11
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public List<User> getUserList() {
        return userService.selectUserList();
    }

    @GetMapping("/es_list")
    public List<EsUser> getEsUserList() {
        return userService.selectEsUserList();
    }

    @GetMapping("/es_save")
    public String saveEsUser(@RequestParam("param") String param) {
        return userService.saveEsUser(param);
    }
}