package com.allst.dynamic.scenario.sharding;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 只有逻辑表 t_order 会被换成 t_order_0 / t_order_1。其它表，包括旧的 city，原样返回。
 */
@Configuration
public class OrderTableConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DynamicTableNameInnerInterceptor tableInterceptor = new DynamicTableNameInnerInterceptor();
        tableInterceptor.setTableNameHandler((sql, tableName) -> {
            if (!"t_order".equalsIgnoreCase(tableName)) {
                return tableName;
            }
            String actual = OrderTableContext.get();
            if (actual == null || actual.isBlank()) {
                return tableName;
            }
            return actual;
        });
        interceptor.addInnerInterceptor(tableInterceptor);
        return interceptor;
    }
}
