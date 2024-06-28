package com.allst.boot.config;

import com.allst.boot.component.GraphQLQuery;
import graphql.Scalars;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

/**
 * @author Hutu
 * @since 2024-06-28 下午 10:55
 */
@Configuration
public class GraphqlQueryConfig {

    @Bean
    public GraphQLScalarType dateTime() {
        return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("A date-time string at UTC, such as 2007-12-03T10:15:30Z, compliant with the ISO 8601 standard for representation of dates and times using the Gregorian calendar.")
                .build();
    }

    @Bean
    public GraphQLSchema graphQLSchema(GraphQLObjectType query) {
        return GraphQLSchema.newSchema()
                .query(query)
                .build();
    }

    @Bean
    public GraphQLObjectType query(GraphQLQuery graphQLQuery) {
        // 定义查询类型
        return GraphQLObjectType.newObject()
                .name("Query")
                .field(newFieldDefinition()
                        .name("getBookById")
                        .type(GraphQLTypeReference.typeRef("Book"))
                        .argument(arg -> arg.name("id").type(Scalars.GraphQLID))
                        .dataFetcher(graphQLQuery::getBookById))
                .build();
    }

    public String query() {
        return "query { book(id: 1) { id title author } }";
    }
}
