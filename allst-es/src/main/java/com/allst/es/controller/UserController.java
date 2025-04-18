package com.allst.es.controller;

import com.allst.es.annotations.Decrypt;
import com.allst.es.annotations.Encrypt;
import com.allst.es.bean.RespBean;
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

    @Encrypt
    @GetMapping("/enc")
    public RespBean getEncUser() {
        User user = new User();
        user.setId(100L);
        user.setName("Kang");
        user.setAge(28);
        user.setEmail("Kang@163.com");
        return RespBean.ok("ok", user);
    }

    @PostMapping("/dec")
    public RespBean addUser(@RequestBody @Decrypt User user) {
        System.out.println("user = " + user);
        return RespBean.ok("ok", user);
    }
}