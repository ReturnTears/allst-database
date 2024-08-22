package com.allst.es.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author Hutu
 * @since 2024-08-22 下午 09:08
 */
@Configuration
public class ElasticConfig {
    static {
        // 获取elastic.store的保存位置
        String storePath = new File(ApplicationContext.class
                .getResource("/").getFile()).getParentFile()
                .getAbsolutePath() + "\\classes\\elastic.store";
        // 设置trustStore的位置
        System.setProperty("javax.net.ssl.trustStore",
                storePath);
        // 设置trustStore的读取密码
        System.setProperty("javax.net.ssl.trustStorePassword", "345678");
    }

    @Bean
    public RestClientBuilderCustomizer restClientBuilderCustomizer() {
        return new RestClientBuilderCustomizer() {
            @Override
            public void customize(RestClientBuilder builder) {
            }

            @Override
            public void customize(HttpAsyncClientBuilder builder) {
                builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            }
        };
    }
}
