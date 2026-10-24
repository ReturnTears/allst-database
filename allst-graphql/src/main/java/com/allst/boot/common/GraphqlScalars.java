package com.allst.boot.common;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public final class GraphqlScalars {

    private static final GraphQLScalarType DATE_TIME = GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("ISO-8601 local date-time")
            .coercing(new Coercing<LocalDateTime, String>() {
                @Override
                public String serialize(Object dataFetcherResult) {
                    if (dataFetcherResult instanceof LocalDateTime) {
                        return dataFetcherResult.toString();
                    }
                    throw new CoercingSerializeException("DateTime 需要 LocalDateTime");
                }

                @Override
                public LocalDateTime parseValue(Object input) {
                    try {
                        return LocalDateTime.parse(String.valueOf(input));
                    } catch (DateTimeParseException ex) {
                        throw new CoercingParseValueException("无法解析日期时间");
                    }
                }

                @Override
                public LocalDateTime parseLiteral(Object input) {
                    if (input instanceof StringValue) {
                        return parseValue(((StringValue) input).getValue());
                    }
                    throw new CoercingParseLiteralException("日期时间必须是字符串");
                }
            })
            .build();

    private static final GraphQLScalarType BIG_DECIMAL = GraphQLScalarType.newScalar()
            .name("BigDecimal")
            .description("Decimal string")
            .coercing(new Coercing<BigDecimal, String>() {
                @Override
                public String serialize(Object dataFetcherResult) {
                    if (dataFetcherResult instanceof BigDecimal) {
                        return ((BigDecimal) dataFetcherResult).toPlainString();
                    }
                    throw new CoercingSerializeException("BigDecimal 序列化失败");
                }

                @Override
                public BigDecimal parseValue(Object input) {
                    try {
                        if (input instanceof BigDecimal) {
                            return (BigDecimal) input;
                        }
                        if (input instanceof Number || input instanceof String) {
                            return new BigDecimal(input.toString());
                        }
                    } catch (NumberFormatException ex) {
                        throw new CoercingParseValueException("无法解析金额");
                    }
                    throw new CoercingParseValueException("金额必须是数字或字符串");
                }

                @Override
                public BigDecimal parseLiteral(Object input) {
                    if (input instanceof StringValue) {
                        return parseValue(((StringValue) input).getValue());
                    }
                    if (input instanceof IntValue) {
                        return new BigDecimal(((IntValue) input).getValue());
                    }
                    if (input instanceof FloatValue) {
                        return ((FloatValue) input).getValue();
                    }
                    throw new CoercingParseLiteralException("金额字面量无法解析");
                }
            })
            .build();

    private GraphqlScalars() {
    }

    public static GraphQLScalarType dateTime() {
        return DATE_TIME;
    }

    public static GraphQLScalarType bigDecimal() {
        return BIG_DECIMAL;
    }
}
