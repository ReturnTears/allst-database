package com.allst.boot.guide.service;

import com.allst.boot.guide.common.NotFoundException;
import com.allst.boot.guide.entity.Account;
import com.allst.boot.guide.repository.AccountRepository;
import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;

/**
 * 重试包在事务外面。每一次尝试都是一个新事务，能读到对方已经提交的版本号。
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionTemplate requiresNew;

    public AccountService(AccountRepository accountRepository, PlatformTransactionManager transactionManager) {
        this.accountRepository = accountRepository;
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.requiresNew = template;
    }

    public Account open(String owner, BigDecimal balance) {
        return requiresNew.execute(status -> {
            Account account = new Account();
            account.setOwner(owner);
            account.setBalance(balance);
            return accountRepository.save(account);
        });
    }

    public Account find(Long id) {
        return requiresNew.execute(status -> accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("账户不存在: " + id)));
    }

    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class, OptimisticLockException.class, StaleObjectStateException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 20)
    )
    public Account withdraw(Long id, BigDecimal amount) {
        return requiresNew.execute(status -> {
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("账户不存在: " + id));
            if (account.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("余额不足");
            }
            account.setBalance(account.getBalance().subtract(amount));
            return accountRepository.saveAndFlush(account);
        });
    }
}
