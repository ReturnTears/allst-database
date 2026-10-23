package com.allst.boot.guide.service;

import com.allst.boot.guide.GuideSliceApplication;
import com.allst.boot.guide.entity.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = GuideSliceApplication.class)
class OptimisticLockRetryTest {

    @Autowired
    private AccountService accountService;

    @Test
    void concurrentWithdrawRetriesUntilBothSucceed() throws Exception {
        Account account = accountService.open("owner-" + System.nanoTime(), new BigDecimal("100.00"));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?> first = pool.submit(task(account.getId(), new BigDecimal("10.00"), ready, start));
            Future<?> second = pool.submit(task(account.getId(), new BigDecimal("20.00"), ready, start));
            ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
        assertEquals(0, new BigDecimal("70.00").compareTo(accountService.find(account.getId()).getBalance()));
    }

    private Runnable task(Long id, BigDecimal amount, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            try {
                start.await(5, TimeUnit.SECONDS);
                accountService.withdraw(id, amount);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        };
    }
}
