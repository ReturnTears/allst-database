package org.example.ware;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * 静态方法：供任意 Util 类调用
     */
    public static String getProperty(String key) {
        if (context == null) {
            throw new IllegalStateException("Spring Context 尚未初始化");
        }
        return context.getEnvironment().getProperty(key);
    }

    public static <T> T getProperty(String key, Class<T> targetType) {
        return context.getEnvironment().getProperty(key, targetType);
    }
}
