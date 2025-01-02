package com.allst.es.listener;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Hutu
 * @since 2025-01-02 下午 10:12
 */
@Component
public class HandleScheduled {
    @Scheduled(cron = "0/10 * * * * ?")   //每10秒执行一次
    public void execute() {
        System.out.println("欢迎访问 BinLog日志 ： " + LocalDateTime.now());
    }
}
