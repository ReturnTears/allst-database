package com.allst.boot.repository.jpa;

import com.allst.boot.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Users的DAO层，用来对实体Users进行CRUD操作
 * @author Hutu
 * @since 2025-11-02 下午 08:07
 */
public interface UsersRepository extends JpaRepository<Users, Long> {
}
