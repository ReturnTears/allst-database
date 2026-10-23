package com.allst.boot.guide.web;

import com.allst.boot.guide.entity.Account;
import com.allst.boot.guide.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/guide/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public Account open(@RequestParam String owner, @RequestParam BigDecimal balance) {
        return accountService.open(owner, balance);
    }

    @PostMapping("/{id}/withdraw")
    public Account withdraw(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return accountService.withdraw(id, amount);
    }

    @GetMapping("/{id}")
    public Account get(@PathVariable Long id) {
        return accountService.find(id);
    }
}
