package com.allst.boot.config;

import com.allst.boot.catalog.dto.CategoryView;
import com.allst.boot.catalog.dto.ProductView;
import com.allst.boot.common.GraphqlScalars;
import com.allst.boot.customer.dto.AddressView;
import com.allst.boot.customer.dto.CustomerView;
import com.allst.boot.order.dto.OrderItemView;
import com.allst.boot.order.dto.OrderView;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class GraphqlRuntimeConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiring -> wiring
                .scalar(GraphqlScalars.dateTime())
                .scalar(GraphqlScalars.bigDecimal())
                .type("Node", type -> type.typeResolver(env -> {
                    Object source = env.getObject();
                    String name = null;
                    if (source instanceof CustomerView) {
                        name = "Customer";
                    } else if (source instanceof AddressView) {
                        name = "Address";
                    } else if (source instanceof CategoryView) {
                        name = "Category";
                    } else if (source instanceof ProductView) {
                        name = "Product";
                    } else if (source instanceof OrderView) {
                        name = "Order";
                    } else if (source instanceof OrderItemView) {
                        name = "OrderItem";
                    }
                    if (name == null) {
                        return null;
                    }
                    return env.getSchema().getObjectType(name);
                }));
    }

    /**
     * 深度和复杂度限制写在文档里。GraphiQL 的内省查询很宽，复杂度阈值不能压得太低。
     * 自定义 Instrumentation 会盖掉默认的 DataLoader 调度，所以这里把调度器放回链里。
     */
    @Bean
    public GraphQlSourceBuilderCustomizer instrumentationCustomizer() {
        return builder -> builder.configureGraphQl(graphQl -> graphQl.instrumentation(new ChainedInstrumentation(Arrays.asList(
                new DataLoaderDispatcherInstrumentation(),
                new MaxQueryDepthInstrumentation(15),
                new MaxQueryComplexityInstrumentation(5000)
        ))));
    }

    @Bean
    public WebMvcConfigurer graphqlCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/graphql")
                        .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                        .allowedMethods("POST", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
