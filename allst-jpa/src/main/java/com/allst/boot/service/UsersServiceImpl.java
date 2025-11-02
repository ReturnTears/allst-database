package com.allst.boot.service;

import com.allst.boot.entity.Users;
import com.allst.boot.repository.jpa.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author Hutu
 * @since 2025-11-02 下午 08:12
 */
@Service
public class UsersServiceImpl implements UsersService {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public Users saveUser(Users users) {
        return usersRepository.save(users);
    }

    @Override
    public Page<Users> fetchAllUsers(Pageable request) {
        return usersRepository.findAll(request);
    }
}
