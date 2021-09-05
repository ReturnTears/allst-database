package com.allst.mysql;

import com.allst.mysql.entity.CUser;
import com.allst.mysql.repository.CUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author June
 * @since 2021年09月
 */
@SpringBootTest(classes = AllstMysqlApplication.class)
public class EncryptorsTests {
    @Autowired
    private CUserRepository cUserRepository;

    @Test
    public void testEncryptAdd() {
        CUser cUser = new CUser();
        cUser.setName("zhoushenchen");
        cUser.setPwd("12345678");
        cUserRepository.save(cUser);
    }

    @Test
    public void testFindUser() {
        List<CUser> cUsers = cUserRepository.findByPwd("12345678");
        cUsers.forEach(System.out::println);
    }
}
