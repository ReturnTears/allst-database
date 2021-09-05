package com.allst.mysql.repository;

import com.allst.mysql.entity.CUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author June
 * @since 2021年09月
 */
public interface CUserRepository extends JpaRepository<CUser, Long> {
    public List<CUser> findByPwd(String pwd);
}
