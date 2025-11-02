package com.allst.boot.controller;

import com.allst.boot.entity.Users;
import com.allst.boot.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.awt.*;

/**
 * @author Hutu
 * @since 2025-11-02 下午 08:09
 */
@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UsersService usersService;

    @PostMapping(path = "/save", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public Users saveUser(@RequestBody Users users) {
        return usersService.saveUser(users);
    }

    @GetMapping("/all")
    @ResponseBody
    public Page<Users> fetchAllUsers(Pageable request) {
        return usersService.fetchAllUsers(request);
    }
}
