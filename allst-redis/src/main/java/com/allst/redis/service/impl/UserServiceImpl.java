package com.allst.redis.service.impl;

import com.allst.redis.anno.CheckVisitTimesAroundAnnotation;
import com.allst.redis.pojo.User;
import com.allst.redis.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * @author Hutu
 * @since 2023-06-05 下午 11:57
 */
@Service
public class UserServiceImpl implements IUserService {

    @CheckVisitTimesAroundAnnotation
    @Override
    public User getUsers(User user) {

        return user;
    }
}
