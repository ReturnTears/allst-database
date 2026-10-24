package com.allst.boot.config;

import com.allst.boot.common.ApiException;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GraphqlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    private static final Logger log = LoggerFactory.getLogger(GraphqlExceptionResolver.class);

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        Throwable known = findKnown(ex);
        if (known instanceof ApiException) {
            ApiException api = (ApiException) known;
            log.warn("GraphQL business error code={} message={}", api.getCode(), api.getMessage());
            return error(env, api.getMessage(), api.getCode());
        }
        if (known instanceof OptimisticLockingFailureException) {
            log.warn("Optimistic lock", known);
            return error(env, "数据已被其他人修改，请刷新后重试", "CONFLICT");
        }
        if (known instanceof DataIntegrityViolationException) {
            log.warn("Constraint violation", known);
            return error(env, "数据约束冲突", "CONFLICT");
        }
        log.error("GraphQL execution failed", ex);
        return error(env, "服务器内部错误", "INTERNAL");
    }

    private Throwable findKnown(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof ApiException
                    || current instanceof DataIntegrityViolationException
                    || current instanceof OptimisticLockingFailureException) {
                return current;
            }
            current = current.getCause();
        }
        return NestedExceptionUtils.getMostSpecificCause(ex);
    }

    private GraphQLError error(DataFetchingEnvironment env, String message, String code) {
        Map<String, Object> extensions = new LinkedHashMap<String, Object>();
        extensions.put("code", code);
        return GraphqlErrorBuilder.newError(env)
                .message(message)
                .errorType(ErrorType.DataFetchingException)
                .extensions(extensions)
                .build();
    }
}
