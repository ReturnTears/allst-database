package com.allst.es.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 启动监听器,监听MySQL binlog日志，并同步到ES
 *
 * @author Hutu
 * @since 2025-01-02 下午 10:41
 */
@Component
public class CommandLineRunnerHandler implements CommandLineRunner {
    @Autowired
    private MySQLBinlogListener listener;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("come in CommandLineRunner method run : " + Arrays.toString(args));
        listener.execListenerMysql();
    }
}
