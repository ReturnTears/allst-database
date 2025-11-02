package com.allst.boot.service;

import com.allst.boot.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Hutu
 * @since 2025-11-02 下午 08:10
 */
public interface UsersService {
    Users saveUser(Users users);
    Page<Users> fetchAllUsers(Pageable request);
}
