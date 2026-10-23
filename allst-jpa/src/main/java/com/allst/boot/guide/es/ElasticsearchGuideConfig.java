package com.allst.boot.guide.es;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * 和 JPA 仓库并存。只有 elasticsearch profile 才创建客户端，避免本地没有 ES 时启动失败。
 */
@Configuration
@Profile("elasticsearch")
@EnableElasticsearchRepositories(basePackages = "com.allst.boot.guide.es")
public class ElasticsearchGuideConfig {

    @Bean
    public RestHighLevelClient elasticsearchClient(
            @Value("${spring.elasticsearch.uris:localhost:9200}") String uris) {
        ClientConfiguration configuration = ClientConfiguration.builder().connectedTo(uris).build();
        return RestClients.create(configuration).rest();
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient client) {
        return new ElasticsearchRestTemplate(client);
    }
}
