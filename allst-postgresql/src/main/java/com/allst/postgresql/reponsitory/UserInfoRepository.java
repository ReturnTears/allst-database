package com.allst.postgresql.reponsitory;

import com.allst.postgresql.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Hutu
 * @since 2024-08-25 下午 02:21
 */
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    UserInfo findByName(String name);

    UserInfo findByNameAndAge(String name, Integer age);

    @Query("from UserInfo u where u.name=:name")
    UserInfo findUser(@Param("name") String name);
}
