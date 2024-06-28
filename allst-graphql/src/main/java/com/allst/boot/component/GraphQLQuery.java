package com.allst.boot.component;

import com.allst.boot.entity.Book;
import graphql.kickstart.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.stereotype.Component;

/**
 * @author Hutu
 * @since 2024-06-28 下午 10:57
 */
@Component
public class GraphQLQuery implements GraphQLQueryResolver {
    public Book getBookById(String id) {
        return new Book();
    }

    public Object getBookById(DataFetchingEnvironment dataFetchingEnvironment) {
        return new Book();
    }
}
