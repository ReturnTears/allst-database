package com.allst.boot;

import com.allst.boot.entity.Users;
import com.allst.boot.repository.jpa.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

/**
 * @author Hutu
 * @since 2025-11-02 下午 09:21
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UsersRepositoryTest {
    @Autowired
    private UsersRepository usersRepository;

    @Test
    void findAllTest() {
        List<Users> list = usersRepository.findAll();
        System.out.println("list : " + list);
    }
}
