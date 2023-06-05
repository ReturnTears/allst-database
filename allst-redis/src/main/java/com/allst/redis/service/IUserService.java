package com.allst.redis.service;

import com.allst.redis.pojo.User;

/**
 * @author Hutu
 * @since 2023-06-05 下午 11:56
 */
public interface IUserService {
    User getUsers(User user);
}
