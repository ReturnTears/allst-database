package com.allst.db.controller;

import com.allst.db.entity.Account;
import com.allst.db.mapper.AccountMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static com.allst.db.entity.table.AccountTableDef.ACCOUNT;

/**
 * @author Hutu
 * @since 2023-07-26 下午 10:13
 */
@RestController
public class AccountController {
    @Autowired
    AccountMapper accountMapper;

    @GetMapping("/accounts")
    List<Account> selectList() {

        //构造 QueryWrapper，也支持使用 QueryWrapper.create() 构造，效果相同
        QueryWrapper query = new QueryWrapper();
        query.where(ACCOUNT.AGE.ge(20));

        //通过 query 查询数据列表返回
        return accountMapper.selectListByQuery(query);
    }

    @GetMapping("/save")
    public void save() {
        Account account = UpdateEntity.of(Account.class);
        account.setId(5L);
        account.setAge(24);
        account.setUserName("康帅");
        account.setBirthday(new Date());
        accountMapper.insert(account);
    }
}
