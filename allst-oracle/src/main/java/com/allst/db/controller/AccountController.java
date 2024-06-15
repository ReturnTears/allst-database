package com.allst.db.controller;

import com.allst.db.entity.Account;
import com.allst.db.mapper.AccountMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

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
        //query.where(ACCOUNT.AGE.ge(20));
        query.where("age > 20");

        //通过 query 查询数据列表返回
        return accountMapper.selectListByQuery(query);
    }

    @GetMapping("/accounts/{id}")
    Account getById(@PathVariable("id") Long id) {
        return accountMapper.selectById(id);
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

    @GetMapping("/update")
    public void update() {
        Account account = UpdateEntity.of(Account.class, 4);
        account.setId(4L);
        account.setAge(24);
        account.setUserName("小张同学");
        accountMapper.update(account);
    }
}
