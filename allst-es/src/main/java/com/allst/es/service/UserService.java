package com.allst.es.service;

import com.allst.es.entity.EsUser;
import com.allst.es.entity.User;

import java.util.List;

/**
 * @author Hutu
 * @since 2025-01-02 下午 10:08
 */
public interface UserService {
    List<User> selectUserList();
    List<EsUser> selectEsUserList();
    String saveEsUser(String param);
}
