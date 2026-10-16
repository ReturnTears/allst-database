package org.example.util;

import org.example.AntlrApp;
import org.example.ware.SpringContextHolder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class EasyUtil {

    public static String getUploadPath() {
        // 注意：静态 context 只在"本进程内启动过 Spring"时才有值，
        // 单独运行 main 时必须先在本进程里启动应用
        return SpringContextHolder.getProperty("rule.template");
    }

    public static void main(String[] args) {
        // 在同一 JVM 进程内先启动 Spring（不启 web 服务，避免占用 8000 端口）
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(AntlrApp.class)
                .web(WebApplicationType.NONE)
                .run(args);
        System.out.println("UPLOAD_PATH: " + getUploadPath());
        ctx.close();
    }
}
